package ee.ut.cs.thesisworkflow.object;

/**
 * Created by weiding on 16/02/15.
 */
public class WorkFlowInvoke extends WorkFlowActivity {
    public String partnerLink;
    public String operation;
    public String inputVariable;
    public String outputVariable;
    public WorkFlowInvoke(String _name, String _partnerLink, String _operation, String _inputVariable, String _outputVariable){
        super(_name);
        partnerLink = _partnerLink;
        operation = _operation;
        inputVariable = _inputVariable;
        outputVariable = _outputVariable;
    }
}