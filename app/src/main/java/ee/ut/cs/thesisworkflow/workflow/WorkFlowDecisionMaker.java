package ee.ut.cs.thesisworkflow.workflow;

import android.util.Log;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
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

    public WorkFlowDecisionMaker(WorkFlowProcess workflowProcess) {
        graphMap = workflowProcess.graphMap;
        graphMapBackword = workflowProcess.graphMapBackword;
        activityMap = workflowProcess.activityMap;
        variables = workflowProcess.variables;
        partnerLinks = workflowProcess.partnerLinks;
        generate = new WorkFlowGenerate(workflowProcess);
    }


    public void MakeDecision(String decisionPoint)  {
        if(IsOffloadingParalleTask(decisionPoint)){
            int totalWeight= 0;
            for(int i = 0 ; i < Conf.IPs.size() ; i ++){
                totalWeight += Conf.IPs.get(i).weight;
            }
            Log.e(TAG,"total weight"  + totalWeight);

            int partitionSize = graphMap.get(decisionPoint).size();
            Log.e(TAG,"partition size"  + partitionSize);
            int position = 0;
            for(int i =0; i < Conf.IPs.size() ; i ++){
                Conf.IPs.get(i).startPosition = position;
                Log.e(TAG,"element " + i + "start : "   + position);
                position = position + (Conf.IPs.get(i).weight * partitionSize) / totalWeight;
                if(i == Conf.IPs.size() -1){
                    Conf.IPs.get(i).endPosition = partitionSize;
                }else {
                    Conf.IPs.get(i).endPosition =  position;
                }
                Log.e(TAG,"element " + i + "end : "   + Conf.IPs.get(i).endPosition);
            }
            Log.e(TAG,"start task "  + decisionPoint);
            Log.e(TAG,"end task "  +FindFlowJointActivty(decisionPoint) );

            try {
                generate.OffloadingParallelTask(decisionPoint,FindFlowJointActivty(decisionPoint),Conf.IPs);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }else{
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
}
