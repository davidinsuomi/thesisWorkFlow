package ee.ut.cs.thesisworkflow.workflow;

import android.os.AsyncTask;

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

import ee.ut.cs.thesisworkflow.Data.Conf;
import ee.ut.cs.thesisworkflow.Data.ExternalIP;

/**
 * Created by weiding on 27/04/15.
 */
public class WorkFlowCollaborate {
    int totalCPU;
    int totalBattery;
    int totalRAM;

    public void GetCollaborateDevicesStatus(){
        ResetValue();
        new fetchingStatusService().execute();
    }
    private class fetchingStatusService extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            try {
                for(int i = 0 ; i < Conf.CollaborateDeviceIPs.size() ; i++){
                    String response = "";
                    try {
                        response = fetchHttp(Conf.CollaborateDeviceIPs.get(i)+":8081");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    List<String> items = Arrays.asList(response.split("\\s*,\\s*"));
                    if(items.size() > 1){
                        ExternalIP device = new ExternalIP(Conf.CollaborateDeviceIPs.get(i));
                        device.RAM = Integer.parseInt(items.get(0));
                        device.Battery = Integer.parseInt(items.get(1));
                        device.CPU = Integer.parseInt(items.get(2));
                        totalRAM += device.RAM;
                        totalBattery += device.Battery;
                        totalCPU += device.CPU;
                        Conf.IPs.add(device);

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
        Conf.IPs.clear();
    }
    private void WeightNormalize(){
        for(ExternalIP device : Conf.IPs){
            float normalizeCPU = (float) device.CPU / (float) totalCPU;
            float normalizeBattery = (float) device.Battery / (float) totalBattery;
            float normalizeRAM = (float) device.RAM / (float) totalRAM;
            float weight = normalizeCPU + normalizeBattery + normalizeRAM;
            device.weight = (int) (weight * 100);
        }
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
}
