package ee.ut.cs.thesisworkflow.workflow;

import android.app.Activity;
import android.os.Bundle;

import java.io.IOException;

import ee.ut.cs.thesisworkflow.Server.BpelHttpServer;

/**
 * Created by weiding on 05/04/15.
 */
public class BpelServiceActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BpelHttpServer server = new BpelHttpServer();

        try{
            server.start();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
