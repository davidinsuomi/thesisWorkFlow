package ee.ut.cs.thesisworkflow.workflow;


import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import ee.ut.cs.thesisworkflow.object.ForeachRepeatTask;
import ee.ut.cs.thesisworkflow.object.PartnerLink;
import ee.ut.cs.thesisworkflow.object.WorkFlowActivity;
import ee.ut.cs.thesisworkflow.object.WorkFlowAssign;
import ee.ut.cs.thesisworkflow.object.WorkFlowInvoke;
import ee.ut.cs.thesisworkflow.object.WorkFlowProcess;
import ee.ut.cs.thesisworkflow.object.WorkFlowVariable;

import static android.util.Log.*;


public class WorkFlowXmlParser {
    private WorkFlowProcess workFlowProcess = new WorkFlowProcess();
    private Map<String,ArrayList<String>> graphMap = new HashMap<String,ArrayList<String>>();
    private Map<String,ArrayList<String>> graphMapBackword = new HashMap<String,ArrayList<String>>();
    private Map<String,WorkFlowActivity> activityMap = new HashMap<String,WorkFlowActivity>();
    private Map<String,ArrayList<String>> flowlastTags = new HashMap<String,ArrayList<String>>();
    private ArrayList<ForeachRepeatTask> repeatTasks = new ArrayList<ForeachRepeatTask>();
    private String previousNodeName;
    private String NodeName;
    private static final String ns = null;
    private static final String TAG = "WorkFlowParser";


    String currentTag= null;


    public WorkFlowProcess parse(InputStream in) throws XmlPullParserException, IOException {
        try{
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            // workFlowProcess.partnerLinks
            readAndParse(parser);
            return workFlowProcess;
        }finally{
            in.close();
        }
    }

    private void readAndParse(XmlPullParser parser) throws XmlPullParserException, IOException{
        parser.require(XmlPullParser.START_TAG, ns, "process");

        while(parser.next() != XmlPullParser.END_TAG){
            if(parser.getEventType() != XmlPullParser.START_TAG){
                continue;
            }
            String name = parser.getName();

            if(name.equals("partnerLinks")){
                workFlowProcess.partnerLinks = readPartnerLink(parser);
            }else if(name.equals("variables")){
                workFlowProcess.variables = (readWorkFlowVariable(parser));
            }else if(name.equals("sequence")){
                readWorkFlowSequence(parser,"Beginnering", XmlPullParser.END_DOCUMENT);
                break;
            }
            d("TAG", " partnerLink" + workFlowProcess.partnerLinks.size());
            d("TAG", " VARIABLES" + workFlowProcess.variables.size());

        }
        addMissingFlowTags();
        addMissingEndFlag();
        parsingGraphMapBackword();
        addRepestTask();
        workFlowProcess.activityMap = activityMap;
        workFlowProcess.graphMap = graphMap;
        workFlowProcess.graphMapBackword = graphMapBackword;
        System.out.println();


    }


    private void addRepestTask(){
        for(ForeachRepeatTask task : repeatTasks){
            createForEachParalleTask(task);
        }
    }
    private  void addMissingEndFlag(){
        graphMap.put(currentTag, new ArrayList<String>(Arrays.asList("ending")));
        System.out.println();
    }

    private void parsingGraphMapBackword(){
        for(Map.Entry<String, ArrayList<String>> entry: graphMap.entrySet()){
            String key = entry.getKey();
            ArrayList<String> nodes = entry.getValue();
            for(String node: nodes){
                if(graphMapBackword.containsKey(node)){
                    ArrayList<String> old = graphMapBackword.get(node);
                    old.add(key);
                    ArrayList<String> newList = new ArrayList<String>(old);
                    graphMapBackword.put(node, newList);
                }else{
                    graphMapBackword.put(node, new ArrayList<String>(Arrays.asList(key)));
                }
            }
        }
    }

    private void addMissingFlowTags(){
        for(Map.Entry<String, ArrayList<String>> entry : flowlastTags.entrySet()){
            String key = entry.getKey();
            ArrayList<String> flowLastTagNeedToAdd = entry.getValue();

            if(graphMap.containsKey(key)){
                ArrayList<String> flowFollowingTag = graphMap.get(key);
                for(String s : flowLastTagNeedToAdd){
                    graphMap.put(s, flowFollowingTag);
                }

            }
        }
    }

    private ArrayList<PartnerLink> readPartnerLink(XmlPullParser parser) throws XmlPullParserException , IOException{
        ArrayList<PartnerLink> partnerLinks = new ArrayList<PartnerLink>();
        parser.require(XmlPullParser.START_TAG, ns, "partnerLinks");
        String partnerLinkName = null;
        String partnerLinkType = null;
        String myRole = null;
        String URL ="";

        while(parser.next() != XmlPullParser.END_TAG ){
            if(parser.getEventType() != XmlPullParser.START_TAG){
                continue;
            }
            String name = parser.getName();
            if(name.equals("partnerLink")){
                //parser.require(XmlPullParser.START_TAG, ns, "partnerLink");
                partnerLinkName = parser.getAttributeValue(ns, "name");
                partnerLinkType = parser.getAttributeValue(ns, "partnerLinkType");
                myRole = parser.getAttributeValue(ns, "myRole");

                if (parser.next() == XmlPullParser.TEXT) {
                    URL = parser.getText();
                    parser.nextTag();
                }
                partnerLinks.add(new PartnerLink(partnerLinkName, partnerLinkType, myRole, URL));
                parser.next();
                //parser.require(XmlPullParser.END_TAG, ns, "partnerLink");
            }
        }
        return partnerLinks;
    }

    private ArrayList<WorkFlowVariable> readWorkFlowVariable(XmlPullParser parser) throws XmlPullParserException, IOException{
        ArrayList<WorkFlowVariable> variables = new ArrayList<WorkFlowVariable>();
        parser.require(XmlPullParser.START_TAG, ns, "variables");
        String variableName = null;
        String variableMessageType = null;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            if (name.equals("variable")) {
                parser.require(XmlPullParser.START_TAG, ns, "variable");
                variableName = parser.getAttributeValue(ns, "name");
                variableMessageType = parser.getAttributeValue(ns,"messageType");
                variables.add(new WorkFlowVariable(variableName, variableMessageType));
                parser.nextTag();
                parser.require(XmlPullParser.END_TAG, ns, "variable");
            }
        }
        return variables;
    }

    private String readWorkFlowSequence(XmlPullParser parser , String previousTag, int TAGTYPE) throws XmlPullParserException, IOException{
        boolean flagSkip = false;
        while(parser.next() != TAGTYPE){
            if(parser.getEventType() != XmlPullParser.START_TAG){
                continue;
            }
            String tagName = parser.getName();
            switch(tagName){
                case "assign":
                    currentTag = readAssign(parser);
                    break;
                case "flow":
                    readFlow(parser,previousTag);
                    flagSkip = true;
                    break;
                case "invoke":
                    currentTag = readInvoke(parser);
                    break;
                case "forEach":
                    readForEach(parser,previousTag);
                    flagSkip = true;
                    break;
                default:
                    break;
            }


            // check the first run
            if(previousTag.equals("Beginnering")){
                graphMap.put("Beginnering", new ArrayList<String>(Arrays.asList(currentTag)));
                previousTag = currentTag;
                continue;
            }
            if (!flagSkip) {
                if (graphMap.containsKey(previousTag)) {
                    ArrayList<String> old = graphMap.get(previousTag);
                    old.add(currentTag);
                    ArrayList<String> newList = new ArrayList<String>(old);
                    graphMap.put(previousTag, newList);
                } else {
                    graphMap.put(previousTag,
                            new ArrayList<String>(Arrays.asList(currentTag)));
                }
            }
            flagSkip = false;
            previousTag = currentTag;
        }

        return currentTag;
    }

    private void readFlow(XmlPullParser parser,String previousTag) throws XmlPullParserException, IOException{
        final String  flowPreviousTag = previousTag;
        Log.e(TAG, "flowPrevious Tag" + flowPreviousTag);
        parser.require(XmlPullParser.START_TAG, ns, "flow");
        ArrayList<String> flowTags = new ArrayList<String>();
        while(parser.next() != XmlPullParser.END_TAG){
            String tag = parser.getName();
            if(parser.getEventType() != XmlPullParser.START_TAG){
                continue;
            }
            if(tag.equals("sequence")){
                String addTag = readWorkFlowSequence(parser, flowPreviousTag ,XmlPullParser.END_TAG);
                Log.e(TAG,"flowTag add " + addTag + "flowPreviousTag " + flowPreviousTag);
                flowTags.add(addTag);
            }
        }
        String tag = flowTags.remove(flowTags.size() -1 );
        flowlastTags.put(tag, flowTags);
        Log.e(TAG,"remove tag " + tag );
        for(String string : flowTags){
              Log.e(TAG,"flowLastTag add " + string );
        }
        //parser.nextTag();
    }

    private void readForEach(XmlPullParser parser, String previousTag) throws XmlPullParserException,IOException{
         final String foreachPreviousTag = previousTag;
         parser.require(XmlPullParser.START_TAG,ns,"forEach");
         ArrayList<String> tags = new ArrayList<String>();
         int startCount=0;
         int finalCount=0;
        while(parser.next() != XmlPullParser.END_TAG){
            String tag = parser.getName();
            if(parser.getEventType() != XmlPullParser.START_TAG){
                continue;
            }
            if(tag.equals("startCounterValue")){
                parser.require(XmlPullParser.START_TAG,ns,"startCounterValue");
                if(parser.next() == XmlPullParser.TEXT){
                    startCount = Integer.parseInt(parser.getText());
                }
                parser.next();
            }else if(tag.equals("finalCounterValue")){
                parser.require(XmlPullParser.START_TAG,ns,"finalCounterValue");
                if(parser.next() == XmlPullParser.TEXT){
                    finalCount = Integer.parseInt(parser.getText());
                }
                parser.next();

            }else if(tag.equals("sequence")){
                String endTag = readWorkFlowSequence(parser, foreachPreviousTag ,XmlPullParser.END_TAG);
                int count = finalCount - startCount -1;
                ForeachRepeatTask task = new ForeachRepeatTask(count,previousTag,endTag);
                repeatTasks.add(task);
            }
        }
    }
    private void createForEachParalleTask(ForeachRepeatTask task){
        int count = task.count;
        String startTag = task.startTag;
        String endTag = graphMap.get(task.endTag).get(0);
        String previousTag = startTag;
        String branchPreviousTag = startTag;
        boolean firstRun = true;
        for(int i=0; i < count ; i ++){
            String currentTag = graphMap.get(previousTag).get(0);
            while(!currentTag.equals(endTag)){
                String newTag= "";
                WorkFlowActivity activity = activityMap.get(currentTag);
                if(activity instanceof WorkFlowInvoke){
                   WorkFlowInvoke invoke = createInvokeWithID((WorkFlowInvoke) activity, i+1);
                    activityMap.put(invoke.name,invoke);
                    newTag = invoke.name;

                } else if (activity instanceof  WorkFlowAssign){
                    WorkFlowAssign assign = createAssignWithID((WorkFlowAssign) activity , i+1);
                    activityMap.put(assign.name,assign);
                    newTag = assign.name;
                }
                updateGraphMap(branchPreviousTag,newTag);
                branchPreviousTag = newTag;
                previousTag = currentTag;
                currentTag = graphMap.get(previousTag).get(0);
            }

            //in here every end activity in foreach node need to add following ending tag
             updateGraphMap(branchPreviousTag,currentTag);
             startTag = task.startTag;
             previousTag = startTag;
             branchPreviousTag = startTag;
        }

        Log.e(TAG,"in the end of createForEachParalleTask" + currentTag);
    }
    private void updateGraphMap(String previousTag, String currentTag){
        if (graphMap.containsKey(previousTag)) {
            ArrayList<String> old = graphMap.get(previousTag);
            old.add(currentTag);
            ArrayList<String> newList = new ArrayList<String>(old);
            graphMap.put(previousTag, newList);
        } else {
            graphMap.put(previousTag,
                    new ArrayList<String>(Arrays.asList(currentTag)));
        }
    }


    private WorkFlowAssign createAssignWithID(WorkFlowAssign workFlowAssign, int id){
        String name = workFlowAssign.name + (id);
        String from = workFlowAssign.from;
        String to = workFlowAssign.to;
        WorkFlowAssign assign = new WorkFlowAssign(name, from, to);
        return assign;
    }
    private WorkFlowInvoke createInvokeWithID(WorkFlowInvoke workFlowInvoke, int id){
        String name = workFlowInvoke.name + (id);
        String partnerLink = workFlowInvoke.partnerLink;
        String operation = workFlowInvoke.operation;
        String inputVariable =workFlowInvoke.inputVariable;
        String outputVariable = workFlowInvoke.outputVariable;
        WorkFlowInvoke invoke = new WorkFlowInvoke(name, partnerLink, operation, inputVariable, outputVariable);
        return invoke;
    }
    private String readInvoke(XmlPullParser parser)throws XmlPullParserException, IOException{
        parser.require(XmlPullParser.START_TAG, ns, "invoke");
        String name = null;
        String partnerLink = null;
        String operation = null;
        String inputVariable =null;
        String outputVariable = null;

        name = parser.getAttributeValue(null,"name");
        partnerLink = parser.getAttributeValue(null, "partnerLink");
        operation = parser.getAttributeValue(null,"operation");
        inputVariable = parser.getAttributeValue(null, "inputVariable");
        outputVariable = parser.getAttributeValue(null, "outputVariable");


        parser.nextTag();
        WorkFlowInvoke invoke = new WorkFlowInvoke(name, partnerLink, operation, inputVariable, outputVariable);

        activityMap.put(name, invoke);

        return name;

    }

    private String readAssign(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "assign");
        String name = null;
        String from = null;
        String to = null;

        name = parser.getAttributeValue(null, "name");
        while(parser.next()!= XmlPullParser.END_TAG){
            String tag = parser.getName();
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            if(tag.equals("from")){
                from= parser.getAttributeValue(null, "variable");
                parser.nextTag();
            }else if ( tag.equals("to")){
                to = parser.getAttributeValue(null, "variable");
                parser.nextTag();
            }
        }
        parser.nextTag();
        WorkFlowAssign assign = new WorkFlowAssign(name, from, to);
        activityMap.put(name, assign);
        return name;

    }

}