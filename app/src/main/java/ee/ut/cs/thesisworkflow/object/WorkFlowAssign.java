package ee.ut.cs.thesisworkflow.object;

/**
 * Created by weiding on 16/02/15.
 */
public class WorkFlowAssign extends WorkFlowActivity {
    public String from;
    public String to;
    public WorkFlowAssign(String _name, String _from, String _to){
        super(_name);
        from=_from;
        to = _to;
    }
}
