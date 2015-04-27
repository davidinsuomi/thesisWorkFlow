package ee.ut.cs.thesisworkflow.Server;

import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import NanoHTTPD.NanoHTTPD;
import ee.ut.cs.thesisworkflow.object.WorkFlowProcess;
import ee.ut.cs.thesisworkflow.workflow.WorkFlowExecution;
import ee.ut.cs.thesisworkflow.workflow.WorkFlowXmlParser;

/**
 * Created by weiding on 05/04/15.
 */
public class BpelHttpServer extends NanoHTTPD {

    private static String TAG = "BpelServer";
    public BpelHttpServer(){
        super(8080);
    }
    @Override public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        String uri = session.getUri();
        Log.e(TAG, "URI:" + uri);

        String msg= "";
        if(method.toString().equals("GET")) {
            msg = "get request";
            Log.e(TAG,"GET REQUEST");
        }else{
            //Post
            Map<String, String> postData = new HashMap<String,String>();
            Log.e(TAG,"POST REQUEST");
            try {
                session.parseBody(postData);
            } catch (IOException ioe) {
                return new Response(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
            } catch (ResponseException re) {
                return new Response(re.getStatus(), MIME_PLAINTEXT, re.getMessage());
            }

            for(Map.Entry<String,String> entry : postData.entrySet()){
                Log.e(TAG,entry.getKey() + "/" + entry.getValue());
                msg = "post request";
            }

            ParsingBpelWorkFlow(postData.get("postData"));
        }


        return new NanoHTTPD.Response(msg);
    }


    private void ParsingBpelWorkFlow(String bpelXmlString){
        longInfo(bpelXmlString);
        InputStream stream = new ByteArrayInputStream(bpelXmlString.getBytes(StandardCharsets.UTF_8));
        WorkFlowXmlParser workFlowXmlParser = new WorkFlowXmlParser();
        WorkFlowExecution workFlowExecution = new WorkFlowExecution();
        WorkFlowProcess workFlowProcess = null;
        try {
             workFlowProcess = workFlowXmlParser.parse(stream);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(workFlowProcess!= null) {
            workFlowExecution.BeginWorkFlow(workFlowProcess);
        }
    }

    public  void longInfo(String str) {
        if(str.length() > 4000) {
            Log.e(TAG, str.substring(0, 4000));
            longInfo(str.substring(4000));
        } else
            Log.e(TAG, str);
    }
}
