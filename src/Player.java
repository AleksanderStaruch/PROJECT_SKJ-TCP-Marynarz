import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class Player {
    private String name;
    private int MYPORT;
    private InetAddress IIP;
    private int IPORT;

    private static List<String> names=new ArrayList<>();
    private static List<Integer> ports=new ArrayList<>();

    Player(String name, int MYPORT, int IPORT,InetAddress IIP ) {
        this.name = name;
        this.MYPORT = MYPORT;
        this.IIP = IIP;
        this.IPORT = IPORT;
        ports.add(MYPORT);
        names.add(name);
    }

    static List<String> getNames() {
        return names;
    }
    static List<Integer> getPorts(){return ports;}

    String getName() {
        return name;
    }

    public String toString() {
        return name+" "+MYPORT+" "+IIP.getHostAddress()+" "+IPORT;
    }
}