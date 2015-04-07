package ee.ut.cs.thesisworkflow.object;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

/**
 * Created by weiding on 16/02/15.
 */
public class WorkFlowVariable {
    public String name;
    public String messageType;
    private byte[] value;
    private String data;
    public  List<String> datas;
    public WorkFlowVariable(String _name, String _messageType){
        name =_name;
        messageType = _messageType;
    }

    public void SetValue(byte[] value) {
        this.value = value;
        if(messageType.contains("List")){
            String listInString = null;
            try {
                listInString = new String(value, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            datas = Arrays.asList(listInString.split(","));
        }else if(messageType.contains("String")){
            try {
                data =  new String(value, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    public String GetData(){
        return this.data;
    }
    public void SetData(String string){
        this.data = string;
        value = string.getBytes(Charset.forName("UTF-8"));
    }
    public boolean HasValue(){
        if(datas !=null || data != null)
            return true;
        else
            return false;
    }
    public boolean IsList(){
        if(datas != null){
            return true;
        }
        else
            return false;
    }
    public byte[] GetValue(){
        return this.value;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return name + "     " + messageType + "\n";
    }


}