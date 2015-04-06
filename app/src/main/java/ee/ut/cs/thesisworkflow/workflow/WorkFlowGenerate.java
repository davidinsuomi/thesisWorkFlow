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
import java.util.Map;

import ee.ut.cs.thesisworkflow.object.PartnerLink;
import ee.ut.cs.thesisworkflow.object.WorkFlowActivity;
import ee.ut.cs.thesisworkflow.object.WorkFlowAssign;
import ee.ut.cs.thesisworkflow.object.WorkFlowInvoke;
import ee.ut.cs.thesisworkflow.object.WorkFlowProcess;
import ee.ut.cs.thesisworkflow.object.WorkFlowVariable;
public class WorkFlowGenerate {
    private Map<String,ArrayList<String>> graphMap;
    private Map<String,ArrayList<String>> graphMapBackword;
    private Map<String,WorkFlowActivity> activityMap;
    private ArrayList<WorkFlowVariable> variables;
    private ArrayList<PartnerLink> partnerLinks;

    private Map<String,ArrayList<String>> offloadingGraphMap;
    private Map<String,ArrayList<String>> offloadingGraphMapBackword;
    private Map<String,WorkFlowActivity> offloadingActivityMap;
    private Map<String,WorkFlowVariable> offloadingVariables = new HashMap<String,WorkFlowVariable>();
    private Map<String,PartnerLink> offloadingPartnerLinks = new HashMap<String,PartnerLink>();

    private static StringWriter writer = new StringWriter();
    private XmlSerializer xmlSerializer = Xml.newSerializer();
    private static String TAG = "GENERATEXML";

    public WorkFlowGenerate(WorkFlowProcess workflowProcess){
        graphMap = workflowProcess.graphMap;
        graphMapBackword = workflowProcess.graphMapBackword;
        activityMap = workflowProcess.activityMap;
        variables = workflowProcess.variables;
        partnerLinks = workflowProcess.partnerLinks;
    }

    public static WorkFlowGenerate testWorkFlowInstance(){
        WorkFlowProcess process = new WorkFlowProcess();
        ArrayList<PartnerLink> partnerLinks = new ArrayList<PartnerLink>();
        ArrayList<WorkFlowVariable> variables = new ArrayList<WorkFlowVariable>();
        Map<String,ArrayList<String>> graphMap = new HashMap<String,ArrayList<String>>();
        Map<String,ArrayList<String>> graphMapBackword = new HashMap<String,ArrayList<String>>();
        Map<String,WorkFlowActivity> activityMap = new HashMap<String,WorkFlowActivity>();
        // logic to fill all the variable

        WorkFlowVariable variable1 = new WorkFlowVariable("wsdlResponse1","tns:GetFileResponseMessage");
        WorkFlowVariable variable2 = new WorkFlowVariable("wsdlResponse2","tns:GetFileResponseMessage");
        WorkFlowVariable variable3 = new WorkFlowVariable("wsdlResponse3","tns:GetFileResponseMessage");
        WorkFlowVariable variable4 = new WorkFlowVariable("wsdlResponse4","tns:GetFileResponseMessage");
        WorkFlowVariable variable5 = new WorkFlowVariable("wsdlResponse5","tns:GetFileResponseMessage");
        WorkFlowVariable variable6 = new WorkFlowVariable("WSDLProcessorResponse1","tns:GetFileResponseMessage");
        WorkFlowVariable variable7 = new WorkFlowVariable("WSDLProcessorResponse2","tns:GetFileResponseMessage");
        WorkFlowVariable variable8 = new WorkFlowVariable("WSDLProcessorResponse3","tns:GetFileResponseMessage");
        WorkFlowVariable variable9 = new WorkFlowVariable("WSDLProcessorResponse4","tns:GetFileResponseMessage");
        WorkFlowVariable variable10 = new WorkFlowVariable("WSDLProcessorResponse5","tns:GetFileResponseMessage");
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

        PartnerLink partnerLink1 = new PartnerLink("getData1PL","tns:GetData",null,"http://192.168.1.1");
        PartnerLink partnerLink2 = new PartnerLink("getData2PL","tns:GetData",null,"http://192.168.1.1");
        PartnerLink partnerLink3 = new PartnerLink("getData3PL","tns:GetData",null,"http://192.168.1.1");
        PartnerLink partnerLink4 = new PartnerLink("getData4PL","tns:GetData",null,"http://192.168.1.1");
        PartnerLink partnerLink5 = new PartnerLink("getData5PL","tns:GetData",null,"http://192.168.1.1");
        PartnerLink partnerLink6 = new PartnerLink("WSDLProcessor","tns:GetData",null,"http://www.wsdlprocessor.com");
        partnerLinks.add(partnerLink1);
        partnerLinks.add(partnerLink2);
        partnerLinks.add(partnerLink3);
        partnerLinks.add(partnerLink4);
        partnerLinks.add(partnerLink5);
        partnerLinks.add(partnerLink6);

        WorkFlowInvoke getData1 = new WorkFlowInvoke("getData1","getData1PL","","","wsdlResponse1");
        WorkFlowInvoke postData1 = new WorkFlowInvoke("postData1","WSDLProcessor","","wsdlResponse1","WSDLProcessorResponse1");
        WorkFlowInvoke getData2 = new WorkFlowInvoke("getData2","getData2PL","","","wsdlResponse2");
        WorkFlowInvoke postData2 = new WorkFlowInvoke("postData2","WSDLProcessor","","wsdlResponse2","WSDLProcessorResponse2");
        WorkFlowInvoke getData3 = new WorkFlowInvoke("getData3","getData3PL","","","wsdlResponse3");
        WorkFlowInvoke postData3 = new WorkFlowInvoke("postData3","WSDLProcessor","","wsdlResponse3","WSDLProcessorResponse3");
        WorkFlowInvoke getData4 = new WorkFlowInvoke("getData4","getData4PL","","","wsdlResponse4");
        WorkFlowInvoke postData4 = new WorkFlowInvoke("postData4","WSDLProcessor","","wsdlResponse4","WSDLProcessorResponse4");
        WorkFlowInvoke getData5 = new WorkFlowInvoke("getData5","getData5PL","","","wsdlResponse5");
        WorkFlowInvoke postData5 = new WorkFlowInvoke("postData5","WSDLProcessor","","wsdlResponse5","WSDLProcessorResponse5");
        WorkFlowInvoke enterPoint = new WorkFlowInvoke("enterPoint","","","","");
        WorkFlowInvoke endPoint = new WorkFlowInvoke("endPoint","","","","");
        activityMap.put("enterPoint",enterPoint);
        activityMap.put("endPoint",endPoint);
        activityMap.put("getData1",getData1);
        activityMap.put("postData1",postData1);
        activityMap.put("getData2",getData2);
        activityMap.put("postData2",postData2);
        activityMap.put("getData3",getData3);
        activityMap.put("postData3",postData3);
        activityMap.put("getData4",getData4);
        activityMap.put("postData4",postData4);
        activityMap.put("getData5",getData5);
        activityMap.put("postData5",postData5);

        graphMap.put("Beginnering", new ArrayList<String>(Arrays.asList("enterPoint")));
        graphMap.put("enterPoint", new ArrayList<String>(Arrays.asList("getData1","getData2","getData3","getData4","getData5")));
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

        graphMapBackword.put("enterPoint", new ArrayList<String>(Arrays.asList("Beginnering")));
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
        graphMapBackword.put("endPoint", new ArrayList<String>(Arrays.asList("postData1","postData2","postData3","postData4","postData5")));
        graphMapBackword.put("ending", new ArrayList<String>(Arrays.asList("endPoint")));

        process.activityMap = activityMap;
        process.graphMap = graphMap;
        process.graphMapBackword = graphMapBackword;
        process.variables = variables;
        process.partnerLinks = partnerLinks;
        WorkFlowGenerate generate = new WorkFlowGenerate(process);
        return generate;
    }
    private void InitializeXmlSerializer() throws IllegalArgumentException, IllegalStateException, IOException {
        xmlSerializer.setOutput(writer);
        //Start Document
        xmlSerializer.startDocument("UTF-8", true);
        xmlSerializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

        //TODO create partnerLinks
        xmlSerializer.startTag("", "partnerLinks");
        for(Object value : offloadingPartnerLinks.values()){
            PartnerLink partnerLink = (PartnerLink) value;
            CreatePartnerLink(partnerLink);
        }
        xmlSerializer.endTag("", "partnerLinks");
        //TODO create variables
        xmlSerializer.startTag("", "variables");
        for(Object value : offloadingVariables.values()){
            WorkFlowVariable workFlowVariable = (WorkFlowVariable) value;
            CreateVariable(workFlowVariable);
        }
        xmlSerializer.endTag("", "variables");

        //Open Tag <file>
        xmlSerializer.startTag("", "process");

    }
    private void FinalizeXmlSerializer() throws IllegalArgumentException, IllegalStateException, IOException{
        xmlSerializer.endDocument();
        Log.e(TAG, writer.toString());

    }

    private void FindNewOffloadingVariablesAndPartnerLink(String startTask, String endTask){
        if(startTask.equals(endTask)){
            FindCurrentTaskVariableAndPartnerLink(startTask);
        }else{
            while(!startTask.equals(graphMap.get(endTask).get(0))){
                FindCurrentTaskVariableAndPartnerLink(startTask);
                int nextActivities = graphMap.get(startTask).size();
                if(nextActivities >1) {
                    String endFlowActivity= null;
                    String startActivityInsideFlow = null;
                    for (int i = 0; i < nextActivities; i++) {
                        startActivityInsideFlow = graphMap.get(startTask).get(i);
                        endFlowActivity = FindFlowEndActivty(startActivityInsideFlow);
                        FindNewOffloadingVariablesAndPartnerLink(startActivityInsideFlow, endFlowActivity);
                    }
                    startTask = graphMap.get(endFlowActivity).get(0);
                }
                else {
                    startTask = graphMap.get(startTask).get(0);
                }
            }
        }
    }

    private void FindCurrentTaskVariableAndPartnerLink(String currentTag){
        WorkFlowActivity activity = activityMap.get(currentTag);
        if(activity instanceof WorkFlowInvoke){
            WorkFlowInvoke invoke = (WorkFlowInvoke) activity;
            for(WorkFlowVariable variable : variables){
                if(variable.name.equals(invoke.inputVariable)){
                    if(!offloadingVariables.containsKey(variable.name)){
                        offloadingVariables.put(variable.name, variable);
                    }

                }else if(variable.name.equals(invoke.outputVariable)){
                    if(!offloadingVariables.containsKey(variable.name)){
                        offloadingVariables.put(variable.name, variable);
                    }
                }
            }

            for(PartnerLink partnerLink : partnerLinks){
                if(partnerLink.name.equals(invoke.partnerLink)){
                    if(!offloadingPartnerLinks.containsKey(partnerLink.name)){
                        offloadingPartnerLinks.put(partnerLink.name, partnerLink);
                    }
                }
            }
        }else if(activity instanceof WorkFlowAssign){
            WorkFlowAssign assign = (WorkFlowAssign) activity;
            for(WorkFlowVariable variable : variables){
                if(variable.name.equals(assign.from)){
                    if(!offloadingVariables.containsKey(variable.name)){
                        offloadingVariables.put(variable.name, variable);
                    }

                }else if(variable.name.equals(assign.to)){
                    if(!offloadingVariables.containsKey(variable.name)){
                        offloadingVariables.put(variable.name, variable);
                    }
                }
            }
        }
    }
    public StringWriter offLoadingTask(String startTask, String endTask) throws IllegalArgumentException, IllegalStateException, IOException{
        FindNewOffloadingVariablesAndPartnerLink(startTask,endTask);
        InitializeXmlSerializer();
        TaskToBeOffloading(startTask,endTask);
        xmlSerializer.endTag("", "process");
        FinalizeXmlSerializer();
        return writer;
    }
    //So far only to able to offloading sequenceTask
    public void TaskToBeOffloading(String startTask, String endTask) throws IllegalArgumentException, IllegalStateException, IOException{
        if(startTask.equals(endTask)){
            //only one task need to offloading
            xmlSerializer.startTag("", "sequence");
            CreateCurrentXMLTag(startTask);
            xmlSerializer.endTag("", "sequence");
            xmlSerializer.endTag("", "process");
        }else{
            xmlSerializer.startTag("", "sequence");
            while(!startTask.equals(graphMap.get(endTask).get(0))){
                CreateCurrentXMLTag(startTask);
//                startTask = graphMap.get(startTask).get(0);
                int nextActivities = graphMap.get(startTask).size();
                if(nextActivities >1) {
                    String endFlowActivity= null;
                    String startActivityInsideFlow = null;
                    xmlSerializer.startTag("", "flow");
                    for (int i = 0; i < nextActivities; i++) {
                        startActivityInsideFlow = graphMap.get(startTask).get(i);
                        endFlowActivity = FindFlowEndActivty(startActivityInsideFlow);
                        TaskToBeOffloading(startActivityInsideFlow, endFlowActivity);

                    }
                    xmlSerializer.endTag("", "flow");
                    startTask = graphMap.get(endFlowActivity).get(0);
                }
                else {
                    startTask = graphMap.get(startTask).get(0);
                }

            }
            xmlSerializer.endTag("", "sequence");
        }
    }

    private String FindFlowEndActivty(String activityName){
        String previousActivity = null;
        while(graphMapBackword.get(activityName).size() == 1){
            previousActivity = activityName;
            activityName = graphMap.get(activityName).get(0);
        }
        return previousActivity;
    }
    private void CreateCurrentXMLTag(String currentTag) throws IllegalArgumentException, IllegalStateException, IOException{
        WorkFlowActivity activity = activityMap.get(currentTag);
        if(activity instanceof WorkFlowInvoke){
            WorkFlowInvoke invoke = (WorkFlowInvoke) activity;
            CreateInvoke(invoke);
        }else if(activity instanceof WorkFlowAssign){
            WorkFlowAssign assign = (WorkFlowAssign) activity;
            CreateAssign(assign);
        }
    }
    private void CreateVariable(WorkFlowVariable workFlowVariable) throws IllegalArgumentException, IllegalStateException, IOException{
        xmlSerializer.startTag("", "variable");
        if(workFlowVariable.messageType != null){
            xmlSerializer.attribute("", "messageType", workFlowVariable.messageType);
        }
        xmlSerializer.attribute("", "name", workFlowVariable.name);
        xmlSerializer.endTag("", "variable");
    }
    private void CreatePartnerLink(PartnerLink partnerLink) throws IllegalArgumentException, IllegalStateException, IOException{
        xmlSerializer.startTag("", "partnerLink");
        if(partnerLink.myRole != null){
            xmlSerializer.attribute("", "myRole", partnerLink.myRole);
        }
        if(partnerLink.partnerLinkType != null){
            xmlSerializer.attribute("", "partnerLinkType", partnerLink.partnerLinkType);
        }
        if(partnerLink.name != null){
            xmlSerializer.attribute("", "name", partnerLink.name);
        }
        if(partnerLink.URL != null){
            xmlSerializer.text(partnerLink.URL);
        }
        xmlSerializer.endTag("", "partnerLink");
    }

    private void CreateAssign(WorkFlowAssign workFlowAssign) throws IllegalArgumentException, IllegalStateException, IOException{
        xmlSerializer.startTag("", "assign");
        xmlSerializer.attribute("", "name" , workFlowAssign.name);
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

    private void CreateInvoke(WorkFlowInvoke workFlowInvoke) throws IllegalArgumentException, IllegalStateException, IOException{
        xmlSerializer.startTag("", "invoke");
        xmlSerializer.attribute("", "name", workFlowInvoke.name);
        if(workFlowInvoke.partnerLink !=null) {
            xmlSerializer.attribute("", "partnerLink", workFlowInvoke.partnerLink);
        }
        if(workFlowInvoke.operation !=null) {
            xmlSerializer.attribute("", "operation", workFlowInvoke.operation);
        }
        if(workFlowInvoke.inputVariable !=null) {
            xmlSerializer.attribute("", "inputVariable", workFlowInvoke.inputVariable);
        }
        if(workFlowInvoke.outputVariable != null) {
            xmlSerializer.attribute("", "outputVariable", workFlowInvoke.outputVariable);
        }
        xmlSerializer.endTag("", "invoke");
    }
}
