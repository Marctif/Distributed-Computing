import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;

/**
 * Created by Marc Tifrea on 2/6/2018.
 */
public class HomeworkDriver {
    public static void main(String args[]) {
        if (args.length < 1) {
            System.err.println("No arguments give. I require the path of config file to start.");
            System.exit(-1);
        }

        String configfile = args[0];
        String hostname = "";
        Config ownConfig = null;
        try {
            // get own hostname. This is required to find which node this is
            hostname = InetAddress.getLocalHost().getHostName();
            System.out.println(String.format("I am running on machine %s", hostname));

            HashMap<String, Config> nodes = ConfigReader.getConfig(configfile);

            // Going to get own port and entry
            // We need to get the first part of hostname though
            // Why first part:
            //  We are getting hostname from system and it will return something like dc01.utdallas.edu
            //  But config file has dc01
            int firstDot = hostname.indexOf('.');

            if (firstDot != -1) {
                hostname = hostname.substring(0, firstDot).toLowerCase();
                ownConfig = nodes.get(hostname);
            }
        } catch (IOException e) {
            System.err.println("An error occured while reading the file");
        }

        int ownUID = 0;
        int ownPort = 0;
        try {
            ownUID = ownConfig.getUID();
            ownPort = ownConfig.getPort();
        } catch (NullPointerException e) {
            System.err.println(String.format("Error! Could not find hostname in config file for %s.", hostname));
        }


        System.out.println(String.format("Launching with UID %d, listening at port %d", ownUID, ownPort));

//        int ownPort = Integer.parseInt(args[1]);
//        int targetPort = Integer.parseInt(args[2]);
//
//        NetworkManager network = new NetworkManager(hostName,ownPort);
//        network.startServer();
//
//        boolean n1Ready = network.nodeReady(hostName, targetPort); //While loops
//        if(n1Ready)
//            System.out.println("Child at " + hostName + " " + targetPort + " is ready for messages");
//
//        // String rsp2 = network.sendTestMessage("Test hello #2",hostName, targetPort);
//        while(true){
//            PriorityQueue<String> queue = network.getMessageQueue();
//            if(queue.peek() != null){
//                System.out.println(queue.poll());
//            }
//        }
    }
}
