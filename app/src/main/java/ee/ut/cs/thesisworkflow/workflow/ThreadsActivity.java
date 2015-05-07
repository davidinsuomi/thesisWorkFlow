package ee.ut.cs.thesisworkflow.workflow;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by weiding on 03/05/15.
 */
public class ThreadsActivity extends Activity{

    private static String TAG = "ThreadsActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        for (int i =0 ; i < 200 ; i++){
            new ExecutionTask().start();
//            new HttpFetchTask().execute();
        }
        Log.i(TAG,"THREAD CREATE FINISH");

    }

    class ExecutionTask implements  Runnable{
        private Thread t;
        @Override
        public void run() {
            HttpConnect();
        }

        public void start(){
            if (t == null) {
                t = new Thread(this);
                t.start();
            }
        }
    }
    private void HttpConnect(){
        HttpClient httpclient = new DefaultHttpClient();
        long startTime = System.nanoTime();
        HttpResponse response = null;
        try {
            response = httpclient.execute(new HttpGet("http://192.168.0.101:8080"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        StatusLine statusLine = response.getStatusLine();
        if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                response.getEntity().writeTo(out);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            long elapsedTime = System.nanoTime() - startTime;
            Log.i(TAG, "Round Trip Time (ms): " + elapsedTime / 1000000);
            Log.i(TAG, "" + out.toString().length());
        } else {
            // Closes the connection.
            try {
                response.getEntity().getContent().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                throw new IOException(statusLine.getReasonPhrase());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    private class HttpFetchTask extends AsyncTask<Void, Void, byte[]> {

        @Override
        protected byte[] doInBackground(Void... params) {
            HttpConnect();
            return null;
        }
    }

}
