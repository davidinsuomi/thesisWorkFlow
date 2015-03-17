package ee.ut.cs.thesisworkflow.object;
/**
 * Created by weiding on 16/02/15.
 */
public class WorkFlowVariable {
    public String name;
    public String messageType;
    public byte[] value;
    public WorkFlowVariable(String _name, String _messageType){
        name =_name;
        messageType = _messageType;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return name + "     " + messageType + "\n";
    }


}