package ee.ut.cs.thesisworkflow.workflow;

import ee.ut.cs.thesisworkflow.object.PartnerLink;
import ee.ut.cs.thesisworkflow.object.WorkFlowActivity;
import ee.ut.cs.thesisworkflow.object.WorkFlowAssign;
import ee.ut.cs.thesisworkflow.object.WorkFlowInvoke;
import ee.ut.cs.thesisworkflow.object.WorkFlowProcess;
import ee.ut.cs.thesisworkflow.object.WorkFlowVariable;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends Activity {
    WorkFlowXmlParser workFlowXmlParser = new WorkFlowXmlParser();
    private WorkFlowProcess workFlowProcess;
    TextView partnerLinksTextView, variablesTextView, sequenceTextView;
    private Map<String,ArrayList<String>> graphMap;
    private Map<String,ArrayList<String>> graphMapBackword;
    private Map<String,WorkFlowActivity> activityMap;
    private ArrayList<WorkFlowVariable> variables;
    private ArrayList<PartnerLink> partnerLinks;
    private ArrayList<BluetoothDevice> bluetooths = new ArrayList<BluetoothDevice>();
    private static String TAG = "EXECUTION";
    private BluetoothAdapter mBluetoothAdapter = null;
    private AssetManager assetManager;
    private static final UUID MY_UUID = UUID.fromString("cc135924-a93b-11e4-89d3-123b93f75cba");
    StringWriter writer;
    private static String SERVER_BACKEND = "http://52.10.154.189/upload.php";

    //time meaue the time passed
    long startTime;
    long endTime;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            ConnectThread thread = null;
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                bluetooths.add(device);
                thread = new ConnectThread(device);
                thread.start();
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //--------Bluetooth part
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null){
            //the device doesn't support bluetooth
        }
        //--------Bluetooth part
        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        //comment to stop discoverty
        startTime = new Date().getTime();
        mBluetoothAdapter.startDiscovery();

        assetManager = getResources().getAssets();
        InputStream inputStream = null;
        partnerLinksTextView = (TextView)findViewById(R.id.partnerLinks);
        variablesTextView =(TextView) findViewById(R.id.variables);
        sequenceTextView =(TextView) findViewById(R.id.sequence);
        try{
            inputStream = assetManager.open("bpel06.xml" );
            if(inputStream !=null ){
                workFlowProcess = workFlowXmlParser.parse(inputStream);
            }
        }catch(IOException e){
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //test workflow offloading
        //===================
        WorkFlowGenerate generate =  WorkFlowGenerate.testWorkFlowInstance();
        try {
            writer = generate.offLoadingTask("enterPoint", "endPoint");
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //read bpel.wsdl and deploy to asset folder

        //save the dynamic bpel to internal storage
        saveBpelToInternalStorage("bpel.xml", writer.toString());
        saveBpelToInternalStorage("bpel.wsdl", bpelWsdl.toString());
        saveBpelToInternalStorage("deploy.xml", deploy.toString());
        String[] files;

        String bpelFilePath = getApplicationContext().getFilesDir() + "/" + "bpel.xml";
        String wsdlFilePath = getApplicationContext().getFilesDir() + "/" + "bpel.wsdl";
        String deployFilePath = getApplicationContext().getFilesDir() + "/" + "deploy.xml";
        Log.e("PATH", "bpelFilePath " + bpelFilePath );
        Log.e("PATH", "wsdlFilePath " + wsdlFilePath);
        Log.e("PATH", "deployFilePath " + deployFilePath );
        //TODO Need to add the wsdl file
        files = new String[] {bpelFilePath,wsdlFilePath,deployFilePath};
        Compress compress = new Compress(files,getApplicationContext().getFilesDir() + "/" + "testing.zip");
        compress.zip();
        //===================
        System.out.println("exectuion the flow");
//        BeginWorkFlow(workFlowProcess);
        new offloadingToServerAsyncTask().execute();
    }


    private void saveBpelToInternalStorage(String filename,String writer){
        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(writer.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) { }
            mmSocket = tmp;
        }

        public void run() {

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            manageConnectedSocket(mmSocket);
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
    private void manageConnectedSocket(BluetoothSocket socket){
        try {
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            String outputIP = convertStreamToString(inputStream);
            endTime = new Date().getTime();
            Log.d("TIME","Elapsed milliseconds: " + (endTime - startTime) );
            Log.d("TAG",outputIP);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
    //    private class ConnectedThread extends Thread {
//        private final BluetoothSocket mmSocket;
//        private final InputStream mmInStream;
//        private final OutputStream mmOutStream;
//
//        public ConnectedThread(BluetoothSocket socket) {
//            mmSocket = socket;
//            InputStream tmpIn = null;
//            OutputStream tmpOut = null;
//
//            // Get the input and output streams, using temp objects because
//            // member streams are final
//            try {
//                tmpOut = socket.getOutputStream();
//            } catch (IOException e) { }
//
//            mmInStream = tmpIn;
//            mmOutStream = tmpOut;
//        }
//
//        public void run() {
//            byte[] buffer = new byte[1024];  // buffer store for the stream
//            int bytes; // bytes returned from read()
//
//            // Keep listening to the InputStream until an exception occurs
//            try {
//                // Read from the InputStream
//                bytes = mmInStream.read(buffer);
//                // Send the obtained bytes to the UI activity
//            } catch (IOException e) {
//            }
//        }
//
//        /* Call this from the main activity to send data to the remote device */
//        public void write(byte[] bytes) {
//            try {
//                mmOutStream.write(bytes);
//            } catch (IOException e) { }
//        }
//
//        /* Call this from the main activity to shutdown the connection */
//        public void cancel() {
//            try {
//                mmSocket.close();
//            } catch (IOException e) { }
//        }
//    }
    private class offloadingToServerAsyncTask extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            try {
                //TODO need to from the internal storage of the zip file
//                InputStream offloadingStream = assetManager.open("HelloWorld2.zip" );
//                FileInputStream fin = openFileInput("testing.zip");
                String zipFileLocation = getApplicationContext().getFilesDir() + "/" + "testing.zip";
                File zipFilePath = new File(zipFileLocation);
                InputStream offloadingStream = null;

                offloadingStream = new BufferedInputStream(new FileInputStream(zipFilePath));

                OffloadingToServer offloadingToServer = new OffloadingToServer();
                offloadingToServer.PostBPELtoServer(SERVER_BACKEND, offloadingStream);
                if(offloadingStream !=null){
                    offloadingStream.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }


    }
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
            Log.d(TAG, "Starting " +  activityName );
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
    private  String bpelWsdl = "<message name=\"getTermRequest\">\n"+
            "  <part name=\"term\" type=\"xs:string\"/>\n"+
            "</message>\n"+
            "\n"+
            "<message name=\"getTermResponse\">\n"+
            "  <part name=\"value\" type=\"xs:string\"/>\n"+
            "</message>\n"+
            "\n"+
            "<portType name=\"tns:GetDataPortType\">\n"+
            "  <operation name=\"tns:GetData\">\n"+
            "    <input message=\"getRequest\"/>\n"+
            "    <output message=\"getResponse\"/>\n"+
            "  </operation>\n"+
            "</portType>";
    private String deploy = "<deploy xmlns=\"http://www.apache.org/ode/schemas/dd/2007/03\"\n" +
            "\txmlns:pns=\"http://ode/bpel/unit-test\" \n" +
            "\txmlns:wns=\"http://ode/bpel/unit-test.wsdl\">\n" +
            "\t<process name=\"pns:bpel\">\n" +
            "\t\t<active>true</active>\n" +
            "\t\t<provide partnerLink=\"bpelPartnerLink\">\n" +
            "\t\t\t<service name=\"wns:bpelService\" port=\"bpelPort\"/>\n" +
            "\t\t</provide>\n" +
            "\t</process>\n" +
            "</deploy>";
}