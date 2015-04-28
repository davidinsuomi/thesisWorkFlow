package ee.ut.cs.thesisworkflow.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by weiding on 07/04/15.
 */
public class Conf {
    public static List<ExternalIP> IPs = new ArrayList<ExternalIP>(Arrays.asList(new ExternalIP[]{
            new ExternalIP("http://192.168.0.102"),
            new ExternalIP("http://192.168.0.103"),
            new ExternalIP("http://192.168.0.104")
    }));

    public static List<String> CollaborateDeviceIPs = Arrays.asList(
            "http://192.168.0.102",
            "http://192.168.0.103",
            "http://192.168.0.104");
}
