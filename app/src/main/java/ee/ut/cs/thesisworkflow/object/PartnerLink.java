package ee.ut.cs.thesisworkflow.object;

/**
 * Created by weiding on 16/02/15.
 */
public class PartnerLink {
    public String name;
    public String partnerLinkType;
    public String myRole;
    public String URL;
    public PartnerLink(String _name, String _partnerLinkType, String _myRole, String _URL){
        name = _name;
        partnerLinkType = _partnerLinkType;
        myRole = _myRole;
        URL = _URL;
    }
    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return  name + "   " + partnerLinkType + "   " + myRole + "\n";
    }

}


//For example, you will have a class called "PartnerLink" for the
//<partnerLink> tag, which consists of three attributes: name,
//partnerLinkType and myRole. All of them are String.