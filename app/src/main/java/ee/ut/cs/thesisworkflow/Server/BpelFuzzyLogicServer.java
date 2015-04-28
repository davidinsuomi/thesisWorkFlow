package ee.ut.cs.thesisworkflow.Server;

/**
 * Created by weiding on 27/04/15.
 */
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

import net.sourceforge.jFuzzyLogic.FIS;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import NanoHTTPD.NanoHTTPD;
import ee.ut.cs.thesisworkflow.workflow.MyApplication;

public class BpelFuzzyLogicServer extends NanoHTTPD {
    private static String TAG = "BpelFuzzyLogicServer";
    private float CPUSpec =  2500f;
    InputStream inputStream;
    FIS fis;
    int availableRAM;
    int availableCPU;
    int availableBattery;


    public BpelFuzzyLogicServer(){
        super(8081);
        try {
            inputStream = MyApplication.getAppContext().getResources().getAssets().open("offloading.fcl");
        } catch (IOException e) {
            e.printStackTrace();
        }
        fis = FIS.load(inputStream, true);
    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        String uri = session.getUri();
        Log.e(TAG, "URI:" + uri);

        String msg= "";
        if(method.toString().equals("GET")) {
            if(IsOffloading()){
                msg = "0";
            }
            else{
                msg = availableRAM + "," + availableBattery + "," + availableCPU;
            }
            Log.e(TAG,"response: " + msg);
        }

        return new Response(msg);
    }

    private boolean IsOffloading() {
        if (fis == null) {
            Log.e(TAG, "FAILED TO OPEN");
        }
        double defuzzifiedValue;

        fis.setVariable("CPU", getCPUUsage());
        fis.setVariable("BATTERY", getBatteryUsage());
        fis.setVariable("RAM", getMemoryUsage());
        fis.evaluate();

        Log.e(TAG, "" + fis.getVariable("decision").getLatestDefuzzifiedValue());
        defuzzifiedValue = fis.getVariable("decision").getLatestDefuzzifiedValue();
        if (defuzzifiedValue > 10) {
            return false;
        } else {
            return true;
        }
    }



    private int getBatteryUsage() {
        float percentage = (readBatteryStatus());
        Log.e(TAG, "read battery usage" + (int) percentage);
        availableBattery = (int) (percentage * readBatteryCapacity());
        return (int) (percentage * 100);
    }

    private int getMemoryUsage() {
        float percentage = (readMemoryStatus() * 100);
        Log.e(TAG, "read memory usage" + (int) percentage);
        return (int) percentage;
    }

    private int getCPUUsage() {
        float percentage = (readCPUStatus());
        Log.e(TAG, "read CPU usage" + (int) percentage);
        availableCPU = (int) (CPUSpec * percentage);
        return (int) (percentage * 100);
    }

    private float readMemoryStatus() {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) MyApplication.getAppContext().getSystemService(MyApplication.getAppContext().ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        availableRAM = (int) (mi.availMem / 1048576L);
        return (float) (mi.totalMem - mi.availMem) / mi.totalMem;
    }

    private float readBatteryStatus() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = MyApplication.getAppContext().getApplicationContext().registerReceiver(null, ifilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        Object mPowerProfile_ = null;

        final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";

        try {
            mPowerProfile_ = Class.forName(POWER_PROFILE_CLASS)
                    .getConstructor(Context.class).newInstance(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            double batteryCapacity = (Double) Class
                    .forName(POWER_PROFILE_CLASS)
                    .getMethod("getAveragePower", java.lang.String.class)
                    .invoke(mPowerProfile_, "battery.capacity");
            Log.e(TAG, batteryCapacity + " mah");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return level / (float) scale;

    }

    private float readBatteryCapacity() {
        Object mPowerProfile_ = null;

        final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";
        double batteryCapacity = 0;
        try {
            mPowerProfile_ = Class.forName(POWER_PROFILE_CLASS)
                    .getConstructor(Context.class).newInstance(MyApplication.getAppContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {

            batteryCapacity = (Double) Class
                    .forName(POWER_PROFILE_CLASS)
                    .getMethod("getAveragePower", java.lang.String.class)
                    .invoke(mPowerProfile_, "battery.capacity");
            Log.e(TAG, batteryCapacity + " mah");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (float) batteryCapacity;
    }

    private float readCPUStatus() {
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
            String load = reader.readLine();

            String[] toks = load.split(" +");  // Split on one or more spaces

            long idle1 = Long.parseLong(toks[4]);
            long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            try {
                Thread.sleep(360);
            } catch (Exception e) {
            }

            reader.seek(0);
            load = reader.readLine();
            reader.close();

            toks = load.split(" +");

            long idle2 = Long.parseLong(toks[4]);
            long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            return (float) (cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return 0;
    }
}
