package ee.ut.cs.thesisworkflow.workflow;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

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

    private ArrayList<String> avaiableExternalIPs = new ArrayList<>(Arrays.asList("http://192.168.0.102:8080,http://192.168.0.103:8080"));

    public WorkFlowDecisionMaker(WorkFlowProcess workflowProcess) {
        graphMap = workflowProcess.graphMap;
        graphMapBackword = workflowProcess.graphMapBackword;
        activityMap = workflowProcess.activityMap;
        variables = workflowProcess.variables;
        partnerLinks = workflowProcess.partnerLinks;
        generate = new WorkFlowGenerate(workflowProcess);
    }


    public void MakeDecision(String decisionPoint){
        if(IsOffloadingParalleTask(decisionPoint)){


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


    private boolean IsOffloadingParalleTask(String activityName) {
        if (graphMap.get(activityName).size() > 1) {
            return true;
        } else
            return false;
    }
}
