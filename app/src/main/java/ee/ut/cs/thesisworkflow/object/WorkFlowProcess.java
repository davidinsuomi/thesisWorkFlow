package ee.ut.cs.thesisworkflow.object;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ding on 2/1/2015.
 */
public class WorkFlowProcess {
    public ArrayList<PartnerLink> partnerLinks = new ArrayList<PartnerLink>();
    public ArrayList<WorkFlowVariable> variables = new ArrayList<WorkFlowVariable>();
    public Map<String,ArrayList<String>> graphMap = new HashMap<String,ArrayList<String>>();
    public Map<String,ArrayList<String>> graphMapBackword = new HashMap<String,ArrayList<String>>();
    public Map<String,WorkFlowActivity> activityMap = new HashMap<String,WorkFlowActivity>();
}