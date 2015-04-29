package ee.ut.cs.thesisworkflow.workflow;

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
import java.util.ArrayList;
import java.util.Map;

import ee.ut.cs.thesisworkflow.Data.Conf;
import ee.ut.cs.thesisworkflow.object.PartnerLink;
import ee.ut.cs.thesisworkflow.object.WorkFlowActivity;
import ee.ut.cs.thesisworkflow.object.WorkFlowProcess;
import ee.ut.cs.thesisworkflow.object.WorkFlowVariable;

/**
 * Created by weiding on 07/04/15.
 */
public class WorkFlowDecisionMaker {

    private Map<String, ArrayList<String>> graphMap;
    private Map<String, ArrayList<String>> graphMapBackword;
    private Map<String, WorkFlowActivity> activityMap;
    private ArrayList<WorkFlowVariable> variables;
    private ArrayList<PartnerLink> partnerLinks;
    private WorkFlowGenerate generate;
    private static final String TAG = "WorkFlowDecisionMaker";
    private WorkFlowCollaborate workFlowCollaborate;
    InputStream inputStream;
    FIS fis;

    public WorkFlowDecisionMaker(WorkFlowProcess workflowProcess) {
        graphMap = workflowProcess.graphMap;
        graphMapBackword = workflowProcess.graphMapBackword;
        activityMap = workflowProcess.activityMap;
        variables = workflowProcess.variables;
        partnerLinks = workflowProcess.partnerLinks;
        generate = new WorkFlowGenerate(workflowProcess);
        try {
            inputStream = MyApplication.getAppContext().getResources().getAssets().open("offloading.fcl");
        } catch (IOException e) {
            e.printStackTrace();
        }
        fis = FIS.load(inputStream, true);
        workFlowCollaborate = new WorkFlowCollaborate();
    }


    public void MakeDecision(String decisionPoint) {
        //TODO just for testing need to delete it afterwards
        workFlowCollaborate.GetCollaborateDevicesStatus();
        if (IsOffloadingParalleTask(decisionPoint)) {
            if (IsOffloading()) {
                int totalWeight = 0;
                for (int i = 0; i < Conf.AvailableDevices.size(); i++) {
                    totalWeight += Conf.AvailableDevices.get(i).weight;
                }
                Log.e(TAG, "total weight" + totalWeight);

                int partitionSize = graphMap.get(decisionPoint).size();
                Log.e(TAG, "partition size" + partitionSize);
                int position = 0;
                for (int i = 0; i < Conf.AvailableDevices.size(); i++) {
                    Conf.AvailableDevices.get(i).startPosition = position;
                    Log.e(TAG, "element " + i + "start : " + position);
                    position = position + (Conf.AvailableDevices.get(i).weight * partitionSize) / totalWeight;
                    if (i == Conf.AvailableDevices.size() - 1) {
                        Conf.AvailableDevices.get(i).endPosition = partitionSize;
                    } else {
                        Conf.AvailableDevices.get(i).endPosition = position;
                    }
                    Log.e(TAG, "element " + i + "end : " + Conf.AvailableDevices.get(i).endPosition);
                }
                Log.e(TAG, "start task " + decisionPoint);
                Log.e(TAG, "end task " + FindFlowJointActivty(decisionPoint));

                try {
                    if (GetParalleSubTasks(decisionPoint) > 9) {
                        Log.e(TAG, "OFFLOADING");
                        generate.OffloadingParallelTask(decisionPoint, FindFlowJointActivty(decisionPoint), Conf.AvailableDevices);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } else {
            //TODO IS SEQUENCE TASK NOT OFF LOADING, THE COMMENT CODE IS HOW TO OFFLOADING SEQUENCE TASK
//            StringWriter stringWriterSequence = null;
//            try {
//                stringWriterSequence = generate.OffLoadingSequenceTask(decisionPoint, "ending");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            generate.ModifyBpelMap(decisionPoint, "ending", stringWriterSequence);
        }
    }

    private boolean IsOffloading() {
        if (fis == null) {
            Log.e(TAG, "FAILED TO OPEN");
        }
        double defuzzifiedValue;

        fis.setVariable("CPU", getCPUUsage());
        fis.setVariable("BATTERY", getBatteryUsage());
        fis.setVariable("RAM", getMemoryUsage());
        fis.setVariable("BANDWIDTH", getBandwidthUsage());
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
        float percentage = (readBatteryStatus() * 100);
        Log.e(TAG, "read battery usage" + (int) percentage);
        return (int) percentage;
    }

    private int getMemoryUsage() {
        float percentage = (readMemoryStatus() * 100);
        Log.e(TAG, "read memory usage" + (int) percentage);
        return (int) percentage;
    }

    private int getCPUUsage() {
        float percentage = (readCPUStatus() * 100);
        Log.e(TAG, "read CPU usage" + (int) percentage);
        return (int) percentage;
    }

    private float readMemoryStatus() {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) MyApplication.getAppContext().getSystemService(MyApplication.getAppContext().ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        return (float) (mi.totalMem - mi.availMem) / mi.totalMem;
    }

    private float getBandwidthUsage(){
        return 3;
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
                    .getConstructor(Context.class).newInstance(this);
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

    private String FindFlowJointActivty(String activityName) {
        String previousActivity = null;
        while (graphMapBackword.get(activityName).size() == 1) {
            previousActivity = activityName;
            activityName = graphMap.get(activityName).get(0);
        }
        return activityName;
    }

    private boolean IsOffloadingParalleTask(String activityName) {
        if (graphMap.get(activityName).size() > 1) {
            return true;
        } else
            return false;
    }

    private int GetParalleSubTasks(String activityName) {
        return graphMap.get(activityName).size();
    }
}
