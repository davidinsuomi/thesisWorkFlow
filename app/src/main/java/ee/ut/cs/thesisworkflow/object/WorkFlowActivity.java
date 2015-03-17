package ee.ut.cs.thesisworkflow.object;
import java.util.concurrent.atomic.AtomicBoolean;
/**
 * Created by weiding on 16/02/15.
 */
public class WorkFlowActivity {
    public String name;
    public AtomicBoolean status = new AtomicBoolean(false);
    public WorkFlowActivity(String activityName){
        name = activityName;
    }
}