package ee.ut.cs.thesisworkflow.Data;

/**
 * Created by weiding on 07/04/15.
 */
public class CollaborateDevice {
    public String IP;
    public int weight;
    public int startPosition;
    public int endPosition;
    public int CPU;
    public int RAM;
    public int Battery;
    public int Bandwidth;
    public CollaborateDevice(String IP){
        this.IP = IP;
    }
}
