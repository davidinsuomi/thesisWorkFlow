package ee.ut.cs.thesisworkflow.workflow;

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

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import ee.ut.cs.thesisworkflow.object.WorkFlowProcess;

public class MainActivity extends Activity {
    private static final UUID MY_UUID = UUID.fromString("cc135924-a93b-11e4-89d3-123b93f75cba");
    private static String SERVER_BACKEND = "http://52.10.154.189/upload.php";
    private final String TAG = "MainActivity";
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            ConnectThread thread = null;
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                bluetooths.add(device);
                thread = new ConnectThread(device);
                thread.start();
            }
        }
    };
    WorkFlowXmlParser workFlowXmlParser = new WorkFlowXmlParser();
    //meausre the time passed
    long startTime;
    long endTime;
    private WorkFlowProcess workFlowProcess;
    private ArrayList<BluetoothDevice> bluetooths = new ArrayList<BluetoothDevice>();
    private BluetoothAdapter mBluetoothAdapter = null;
    private WorkFlowExecution workFlowExecution = new WorkFlowExecution();
    private AssetManager assetManager;

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TestWorkFlowExecution();
//        TestWorkFlowGenerate();

    }

    private void TestWorkFlowExecution() {
        assetManager = getResources().getAssets();
        InputStream inputStream = null;

        try {
            inputStream = assetManager.open("bpel06.xml");
            if (inputStream != null) {
                workFlowProcess = workFlowXmlParser.parse(inputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        workFlowExecution.BeginWorkFlow(workFlowProcess);

    }

    private void TestWorkFlowGenerate() {
        //test workflow offloading
//        //===================
//        WorkFlowGenerate generate = new WorkFlowGenerate(workFlowProcess);
//        StringWriter writer = new StringWriter();
//        try {
//            writer = generate.OffloadingTask("getData2", "postData4");
//        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        longInfo(writer.toString());

    }

    public void longInfo(String str) {
        if (str.length() > 4000) {
            Log.e(TAG, str.substring(0, 4000));
            longInfo(str.substring(4000));
        } else
            Log.e(TAG, str);
    }

    private void TestWorkflowUploadInApacheOde() {
        //save the dynamic bpel to internal storage
//        saveBpelToInternalStorage("bpel.xml", writer.toString());
        saveBpelToInternalStorage("bpel.wsdl", bpelWsdl.toString());
        saveBpelToInternalStorage("deploy.xml", deploy.toString());
        String[] files;

        String bpelFilePath = getApplicationContext().getFilesDir() + "/" + "bpel.xml";
        String wsdlFilePath = getApplicationContext().getFilesDir() + "/" + "bpel.wsdl";
        String deployFilePath = getApplicationContext().getFilesDir() + "/" + "deploy.xml";

        //TODO Need to add the wsdl file
        files = new String[]{bpelFilePath, wsdlFilePath, deployFilePath};
        Compress compress = new Compress(files, getApplicationContext().getFilesDir() + "/" + "testing.zip");
        compress.zip();


        new offloadingToServerAsyncTask().execute();

    }

    // in order to enable ble call it onStart
    private void InitBluetooth() {
        //--------Bluetooth part
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            //the device doesn't support bluetooth
        }
        //--------Bluetooth part
        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        //comment to stop discoverty
        startTime = new Date().getTime();
        mBluetoothAdapter.startDiscovery();

    }

    private void saveBpelToInternalStorage(String filename, String writer) {
        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(writer.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void manageConnectedSocket(BluetoothSocket socket) {
        try {
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            String outputIP = convertStreamToString(inputStream);
            endTime = new Date().getTime();
            Log.d("TIME", "Elapsed milliseconds: " + (endTime - startTime));
            Log.d("TAG", outputIP);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    class CoapConnectionTask extends AsyncTask<Void, Void, Void> {
//
//        @Override
//        protected Void doInBackground(Void... params) {
//
//            //testing the coap
//            CoapConnection coapConnection = new CoapConnection("DISCOVER", "coap://localhost", null);
//            coapConnection.Connect();
//            return null;
//        }
//    }

    // Bluetooth part
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
            } catch (IOException e) {
            }
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
                } catch (IOException closeException) {
                }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            manageConnectedSocket(mmSocket);
        }

        /**
         * Will cancel an in-progress connection, and close the socket
         */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    private class offloadingToServerAsyncTask extends AsyncTask<Void, Void, Void> {

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
                if (offloadingStream != null) {
                    offloadingStream.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }


    }

    private String bpelWsdl = "<message name=\"getTermRequest\">\n" +
            "  <part name=\"term\" type=\"xs:string\"/>\n" +
            "</message>\n" +
            "\n" +
            "<message name=\"getTermResponse\">\n" +
            "  <part name=\"value\" type=\"xs:string\"/>\n" +
            "</message>\n" +
            "\n" +
            "<portType name=\"tns:GetDataPortType\">\n" +
            "  <operation name=\"tns:GetData\">\n" +
            "    <input message=\"getRequest\"/>\n" +
            "    <output message=\"getResponse\"/>\n" +
            "  </operation>\n" +
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