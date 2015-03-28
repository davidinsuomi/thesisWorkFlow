package ee.ut.cs.thesisworkflow.workflow;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

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

    private Map<String,ArrayList<String>> graphMap;
    private Map<String,ArrayList<String>> graphMapBackword;
    private Map<String,WorkFlowActivity> activityMap;
    private ArrayList<WorkFlowVariable> variables;
    private ArrayList<PartnerLink> partnerLinks;

    public void BeginWorkFlow(WorkFlowProcess workflowProcess){
        graphMap = workflowProcess.graphMap;
        graphMapBackword = workflowProcess.graphMapBackword;
        activityMap = workflowProcess.activityMap;
        variables = workflowProcess.variables;
        partnerLinks = workflowProcess.partnerLinks;
        ProcessWorkFlow("Beginnering");
    }
    private void ProcessWorkFlow(String graphKey){
        if(!IsLastExecutionInGraph(graphKey)&& IsPreviousTaskFinish(graphKey)){
            ArrayList<String> graphValues = graphMap.get(graphKey);
            for(int i=0; i < graphValues.size() ; i++){
                //sequence task
                ExecutionTask task = new ExecutionTask(graphValues.get(i));
                task.start();
            }
        }
    }
    class ExecutionTask implements Runnable{
        private String activityName;
        private Thread t;
        @Override
        public void run() {
            // TODO Auto-generated method stub
            WorkFlowActivity activity = activityMap.get(activityName);
            if(activity instanceof WorkFlowInvoke){
                WorkFlowInvoke workFlowInvoke= (WorkFlowInvoke) activity;
                if(workFlowInvoke.operation.contains("post")){
                    try {
                        PostToServer(workFlowInvoke);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }else{
                    try {
                        FetchFromServer(workFlowInvoke);
                    } catch (ClientProtocolException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }else if(activity instanceof WorkFlowAssign){
                AssignVariable((WorkFlowAssign) activity);
            }
            activity.status.compareAndSet(false, true);
            ProcessWorkFlow(activityName);
        }
        ExecutionTask(String _activityName){
            activityName = _activityName;
        }
        public void start(){
            Log.d(TAG, "Starting " + activityName);
            if (t == null)
            {
                t = new Thread (this, activityName);
                t.start ();
            }
        }

    }

    private void PostToServer(WorkFlowInvoke workFlowInvoke) throws ClientProtocolException, IOException{
        String URLPATH = "";
        WorkFlowVariable inputVariable = null, outputVariable = null;
        for(WorkFlowVariable variable : variables){
            if(variable.name.equals(workFlowInvoke.inputVariable)){
                inputVariable = variable;
            }else if(variable.name.equals(workFlowInvoke.outputVariable)){
                outputVariable = variable;
            }
        }
        for (PartnerLink partnerLink : partnerLinks) {
            if (partnerLink.name.equals(workFlowInvoke.partnerLink)) {
                URLPATH = partnerLink.URL;
            }
        }

        String FullURL = URLPATH + "/" + workFlowInvoke.operation;
        Log.d(TAG, "POST TO server " + FullURL);

        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(FullURL);
        if (inputVariable.value != null) {
            Log.d(TAG, "POST TO server not null");
            httpPost.setEntity(new ByteArrayEntity(inputVariable.value));
            HttpResponse response = httpclient.execute(httpPost);
        }
//        byte[] content = EntityUtils.toByteArray(response.getEntity());
//        outputVariable.value = content;
    }

    private void FetchFromServer(WorkFlowInvoke workFlowInvoke) throws ClientProtocolException, IOException {
        String URLPATH = "";
        byte[] byteFromServer;
        for (PartnerLink partnerLink : partnerLinks) {
            if (partnerLink.name.equals(workFlowInvoke.partnerLink)) {
                URLPATH = partnerLink.URL;
            }
        }
        String FullURL = URLPATH + "/" + workFlowInvoke.operation;
        Log.d(TAG, "fetch from server " + FullURL);

        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = httpclient.execute(new HttpGet(URLPATH));
        StatusLine statusLine = response.getStatusLine();
        if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            response.getEntity().writeTo(out);
            out.close();
            byteFromServer = out.toByteArray();
            // ..more logic
        } else {
            // Closes the connection.
            response.getEntity().getContent().close();
            throw new IOException(statusLine.getReasonPhrase());
        }

        for(WorkFlowVariable variable : variables){
            if(variable.name.equals(workFlowInvoke.outputVariable)){
                variable.value = byteFromServer;
                Log.d(TAG, "get TO server not null" + byteFromServer.length);
            }
        }
    }

    private void AssignVariable(WorkFlowAssign assign){
        String from = ((WorkFlowAssign) assign).from;
        String to = ((WorkFlowAssign) assign).to;
        Log.d(TAG,"copy from" + from + " to " + to);
        WorkFlowVariable copyFromVariable = null, copyToVariable = null;
        for (WorkFlowVariable variable : variables) {
            if(variable.name.equals(to)){
                copyToVariable = variable;
            }else if(variable.name.equals(from)){
                copyFromVariable = variable;
            }
        }

        copyToVariable.value = copyFromVariable.value;

    }
    private boolean IsLastExecutionInGraph(String graphKey){
        ArrayList<String> graphValues = graphMap.get(graphKey);
        if(graphValues.get(0).equals("ending"))
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
}
