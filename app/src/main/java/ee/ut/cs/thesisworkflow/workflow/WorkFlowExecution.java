package ee.ut.cs.thesisworkflow.workflow;

import android.os.SystemClock;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.Lock;

import coap.GETRequest;
import coap.MediaTypeRegistry;
import coap.POSTRequest;
import coap.RemoteResource;
import coap.Request;
import coap.Resource;
import coap.Response;
import ee.ut.cs.thesisworkflow.object.PartnerLink;
import ee.ut.cs.thesisworkflow.object.WorkFlowActivity;
import ee.ut.cs.thesisworkflow.object.WorkFlowAssign;
import ee.ut.cs.thesisworkflow.object.WorkFlowInvoke;
import ee.ut.cs.thesisworkflow.object.WorkFlowProcess;
import ee.ut.cs.thesisworkflow.object.WorkFlowVariable;

/**
 * Created by weiding on 28/03/15.
 */
public class WorkFlowExecution {

    private static String TAG = "EXECUTION";

    private Map<String, ArrayList<String>> graphMap;
    private Map<String, ArrayList<String>> graphMapBackword;
    private Map<String, WorkFlowActivity> activityMap;
    private ArrayList<WorkFlowVariable> variables;
    private ArrayList<PartnerLink> partnerLinks;
    private WorkFlowDecisionMaker decisionMaker;
    private Object threadLock = new Object();

    public void BeginWorkFlow(WorkFlowProcess workflowProcess) {
        graphMap = workflowProcess.graphMap;
        graphMapBackword = workflowProcess.graphMapBackword;
        activityMap = workflowProcess.activityMap;
        variables = workflowProcess.variables;
        partnerLinks = workflowProcess.partnerLinks;
        decisionMaker = new WorkFlowDecisionMaker(workflowProcess);
        ProcessWorkFlow("Beginning");
    }

    private void ProcessWorkFlow(String graphKey) {
        if (!IsLastExecutionInGraph(graphKey) && IsPreviousTaskFinish(graphKey)) {
            decisionMaker.MakeDecision(graphKey);
            Log.e(TAG,"execute task" + graphKey);
            ArrayList<String> graphValues = graphMap.get(graphKey);
            for (int i = 0; i < graphValues.size(); i++) {
                Log.e("SIZE","PROCESS workflow size" + graphValues.size());
                ExecutionTask task = new ExecutionTask(graphValues.get(i));
                task.start();
            }
        }
        if(graphKey.equals("endPoint")) {
            long endTime = System.currentTimeMillis();
            long duration = endTime - MainActivity.testCaseStartTime;
            Log.e("DURATION", "" + duration + "ms");
        }

    }

    private void PostToServer(WorkFlowInvoke workFlowInvoke) throws ClientProtocolException, IOException {
        String URLPATH = "";
        WorkFlowVariable inputVariable = null, outputVariable = null;
        for (WorkFlowVariable variable : variables) {
            if (variable.name.equals(workFlowInvoke.inputVariable)) {
                inputVariable = variable;
            } else if (variable.name.equals(workFlowInvoke.outputVariable)) {
                outputVariable = variable;
            }
        }
        for (PartnerLink partnerLink : partnerLinks) {
            if (partnerLink.name.equals(workFlowInvoke.partnerLink)) {
                URLPATH = partnerLink.URL;
            }
        }

        if (URLPATH.startsWith("coap")) {
            if(inputVariable.GetData() != null) {
                PostCoap(URLPATH, inputVariable.GetData());
            }else{
                Log.e(TAG,"POST COAP DATA IS NULL");
            }
        } else {

            String FullURL = URLPATH;
            Log.d(TAG, "POST TO server " + FullURL);
            BpelHttpPost(FullURL,inputVariable);

        }
    }


    private String BpelHttpPost(String url, WorkFlowVariable inputVariable) throws IOException {
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        if (inputVariable.GetValue() != null) {
            Log.d(TAG, "inputvariable" + inputVariable.name + "POST TO server last string:" + inputVariable.GetData().substring(inputVariable.GetData().length() -10 ));
            httpPost.setEntity(new ByteArrayEntity(inputVariable.GetValue()));
            HttpResponse response = httpclient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity, "UTF-8");
            Log.e(TAG,"OFFLOADING RESPONSE " + responseString);
            long endTime = System.currentTimeMillis();
            long duration = endTime - MainActivity.testCaseStartTime;
            Log.e("DURATION", "" + duration + "ms");
            return responseString;
        }
        return null;
    }

    private void FetchFromServer(WorkFlowInvoke workFlowInvoke) throws IOException {
        String URLPATH = "";
        String fullUri = "";
        byte[] byteFromServer = null;
        for (PartnerLink partnerLink : partnerLinks) {
            if (partnerLink.name.equals(workFlowInvoke.partnerLink)) {
                URLPATH = partnerLink.URL;
            }
        }

        if(URLPATH.startsWith("$")){
            URLPATH =GetUriPathFromList(workFlowInvoke.name, URLPATH.substring(1));
        }
        if(URLPATH.startsWith("coap")){
            fullUri = URLPATH;
            if (workFlowInvoke.operation != null && workFlowInvoke.operation.contains("well-known")) {
                fullUri = fullUri + workFlowInvoke.operation;
                byteFromServer = FetchCoap(fullUri);
            } else {
                byteFromServer = FetchCoap(fullUri);
            }
        }else if (fullUri.startsWith("http")){
            fullUri = URLPATH + "/" + workFlowInvoke.operation;
            byteFromServer = fetchHttp(fullUri);
        }

//        previous logic, too confusing
//        if (URLPATH.startsWith("coap")) {
//            fullUri = URLPATH;
//            if (workFlowInvoke.operation != null && workFlowInvoke.operation.contains("well-known")) {
//                fullUri = fullUri + workFlowInvoke.operation;
//                byteFromServer = FetchCoap(fullUri);
//            } else {
//                byteFromServer = FetchCoap(fullUri);
//            }
//        } else if (URLPATH.startsWith("$")) {
//            fullUri = GetUriPathFromList(workFlowInvoke.name, URLPATH.substring(1));
//            Log.d(TAG, "$coap uri " + fullUri);
//            if(fullUri.startsWith("http")){
//                byteFromServer = fetchHttp(fullUri);
//            }else if (fullUri.startsWith("coap")) {
//                if (workFlowInvoke.operation != null && workFlowInvoke.operation.contains("well-known")) {
//                    fullUri = fullUri + workFlowInvoke.operation;
//                    byteFromServer = FetchCoap(fullUri);
//                } else {
//                    byteFromServer = FetchCoap(fullUri);
//                }
//            }
//        } else {
//            fullUri = URLPATH + "/" + workFlowInvoke.operation;
//            byteFromServer = fetchHttp(fullUri);
//        }

        for (WorkFlowVariable variable : variables) {
            if (variable.name.equals(workFlowInvoke.outputVariable)) {
                if(byteFromServer != null) {
                    variable.SetValue(byteFromServer);
                    String log = new String(byteFromServer, "UTF-8");
                    Log.d(TAG, "get TO server not null" + log);
                }
            }
        }
    }

    private String GetUriPathFromList(String invokeName, String partnerLinkUri) {
        int loopCount = CalculateLoopCount(invokeName);
        String uri = null;
        for (WorkFlowVariable variable : variables) {
            if (variable.name.equals(partnerLinkUri)) {
                uri = variable.datas.get(loopCount);
            }
        }

        return uri;

    }

    private int CalculateLoopCount(String name) {
        // if last character of name has number is means need to have position in list
        char lastCharacter = name.charAt(name.length() - 1);
        if (lastCharacter >= '0' && lastCharacter <= '9') {
            return Character.getNumericValue(lastCharacter);
        } else {
            return 0;
        }
    }

    private byte[] PostCoap(String uri, String payload) {
        Request request = new POSTRequest();
        return CoapConnection(request, payload, uri);
    }

    private byte[] FetchCoap(String uri) {
        Request request = new GETRequest();
        return CoapConnection(request, null, uri);
    }

    private byte[] CoapConnection(Request request, String payload, String uri) {

        byte[] byteFromServer = null;
        try {
            request.setURI(new URI(uri));
        } catch (URISyntaxException e) {
            Log.e(TAG, "Failed to parse URI: " + e.getMessage());
            return null;
        }
        request.setPayload(payload);
        // enable response queue in order to use blocking I/O
        request.enableResponseQueue(true);

        // execute request
        try {
            request.execute();
        } catch (IOException e) {
            Log.e(TAG, "Failed to execute request: " + e.getMessage());
            return null;
        }

        Response response = null;
        try {
            response = request.receiveResponse();

            // check for indirect response
            if (response != null && response.isEmptyACK()) {
                response.log();
                Log.e(TAG, "Request acknowledged, waiting for separate response...");

                response = request.receiveResponse();
            }

        } catch (InterruptedException e) {
            Log.e(TAG, "Failed to receive response: " + e.getMessage());
            return null;
        }

        // output response

        if (response != null) {
            response.log();
            byteFromServer = response.getPayload();
            Log.e(TAG, "Coap Round Trip Time (ms): " + response.getRTT());

            // check of response contains resources
            if (response.hasFormat(MediaTypeRegistry.LINK_FORMAT)) {

                String linkFormat = response.getPayloadString();

                // create resource three from link format
                Resource root = RemoteResource.newRoot(linkFormat);
                if (root != null) {
                    // output discovered resources
                    Log.e(TAG, "\nDiscovered resources:");
                    root.log();

                } else {
                    Log.e(TAG, "Failed to parse link format");
                }
            }

        } else {
            // no response received
            // calculate time elapsed
            long elapsed = System.nanoTime() - request.getTimestamp();
            Log.e(TAG, "Request timed out (ms): " + elapsed);
        }


        return byteFromServer;
    }

    private byte[] fetchHttp(String uri) throws IOException {
        HttpClient httpclient = new DefaultHttpClient();
        long startTime = System.nanoTime();
        HttpResponse response = httpclient.execute(new HttpGet(uri));
        StatusLine statusLine = response.getStatusLine();
        if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            response.getEntity().writeTo(out);
            out.close();
            long elapsedTime = System.nanoTime() - startTime;
            Log.i("HTTPDEBUG", "HTTP Round Trip Time (ms): " + elapsedTime/1000000);
            Log.i("HTTPSIZE", "" + out.toString().length());
            return out.toByteArray();
            // ..more logic
        } else {
            // Closes the connection.
            response.getEntity().getContent().close();
            throw new IOException(statusLine.getReasonPhrase());
        }
    }

    private void AssignVariable(WorkFlowAssign assign) {
        String from = ((WorkFlowAssign) assign).from;
        String to = ((WorkFlowAssign) assign).to;
        Log.d(TAG, "copy from" + from + " to " + to);
        WorkFlowVariable copyFromVariable = null, copyToVariable = null;
        for (WorkFlowVariable variable : variables) {
            if (variable.name.equals(to)) {
                copyToVariable = variable;
            } else if (variable.name.equals(from)) {
                copyFromVariable = variable;
            }
        }
        if(copyFromVariable.GetValue() != null) {
            copyToVariable.SetValue(copyFromVariable.GetValue());
        }
    }

    private boolean IsLastExecutionInGraph(String graphKey) {
        ArrayList<String> graphValues = graphMap.get(graphKey);
        if (graphValues.get(0).equals("ending"))
            return true;
        else
            return false;
    }

    private boolean IsPreviousTaskFinish(String graphKey) {
        ArrayList<String> nextValues = graphMap.get(graphKey);
        if (graphMapBackword.containsKey(nextValues.get(0))) {
            ArrayList<String> graphValues = graphMapBackword.get(nextValues.get(0));
            for (int i = 0; i < graphValues.size(); i++) {
                if (activityMap.containsKey(graphValues.get(i))) {
                    WorkFlowActivity activity = activityMap.get(graphValues
                            .get(i));
                    if (!activity.status.get()) {
                        Log.d(TAG, graphKey + "previous not finish");
                        return false;
                    }
                } else {
                    // the first element
                    return true;
                }
            }
            return true;
        } else {
            // the first execution task.
            return true;
        }
    }

    class ExecutionTask implements Runnable {
        private String activityName;
        private Thread t;

        ExecutionTask(String _activityName) {
            activityName = _activityName;
        }

        @Override
        public void run() {
            WorkFlowActivity activity = activityMap.get(activityName);
            if (activity instanceof WorkFlowInvoke) {
                WorkFlowInvoke workFlowInvoke = (WorkFlowInvoke) activity;
                if (workFlowInvoke.operation != null && workFlowInvoke.operation.contains("POST")) {
                    try {
                        PostToServer(workFlowInvoke);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        FetchFromServer(workFlowInvoke);
                    } catch (ClientProtocolException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if (activity instanceof WorkFlowAssign) {
                AssignVariable((WorkFlowAssign) activity);
            }
            Log.e(TAG,"SETTING FINISH TAG" + activity.name);
            activity.status.compareAndSet(false, true);
            ProcessWorkFlow(activityName);
        }

        public void start() {
            Log.d(TAG, "Starting " + activityName);
            if (t == null) {
                t = new Thread(this, activityName);
                t.start();
            }
        }
    }
}
