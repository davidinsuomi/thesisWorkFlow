package ee.ut.cs.thesisworkflow.connection;

/**
 * Created by weiding on 28/03/15.
 */
import android.util.Log;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/*
 * This class implements a simple CoAP client for testing purposes.
 *
 * Usage: java -jar SampleClient.jar [-l] METHOD URI [PAYLOAD]
 *   METHOD  : {GET, POST, PUT, DELETE, DISCOVER}
 *   URI     : The URI to the remote endpoint or resource
 *   PAYLOAD : The data to send with the request
 * Options:
 *   -l      : Wait for multiple responses
 *
 * Examples:
 *   SampleClient DISCOVER coap://localhost
 *   SampleClient POST coap://someServer.org:61616 my data
 *
 */

import coap.*;

public class CoapConnection {
    public static final String DISCOVERY_RESOURCE = "/.well-known/core";
    public String method;
    public String uri;
    public String payload;
    boolean loop   = false;
    public static final String TAG = "CoapConnection";

    public CoapConnection(String method, String uri, String payload){
        this.method = method;
        this.uri = uri;
        this.payload = payload;
    }

    public void Connect(){
        Request request = newRequest(method);

        if (method.equals("DISCOVER") && !uri.endsWith(DISCOVERY_RESOURCE)) {
            uri = uri + DISCOVERY_RESOURCE;
        }

        try {
            request.setURI(new URI(uri));
        } catch (URISyntaxException e) {
            Log.e(TAG,"Failed to parse URI: " + e.getMessage());
            return;
        }

        // set request payload
        request.setPayload(payload);

        // enable response queue in order to use blocking I/O
        request.enableResponseQueue(true);

        // execute request
        try {
            request.execute();
        } catch (IOException e) {
            Log.e(TAG, "Failed to execute request: " + e.getMessage());
            return;
        }

        // loop for receiving multiple responses
        do {

            // receive response

            Log.e(TAG,"Receiving response...");
            Response response = null;
            try {
                response = request.receiveResponse();

                // check for indirect response
                if (response != null && response.isEmptyACK()) {
                    response.log();
                    Log.e(TAG,"Request acknowledged, waiting for separate response...");

                    response = request.receiveResponse();
                }

            } catch (InterruptedException e) {
                Log.e(TAG,"Failed to receive response: " + e.getMessage());
                return;
            }

            // output response

            if (response != null) {

                response.log();
                Log.e(TAG,"Round Trip Time (ms): " + response.getRTT());

                // check of response contains resources
                if (response.hasFormat(MediaTypeRegistry.LINK_FORMAT)) {

                    String linkFormat = response.getPayloadString();

                    // create resource three from link format
                    Resource root = RemoteResource.newRoot(linkFormat);
                    if (root != null) {

                        // output discovered resources
                        Log.e(TAG,"\nDiscovered resources:");
                        root.log();

                    } else {
                        Log.e(TAG,"Failed to parse link format");
                    }
                } else {

                    // check if link format was expected by client
                    if (method.equals("DISCOVER")) {
                        Log.e(TAG,"Server error: Link format not specified");
                    }
                }

            } else {

                // no response received
                // calculate time elapsed
                long elapsed = System.currentTimeMillis() - request.getTimestamp();

                Log.e(TAG,"Request timed out (ms): " + elapsed);
                break;
            }

        } while (loop);
    }


    /*
 * Instantiates a new request based on a string describing a method.
 *
 * @return A new request object, or null if method not recognized
 */
    private static Request newRequest(String method) {
        if (method.equals("GET")) {
            return new GETRequest();
        } else if (method.equals("POST")) {
            return new POSTRequest();
        } else if (method.equals("PUT")) {
            return new PUTRequest();
        } else if (method.equals("DELETE")) {
            return new DELETERequest();
        } else if (method.equals("DISCOVER")){
            return new GETRequest();
        } else if (method.equals("OBSERVE")){
            return new GETRequest();
        } else {
            return null;
        }
    }

}
