package ee.ut.cs.thesisworkflow.workflow;

import android.app.Application;
import android.content.Context;

/**
 * Created by weiding on 27/04/15.
 */
public class MyApplication extends Application {

    private static Context context;

    public void onCreate(){
        super.onCreate();
        MyApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }
}
