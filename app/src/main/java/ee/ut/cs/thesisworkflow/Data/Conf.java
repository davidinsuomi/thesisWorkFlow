package ee.ut.cs.thesisworkflow.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by weiding on 07/04/15.
 */
public class Conf {
    public static List<CollaborateDevice> AvailableDevices = new ArrayList<CollaborateDevice>(Arrays.asList(new CollaborateDevice[]{
            new CollaborateDevice("http://192.168.0.100"),
            new CollaborateDevice("http://192.168.0.103")
    }));

    public static List<String> CollaborateDevices = Arrays.asList(
            "http://192.168.0.100",
            "http://192.168.0.103");
}
