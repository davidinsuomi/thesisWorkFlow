package ee.ut.cs.thesisworkflow.workflow;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import ee.ut.cs.thesisworkflow.Data.CollaborateDevice;
import ee.ut.cs.thesisworkflow.Data.Conf;

/**
 * Created by weiding on 27/04/15.
 */
public class WorkFlowCollaborate {
    int totalCPU;
    int totalBattery;
    int totalRAM;
    int totalBandwidth;
    public static String TAG = "WorkFlowCollaborate";
    int hightWeight;
    public void GetCollaborateDevicesStatus(){
//        ResetValue();
        new fetchingStatusService().execute();
    }
    private class fetchingStatusService extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                for(int i = 0 ; i < Conf.CollaborateDevices.size() ; i++){
                    String response = "";
                    try {
                        response = fetchHttp(Conf.CollaborateDevices.get(i)+":8081");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    List<String> items = Arrays.asList(response.split("\\s*,\\s*"));
                    if(items.size() > 1){
                        CollaborateDevice device = new CollaborateDevice(Conf.CollaborateDevices.get(i));
                        device.RAM = Integer.parseInt(items.get(0));
                        device.Battery = Integer.parseInt(items.get(1));
                        device.CPU = Integer.parseInt(items.get(2));
                        device.Bandwidth = Integer.parseInt(items.get(3));
                        totalRAM += device.RAM;
                        totalBattery += device.Battery;
                        totalCPU += device.CPU;
                        totalBandwidth += device.Bandwidth;
                        Log.e(TAG,"Setting the realtime speic");
                        Conf.AvailableDevices.set(i,device);
                    }
                }
            }catch (Exception e){

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            WeightNormalize();
        }
    }
    private void ResetValue(){
        totalBattery = 0;
        totalCPU =0 ;
        totalRAM = 0;
        Conf.AvailableDevices.clear();
    }
    private void WeightNormalize(){
        float normalizeCPU = 0;
        for(CollaborateDevice device : Conf.AvailableDevices){
            if(totalCPU !=0) {
                 normalizeCPU = (float) device.CPU / (float) totalCPU;
            }
            float normalizeBattery = (float) device.Battery / (float) totalBattery;
            float normalizeRAM = (float) device.RAM / (float) totalRAM;
            float normalizeBandwidth = (float) device.Bandwidth / (float) totalBandwidth;
            float weight = normalizeCPU + 3 * normalizeBattery + 55 * normalizeRAM + normalizeBandwidth;
            Log.e(TAG, "DEVICE WEIGHT IS " + weight);
            if(weight == 0){
                device.weight = randInt(3,6) * 10;
            }else {
                device.weight = (int) (weight * 100);
            }
        }
    }

    public void PartitionEqual(){
        for(CollaborateDevice device : Conf.AvailableDevices){
            device.weight = 50;
        }
    }

    public void PartitionNotEqual(){
        hightWeight = randInt(6,8) * 10;
        Log.e("TAG","SET RANDOM weight " + hightWeight);
//        if(Conf.AvailableDevices.size() >0){
//            int size = Conf.AvailableDevices.size();
//            for(int i=0; i < size ; i ++){
//                Conf.AvailableDevices.get(i).weight = randInt(6,8) * 10;
//            }
//        }
        Conf.AvailableDevices.get(0).weight =hightWeight;
        Conf.AvailableDevices.get(1).weight = 100 - hightWeight;
        hightWeight = randInt(6,8) * 10;
        Conf.AvailableDevices.get(2).weight = hightWeight;


        Log.e(TAG, "DEVICE WEIGHT IS " + Conf.AvailableDevices.get(0).weight);
        Log.e(TAG, "DEVICE WEIGHT IS " + Conf.AvailableDevices.get(1).weight);
        Log.e(TAG, "DEVICE WEIGHT IS " + Conf.AvailableDevices.get(2).weight);
    }


    private String fetchHttp(String uri) throws IOException {
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = httpclient.execute(new HttpGet(uri));
        StatusLine statusLine = response.getStatusLine();
        if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            response.getEntity().writeTo(out);
            out.close();
            return out.toString();
        } else {
            // Closes the connection.
            response.getEntity().getContent().close();
            throw new IOException(statusLine.getReasonPhrase());
        }
    }

    public static int randInt(int min, int max) {

        // NOTE: Usually this should be a field rather than a method
        // variable so that it is not re-seeded every call.
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }
}
