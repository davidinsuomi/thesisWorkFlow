package ee.ut.cs.thesisworkflow.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by weiding on 07/04/15.
 */
public class Conf {
    public static List<ExternalIP> IPs = new ArrayList<ExternalIP>(Arrays.asList(new ExternalIP[]{
            new ExternalIP("http://192.168.0.102:8080", 40),
            new ExternalIP("http://192.168.0.103:8080", 30),
            new ExternalIP("http://192.168.0.104:8080", 30)
    }));
}
