package ee.ut.cs.thesisworkflow.object;

/**
 * Created by weiding on 27/03/15.
 */
public class ForeachRepeatTask {
    public int count;
    public String startTag;
    public String endTag;
    public ForeachRepeatTask(int count, String startTag, String endTag){
        this.count  = count;
        this.startTag = startTag;
        this.endTag  = endTag;
    }
}
