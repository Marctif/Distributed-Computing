import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Created by Marc Tifrea on 2/6/2018.
 */
public class HomeworkDriver {
    public static void main(String args[]) {
        String configfile = "config.txt";
        String hostname = "";
        Config ownConfig = null;
        HashMap<String, Config> nodesByHostname;
        HashMap<String, Config> nodesByID = null;
        try {
            HashMap<String, HashMap<String, Config>> nodes = ConfigReader.getConfig(configfile);
            nodesByID = nodes.get("id");
            ownConfig = nodesByID.get(args[0]);
        } catch (IOException e) {
            System.err.println("An error occured while reading the file");
        }

        int ownUID = 0;
        int ownPort = 0;
        String ownHostname = "";
        ArrayList<Integer> ownNeighbors = null;
        try {
            ownUID = ownConfig.getUID();
            ownPort = ownConfig.getPort();
            ownHostname = ownConfig.getHostname() + ".utdallas.edu"; // hostname only contains dcXX part
            ownNeighbors = ownConfig.getNeighbors();
        } catch (NullPointerException e) {
            System.err.println(String.format("Error! Could not find hostname in config file for %s.", hostname));
        }

        System.out.println(String.format("Launching with UID %d, listening at port %d", ownUID, ownPort));

        NetworkManager network = new NetworkManager(ownHostname, ownPort);
        network.startServer();

        // Message all neighbors
        for(int i = 0; i < ownNeighbors.size(); i++) {
            Config targetNodeConfig = nodesByID.get(Integer.toString(ownNeighbors.get(i)));
            int targetNodeUID = targetNodeConfig.getUID();
            int targetNodePort = targetNodeConfig.getPort();
            String targetNodeHostname = targetNodeConfig.getHostname();

            boolean n1Ready = network.nodeReady(targetNodeHostname, targetNodePort); //While loops
            if (n1Ready)
                System.out.println("Child at " + targetNodeHostname + " " + targetNodePort + " is ready for messages");
        }

        System.out.println(ownUID + " at " + ownHostname + ":" + ownPort + " has connected to all neighbors");

        /**
         * Peleg's Algorithm
         */
        int rounds = 3;
        int numNeighbors = ownNeighbors.size();
        int maxUID = ownUID;

        for(int x = 1; x <= rounds; x++){
            Message message = new Message(maxUID, 0, x);

            // Message all neighbors
            for(int i = 0; i < ownNeighbors.size(); i++) {
                Config targetNodeConfig = nodesByID.get(Integer.toString(ownNeighbors.get(i)));
                int targetNodePort = targetNodeConfig.getPort();
                String targetNodeHostname = targetNodeConfig.getHostname();
                String rsp = network.sendTestMessage(message,targetNodeHostname, targetNodePort);
                System.out.println("RESP: " + targetNodeHostname + " - " + rsp);
            }

            int recieved = 0;
            while(true) {
                // Print all messages
                System.out.println("waiting for messages"); //Keep this in (things got out of sync when it was left out IDK why)
                Queue<String> queue = network.getMessageQueue();
                if (queue.peek() != null) {
                    String stringMessage = queue.poll();
                    Message m = new Message(stringMessage);
                    System.out.println("MESSAGE: " + stringMessage);
                    if (m.getRoundNumber() == x) { //Message from current round
                        recieved++;
                        if (m.getMaxUID() > maxUID)
                            maxUID = m.getMaxUID();
                    } else { //Push message back into queue for later
                        queue.add(stringMessage);
                    }
                }
                if(recieved >= numNeighbors)
                    break;
            }
        }
        System.out.println("The max UID is: " + maxUID);

        while(true){ //To keep everything running

        }
            // String rsp2 = network.sendTestMessage("Test hello #2",hostName, targetPort);
//            while (true) {
//                PriorityQueue<String> queue = network.getMessageQueue();
//                if (queue.peek() != null) {
//                    System.out.println(queue.poll());
//                }
//            }
    }
}
