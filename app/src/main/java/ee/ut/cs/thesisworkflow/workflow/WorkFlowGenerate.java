package ee.ut.cs.thesisworkflow.workflow;

/**
 * Created by weiding on 16/02/15.
 */

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ee.ut.cs.thesisworkflow.Data.ExternalIP;
import ee.ut.cs.thesisworkflow.object.PartnerLink;
import ee.ut.cs.thesisworkflow.object.WorkFlowActivity;
import ee.ut.cs.thesisworkflow.object.WorkFlowAssign;
import ee.ut.cs.thesisworkflow.object.WorkFlowInvoke;
import ee.ut.cs.thesisworkflow.object.WorkFlowProcess;
import ee.ut.cs.thesisworkflow.object.WorkFlowVariable;

public class WorkFlowGenerate {
    private static StringWriter writer;
    private static String TAG = "GENERATEXML";
    private Map<String, ArrayList<String>> graphMap;
    private Map<String, ArrayList<String>> graphMapBackword;
    private Map<String, WorkFlowActivity> activityMap;
    private ArrayList<WorkFlowVariable> variables;
    private ArrayList<PartnerLink> partnerLinks;
    private Map<String, WorkFlowVariable> offloadingVariables = new HashMap<String, WorkFlowVariable>();
    private Map<String, PartnerLink> offloadingPartnerLinks = new HashMap<String, PartnerLink>();
    private XmlSerializer xmlSerializer = Xml.newSerializer();

    public WorkFlowGenerate(WorkFlowProcess workflowProcess) {
        graphMap = workflowProcess.graphMap;
        graphMapBackword = workflowProcess.graphMapBackword;
        activityMap = workflowProcess.activityMap;
        variables = workflowProcess.variables;
        partnerLinks = workflowProcess.partnerLinks;
    }



    private void InitializeXmlSerializer() throws IllegalArgumentException, IllegalStateException, IOException {
        writer = new StringWriter();
        xmlSerializer.setOutput(writer);
        //Start Document
        xmlSerializer.startDocument("UTF-8", true);
        xmlSerializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
        //Open Tag <file>
        xmlSerializer.startTag("", "process");
        //TODO create partnerLinks
        xmlSerializer.startTag("", "partnerLinks");
        for (Object value : offloadingPartnerLinks.values()) {
            PartnerLink partnerLink = (PartnerLink) value;
            CreatePartnerLink(partnerLink);
        }
        xmlSerializer.endTag("", "partnerLinks");
        //TODO create variables
        xmlSerializer.startTag("", "variables");
        for (Object value : offloadingVariables.values()) {
            WorkFlowVariable workFlowVariable = (WorkFlowVariable) value;
            CreateVariable(workFlowVariable);
        }
        xmlSerializer.endTag("", "variables");

    }

    private void FinalizeXmlSerializer() throws IllegalArgumentException, IllegalStateException, IOException {
        xmlSerializer.endDocument();

    }

    private void FindNewOffloadingVariablesAndPartnerLinkSequence(String startTask, String endTask) {
        if (startTask.equals(endTask)) {
            FindCurrentTaskVariableAndPartnerLink(startTask);
        } else {
            while (!startTask.equals(graphMap.get(endTask).get(0))) {
                FindCurrentTaskVariableAndPartnerLink(startTask);
                int nextActivities = graphMap.get(startTask).size();
                if (nextActivities > 1) {
                    String endFlowActivity = null;
                    String startActivityInsideFlow = null;
                    for (int i = 0; i < nextActivities; i++) {
                        startActivityInsideFlow = graphMap.get(startTask).get(i);
                        endFlowActivity = FindFlowEndActivty(startActivityInsideFlow);
                        FindNewOffloadingVariablesAndPartnerLinkSequence(startActivityInsideFlow, endFlowActivity);
                    }
                    startTask = graphMap.get(endFlowActivity).get(0);
                } else {
                    startTask = graphMap.get(startTask).get(0);
                }
            }
        }
    }

    private void FindNewOffloadingVariablesAndPartnerParallel(String startTask, ExternalIP externalIP) {
        FindCurrentTaskVariableAndPartnerLink(startTask);
        String endFlowActivity = null;
        String startActivityInsideFlow = null;
        for (int i = externalIP.startPosition; i < externalIP.endPosition; i++) {
            startActivityInsideFlow = graphMap.get(startTask).get(i);
            endFlowActivity = FindFlowEndActivty(startActivityInsideFlow);
            FindNewOffloadingVariablesAndPartnerLinkSequence(startActivityInsideFlow, endFlowActivity);
        }
    }


    private void FindCurrentTaskVariableAndPartnerLink(String currentTag) {
        WorkFlowActivity activity = activityMap.get(currentTag);
        if (activity instanceof WorkFlowInvoke) {
            WorkFlowInvoke invoke = (WorkFlowInvoke) activity;
            for (WorkFlowVariable variable : variables) {
                if (variable.name.equals(invoke.inputVariable)) {
                    if (!offloadingVariables.containsKey(variable.name)) {
                        offloadingVariables.put(variable.name, variable);
                    }

                } else if (variable.name.equals(invoke.outputVariable)) {
                    if (!offloadingVariables.containsKey(variable.name)) {
                        offloadingVariables.put(variable.name, variable);
                    }
                }
            }

            for (PartnerLink partnerLink : partnerLinks) {
                if (partnerLink.name.equals(invoke.partnerLink)) {
                    if (!offloadingPartnerLinks.containsKey(partnerLink.name)) {
                        offloadingPartnerLinks.put(partnerLink.name, partnerLink);
                    }
                }
            }
        } else if (activity instanceof WorkFlowAssign) {
            WorkFlowAssign assign = (WorkFlowAssign) activity;
            for (WorkFlowVariable variable : variables) {
                if (variable.name.equals(assign.from)) {
                    if (!offloadingVariables.containsKey(variable.name)) {
                        offloadingVariables.put(variable.name, variable);
                    }

                } else if (variable.name.equals(assign.to)) {
                    if (!offloadingVariables.containsKey(variable.name)) {
                        offloadingVariables.put(variable.name, variable);
                    }
                }
            }
        }
    }


//    public StringWriter OffloadingTask(String startTask, String endTask) throws IOException {
//        if (IsOffloadingParalleTask(startTask, endTask)) {
//            return OffloadingParallelTask(startTask, endTask, 30);
//        } else {
//            StringWriter stringWriterSequence =  OffLoadingSequenceTask(startTask, endTask);
//            ModifyBpelMap(startTask,endTask,stringWriterSequence);
//            longInfo(stringWriterSequence.toString());
//            return stringWriterSequence;
//        }
//    }

    public void ModifyBpelMap(String startTask, String endTask,StringWriter writer){
        //Create offloading input variable
        WorkFlowVariable offloadingInput = new WorkFlowVariable("offloadingInput","tns:String");
        WorkFlowVariable offloadingOutput = new WorkFlowVariable("offloadingOutput","tns:String");
        offloadingInput.SetData(writer.toString());

        variables.add(offloadingInput);
        variables.add(offloadingOutput);
        //Create Offloading partnerlink

        PartnerLink offLoadingPartnerLink = new PartnerLink("offLoadingPartnerLink","tns:PostData","","http://192.168.0.102:8080");
        partnerLinks.add(offLoadingPartnerLink);

        WorkFlowInvoke invoke = new WorkFlowInvoke("InvokeOffloading",offLoadingPartnerLink.name,"POST",offloadingInput.name,offloadingOutput.name);
        activityMap.put(invoke.name,invoke);
        //update grapmap the next task is new invoke
        ArrayList<String> newArrayList = new ArrayList<>();
        newArrayList.add(invoke.name);
        graphMap.put(startTask,newArrayList);

        //update next inovke activity to endtask
        ArrayList<String> newArrayList2 = new ArrayList<>();
        newArrayList2.add(endTask);
        graphMap.put(invoke.name,newArrayList2);

        graphMapBackword.put(endTask,newArrayList);
    }

    public void longInfo(String str) {
        if (str.length() > 4000) {
            Log.i(TAG, str.substring(0, 4000));
            longInfo(str.substring(4000));
        } else
            Log.i(TAG, str);
    }


    public StringWriter OffLoadingSequenceTask(String startTask, String endTask) throws IllegalArgumentException, IllegalStateException, IOException {

        // offloading sequence task
        startTask = graphMap.get(startTask).get(0);
        endTask = graphMapBackword.get(endTask).get(0);
        FindNewOffloadingVariablesAndPartnerLinkSequence(startTask, endTask);
        InitializeXmlSerializer();
        TaskToBeOffloadingSequence(startTask, endTask);
        xmlSerializer.endTag("", "process");
        FinalizeXmlSerializer();
        return writer;
    }

    //So far only to able to offloading sequenceTask
    public void TaskToBeOffloadingSequence(String startTask, String endTask) throws IllegalArgumentException, IllegalStateException, IOException {
        if (startTask.equals(endTask)) {
            //only one task need to offloading
            xmlSerializer.startTag("", "sequence");
            CreateCurrentXMLTag(startTask);
            xmlSerializer.endTag("", "sequence");
            xmlSerializer.endTag("", "process");
        } else {
            xmlSerializer.startTag("", "sequence");
            while (!startTask.equals(graphMap.get(endTask).get(0))) {
                CreateCurrentXMLTag(startTask);
//                startTask = graphMap.get(startTask).get(0);
                int nextActivities = graphMap.get(startTask).size();
                if (nextActivities > 1) {
                    String endFlowActivity = null;
                    String startActivityInsideFlow = null;
                    xmlSerializer.startTag("", "flow");
                    for (int i = 0; i < nextActivities; i++) {
                        startActivityInsideFlow = graphMap.get(startTask).get(i);
                        endFlowActivity = FindFlowEndActivty(startActivityInsideFlow);
                        TaskToBeOffloadingSequence(startActivityInsideFlow, endFlowActivity);

                    }
                    xmlSerializer.endTag("", "flow");
                    startTask = graphMap.get(endFlowActivity).get(0);
                } else {
                    startTask = graphMap.get(startTask).get(0);
                }

            }
            xmlSerializer.endTag("", "sequence");
        }
    }

//
//    public StringWriter OffloadingParallelTask(String startTask, String endTask, int subtasks) throws IllegalArgumentException, IllegalStateException, IOException {
//
//        // offloading sequence task
////        startTask = graphMap.get(startTask).get(0);
//        endTask = graphMapBackword.get(endTask).get(0);
//        AddDummyInovekeVariableForParallelTask();
//        FindNewOffloadingVariablesAndPartnerLink(startTask, endTask);
//        InitializeXmlSerializer();
//        TaskToBeOffloadingParallel(startTask, endTask, subtasks);
//        xmlSerializer.endTag("", "process");
//        FinalizeXmlSerializer();
//        return writer;
//    }



    public void  OffloadingParallelTask(String startTask, String endTask, List<ExternalIP> IPs ) throws IOException {

        ArrayList<String> inputVariables = new ArrayList<>();
        for(int i = 0 ; i < IPs.size() ; i ++){
            String flowEndTask = graphMapBackword.get(endTask).get(0);
            AddDummyInovekeVariableForParallelTask();
            //TODO need to remove unnecessasry varaiable and partnerLinks
            FindNewOffloadingVariablesAndPartnerParallel(startTask, IPs.get(i));
            InitializeXmlSerializer();
            TaskToBeOffloadingParallel(startTask, flowEndTask, IPs.get(i));
            xmlSerializer.endTag("", "process");
            FinalizeXmlSerializer();
            inputVariables.add(writer.toString());
        }

        ModifyBpelParallel(startTask,endTask,IPs,inputVariables);
    }

    public void ModifyBpelParallel(String startTask, String endTask, List<ExternalIP> IPs,ArrayList<String> inputVariable){
        ArrayList<String> invokes = new ArrayList<>();
        for(int i = 0 ; i < IPs.size() ; i++){
            WorkFlowVariable offloadingInput = new WorkFlowVariable("offloadingInput" + i,"tns:String");
            WorkFlowVariable offloadingOutput = new WorkFlowVariable("offloadingOutput" + i,"tns:String");
            offloadingInput.SetData(inputVariable.get(i));

            variables.add(offloadingInput);
            variables.add(offloadingOutput);


            PartnerLink offLoadingPartnerLink = new PartnerLink("offLoadingPartnerLink" + i ,"tns:PostData","",IPs.get(0).IP);
            partnerLinks.add(offLoadingPartnerLink);


            WorkFlowInvoke invoke = new WorkFlowInvoke("InvokeOffloading" + i ,offLoadingPartnerLink.name,"POST",offloadingInput.name,offloadingOutput.name);
            activityMap.put(invoke.name,invoke);
            invokes.add(invoke.name);
            // next activity joint activity
            ArrayList<String> jointActivity = new ArrayList<>();
            jointActivity.add(endTask);
            graphMap.put(invoke.name,jointActivity);
        }

        graphMap.put(startTask,invokes);
        graphMapBackword.put(endTask,invokes);
    }

    private void AddDummyInovekeVariableForParallelTask() {
        WorkFlowVariable dummyAssign1 = new WorkFlowVariable("dummyAssign1", "tns:String");
        WorkFlowVariable dummyAssign2 = new WorkFlowVariable("dummyAssign2", "tns:String");
        offloadingVariables.put("dummyAssign1", dummyAssign1);
        offloadingVariables.put("dummyAssign2", dummyAssign2);
    }

//    //So far only to able to offloading sequenceTask
//    public void TaskToBeOffloadingParallel(String startTask, String endTask, int subtasks) throws IllegalArgumentException, IllegalStateException, IOException {
//        xmlSerializer.startTag("", "sequence");
//        //TODO Need to change first task to empty
//        CreateStartingBpelActivity();
//        String endFlowActivity = null;
//        String startActivityInsideFlow = null;
//        xmlSerializer.startTag("", "flow");
//        for (int i = 0; i < subtasks; i++) {
//            startActivityInsideFlow = graphMap.get(startTask).get(i);
//            endFlowActivity = FindFlowEndActivty(startActivityInsideFlow);
//            TaskToBeOffloadingSequence(startActivityInsideFlow, endFlowActivity);
//
//        }
//        xmlSerializer.endTag("", "flow");
//        xmlSerializer.endTag("", "sequence");
//    }

    public void TaskToBeOffloadingParallel(String startTask, String endTask, ExternalIP externalIP) throws IllegalArgumentException, IllegalStateException, IOException {
        xmlSerializer.startTag("", "sequence");
        CreateStartingBpelActivity();
        String endFlowActivity = null;
        String startActivityInsideFlow = null;
        xmlSerializer.startTag("", "flow");
        for (int i = externalIP.startPosition; i < externalIP.endPosition; i++) {
            startActivityInsideFlow = graphMap.get(startTask).get(i);
            endFlowActivity = FindFlowEndActivty(startActivityInsideFlow);
            TaskToBeOffloadingSequence(startActivityInsideFlow, endFlowActivity);
        }
        xmlSerializer.endTag("", "flow");
        xmlSerializer.endTag("", "sequence");
    }

    private void CreateStartingBpelActivity() throws IOException {
        xmlSerializer.startTag("", "assign");
        xmlSerializer.attribute("", "name", "entryPoint");
        xmlSerializer.startTag("", "copy");
        xmlSerializer.startTag("", "from");
        xmlSerializer.attribute("", "variable", "dummyAssign1");
        xmlSerializer.endTag("", "from");
        xmlSerializer.startTag("", "to");
        xmlSerializer.attribute("", "variable", "dummyAssign2");
        xmlSerializer.endTag("", "to");
        xmlSerializer.endTag("", "copy");
        xmlSerializer.endTag("", "assign");
    }

    private String FindFlowEndActivty(String activityName) {
        String previousActivity = null;
        while (graphMapBackword.get(activityName).size() == 1) {
            previousActivity = activityName;
            activityName = graphMap.get(activityName).get(0);
        }
        return previousActivity;
    }

    private void CreateCurrentXMLTag(String currentTag) throws IllegalArgumentException, IllegalStateException, IOException {
        WorkFlowActivity activity = activityMap.get(currentTag);
        if (activity instanceof WorkFlowInvoke) {
            WorkFlowInvoke invoke = (WorkFlowInvoke) activity;
            CreateInvoke(invoke);
        } else if (activity instanceof WorkFlowAssign) {
            WorkFlowAssign assign = (WorkFlowAssign) activity;
            CreateAssign(assign);
        }
    }

    private void CreateVariable(WorkFlowVariable workFlowVariable) throws IllegalArgumentException, IllegalStateException, IOException {
        xmlSerializer.startTag("", "variable");
        if (workFlowVariable.messageType != null) {
            xmlSerializer.attribute("", "messageType", workFlowVariable.messageType);
        }
        xmlSerializer.attribute("", "name", workFlowVariable.name);
        CreateVariableValue(workFlowVariable);
        xmlSerializer.endTag("", "variable");
    }

    private void CreateVariableValue(WorkFlowVariable workFlowVariable) throws IOException {
        if (workFlowVariable.HasValue()) {
            if (workFlowVariable.IsList()) {
                String output = "";
                for (String data : workFlowVariable.datas) {
                    output += data + ",";
                }
                //remove last comma in the string
                output = output.substring(0, output.length() - 1);
                xmlSerializer.text(output);
            } else {
                xmlSerializer.text(workFlowVariable.GetData());
            }
        }
    }

    private void CreatePartnerLink(PartnerLink partnerLink) throws IllegalArgumentException, IllegalStateException, IOException {
        xmlSerializer.startTag("", "partnerLink");
        if (partnerLink.myRole != null) {
            xmlSerializer.attribute("", "myRole", partnerLink.myRole);
        }
        if (partnerLink.partnerLinkType != null) {
            xmlSerializer.attribute("", "partnerLinkType", partnerLink.partnerLinkType);
        }
        if (partnerLink.name != null) {
            xmlSerializer.attribute("", "name", partnerLink.name);
        }
        if (partnerLink.URL != null) {
            xmlSerializer.text(partnerLink.URL);
        }
        xmlSerializer.endTag("", "partnerLink");
    }

    private void CreateAssign(WorkFlowAssign workFlowAssign) throws IllegalArgumentException, IllegalStateException, IOException {
        xmlSerializer.startTag("", "assign");
        xmlSerializer.attribute("", "name", workFlowAssign.name);
        xmlSerializer.startTag("", "copy");
        xmlSerializer.startTag("", "from");
        xmlSerializer.attribute("", "variable", workFlowAssign.from);
        xmlSerializer.endTag("", "from");
        xmlSerializer.startTag("", "to");
        xmlSerializer.attribute("", "variable", workFlowAssign.to);
        xmlSerializer.endTag("", "to");
        xmlSerializer.endTag("", "copy");
        xmlSerializer.endTag("", "assign");
    }

    private void CreateInvoke(WorkFlowInvoke workFlowInvoke) throws IllegalArgumentException, IllegalStateException, IOException {
        xmlSerializer.startTag("", "invoke");
        xmlSerializer.attribute("", "name", workFlowInvoke.name);
        if (workFlowInvoke.partnerLink != null) {
            xmlSerializer.attribute("", "partnerLink", workFlowInvoke.partnerLink);
        }
        if (workFlowInvoke.operation != null) {
            xmlSerializer.attribute("", "operation", workFlowInvoke.operation);
        }
        if (workFlowInvoke.inputVariable != null) {
            xmlSerializer.attribute("", "inputVariable", workFlowInvoke.inputVariable);
        }
        if (workFlowInvoke.outputVariable != null) {
            xmlSerializer.attribute("", "outputVariable", workFlowInvoke.outputVariable);
        }
        xmlSerializer.endTag("", "invoke");
    }

    public static WorkFlowGenerate testWorkFlowInstance() {
        WorkFlowProcess process = new WorkFlowProcess();
        ArrayList<PartnerLink> partnerLinks = new ArrayList<PartnerLink>();
        ArrayList<WorkFlowVariable> variables = new ArrayList<WorkFlowVariable>();
        Map<String, ArrayList<String>> graphMap = new HashMap<String, ArrayList<String>>();
        Map<String, ArrayList<String>> graphMapBackword = new HashMap<String, ArrayList<String>>();
        Map<String, WorkFlowActivity> activityMap = new HashMap<String, WorkFlowActivity>();
        // logic to fill all the variable

        WorkFlowVariable variable1 = new WorkFlowVariable("wsdlResponse1", "tns:GetFileResponseMessage");
        WorkFlowVariable variable2 = new WorkFlowVariable("wsdlResponse2", "tns:GetFileResponseMessage");
        WorkFlowVariable variable3 = new WorkFlowVariable("wsdlResponse3", "tns:GetFileResponseMessage");
        WorkFlowVariable variable4 = new WorkFlowVariable("wsdlResponse4", "tns:GetFileResponseMessage");
        WorkFlowVariable variable5 = new WorkFlowVariable("wsdlResponse5", "tns:GetFileResponseMessage");
        WorkFlowVariable variable6 = new WorkFlowVariable("WSDLProcessorResponse1", "tns:GetFileResponseMessage");
        WorkFlowVariable variable7 = new WorkFlowVariable("WSDLProcessorResponse2", "tns:GetFileResponseMessage");
        WorkFlowVariable variable8 = new WorkFlowVariable("WSDLProcessorResponse3", "tns:GetFileResponseMessage");
        WorkFlowVariable variable9 = new WorkFlowVariable("WSDLProcessorResponse4", "tns:GetFileResponseMessage");
        WorkFlowVariable variable10 = new WorkFlowVariable("WSDLProcessorResponse5", "tns:GetFileResponseMessage");
        variables.add(variable1);
        variables.add(variable2);
        variables.add(variable3);
        variables.add(variable4);
        variables.add(variable5);
        variables.add(variable6);
        variables.add(variable7);
        variables.add(variable8);
        variables.add(variable9);
        variables.add(variable10);

        PartnerLink partnerLink1 = new PartnerLink("getData1PL", "tns:GetData", null, "http://192.168.1.1");
        PartnerLink partnerLink2 = new PartnerLink("getData2PL", "tns:GetData", null, "http://192.168.1.1");
        PartnerLink partnerLink3 = new PartnerLink("getData3PL", "tns:GetData", null, "http://192.168.1.1");
        PartnerLink partnerLink4 = new PartnerLink("getData4PL", "tns:GetData", null, "http://192.168.1.1");
        PartnerLink partnerLink5 = new PartnerLink("getData5PL", "tns:GetData", null, "http://192.168.1.1");
        PartnerLink partnerLink6 = new PartnerLink("WSDLProcessor", "tns:GetData", null, "http://www.wsdlprocessor.com");
        partnerLinks.add(partnerLink1);
        partnerLinks.add(partnerLink2);
        partnerLinks.add(partnerLink3);
        partnerLinks.add(partnerLink4);
        partnerLinks.add(partnerLink5);
        partnerLinks.add(partnerLink6);

        WorkFlowInvoke getData1 = new WorkFlowInvoke("getData1", "getData1PL", "", "", "wsdlResponse1");
        WorkFlowInvoke postData1 = new WorkFlowInvoke("postData1", "WSDLProcessor", "", "wsdlResponse1", "WSDLProcessorResponse1");
        WorkFlowInvoke getData2 = new WorkFlowInvoke("getData2", "getData2PL", "", "", "wsdlResponse2");
        WorkFlowInvoke postData2 = new WorkFlowInvoke("postData2", "WSDLProcessor", "", "wsdlResponse2", "WSDLProcessorResponse2");
        WorkFlowInvoke getData3 = new WorkFlowInvoke("getData3", "getData3PL", "", "", "wsdlResponse3");
        WorkFlowInvoke postData3 = new WorkFlowInvoke("postData3", "WSDLProcessor", "", "wsdlResponse3", "WSDLProcessorResponse3");
        WorkFlowInvoke getData4 = new WorkFlowInvoke("getData4", "getData4PL", "", "", "wsdlResponse4");
        WorkFlowInvoke postData4 = new WorkFlowInvoke("postData4", "WSDLProcessor", "", "wsdlResponse4", "WSDLProcessorResponse4");
        WorkFlowInvoke getData5 = new WorkFlowInvoke("getData5", "getData5PL", "", "", "wsdlResponse5");
        WorkFlowInvoke postData5 = new WorkFlowInvoke("postData5", "WSDLProcessor", "", "wsdlResponse5", "WSDLProcessorResponse5");
        WorkFlowInvoke enterPoint = new WorkFlowInvoke("enterPoint", "", "", "", "");
        WorkFlowInvoke endPoint = new WorkFlowInvoke("endPoint", "", "", "", "");
        activityMap.put("enterPoint", enterPoint);
        activityMap.put("endPoint", endPoint);
        activityMap.put("getData1", getData1);
        activityMap.put("postData1", postData1);
        activityMap.put("getData2", getData2);
        activityMap.put("postData2", postData2);
        activityMap.put("getData3", getData3);
        activityMap.put("postData3", postData3);
        activityMap.put("getData4", getData4);
        activityMap.put("postData4", postData4);
        activityMap.put("getData5", getData5);
        activityMap.put("postData5", postData5);

        graphMap.put("Beginning", new ArrayList<String>(Arrays.asList("enterPoint")));
        graphMap.put("enterPoint", new ArrayList<String>(Arrays.asList("getData1", "getData2", "getData3", "getData4", "getData5")));
        graphMap.put("getData1", new ArrayList<String>(Arrays.asList("postData1")));
        graphMap.put("getData2", new ArrayList<String>(Arrays.asList("postData2")));
        graphMap.put("getData3", new ArrayList<String>(Arrays.asList("postData3")));
        graphMap.put("getData4", new ArrayList<String>(Arrays.asList("postData4")));
        graphMap.put("getData5", new ArrayList<String>(Arrays.asList("postData5")));

        graphMap.put("postData1", new ArrayList<String>(Arrays.asList("endPoint")));
        graphMap.put("postData2", new ArrayList<String>(Arrays.asList("endPoint")));
        graphMap.put("postData3", new ArrayList<String>(Arrays.asList("endPoint")));
        graphMap.put("postData4", new ArrayList<String>(Arrays.asList("endPoint")));
        graphMap.put("postData5", new ArrayList<String>(Arrays.asList("endPoint")));

        graphMap.put("endPoint", new ArrayList<String>(Arrays.asList("ending")));

        graphMapBackword.put("enterPoint", new ArrayList<String>(Arrays.asList("Beginning")));
        graphMapBackword.put("getData1", new ArrayList<String>(Arrays.asList("enterPoint")));
        graphMapBackword.put("getData2", new ArrayList<String>(Arrays.asList("enterPoint")));
        graphMapBackword.put("getData3", new ArrayList<String>(Arrays.asList("enterPoint")));
        graphMapBackword.put("getData4", new ArrayList<String>(Arrays.asList("enterPoint")));
        graphMapBackword.put("getData5", new ArrayList<String>(Arrays.asList("enterPoint")));
        graphMapBackword.put("postData1", new ArrayList<String>(Arrays.asList("getData1")));
        graphMapBackword.put("postData2", new ArrayList<String>(Arrays.asList("getData2")));
        graphMapBackword.put("postData3", new ArrayList<String>(Arrays.asList("getData3")));
        graphMapBackword.put("postData4", new ArrayList<String>(Arrays.asList("getData4")));
        graphMapBackword.put("postData5", new ArrayList<String>(Arrays.asList("getData5")));
        graphMapBackword.put("endPoint", new ArrayList<String>(Arrays.asList("postData1", "postData2", "postData3", "postData4", "postData5")));
        graphMapBackword.put("ending", new ArrayList<String>(Arrays.asList("endPoint")));

        process.activityMap = activityMap;
        process.graphMap = graphMap;
        process.graphMapBackword = graphMapBackword;
        process.variables = variables;
        process.partnerLinks = partnerLinks;
        WorkFlowGenerate generate = new WorkFlowGenerate(process);
        return generate;
    }
}
