package ee.ut.cs.thesisworkflow.Data;

/**
 * Created by weiding on 07/04/15.
 */
public class ExternalIP {
    public String IP;
    public int weight;
    public int startPosition;
    public int endPosition;
    public int CPU;
    public int RAM;
    public int Battery;
    public ExternalIP(String IP){
        this.IP = IP;
    }
}
