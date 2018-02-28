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
        String configFile = "config.txt";
        String hostname = "";
        Config ownConfig = null;
        HashMap<String, Config> nodesByHostname;
        HashMap<String, Config> nodesByID = null;
        try {
            HashMap<String, HashMap<String, Config>> nodes = ConfigReader.getConfig(configFile);
            nodesByID = nodes.get("id");
            ownConfig = nodesByID.get(args[0]);
        } catch (IOException e) {
            System.err.println("An error occurred while reading the file");
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
        for (int i = 0; i < ownNeighbors.size(); i++) {
            Config targetNodeConfig = nodesByID.get(Integer.toString(ownNeighbors.get(i)));
            int targetNodeUID = targetNodeConfig.getUID();
            int targetNodePort = targetNodeConfig.getPort();
            String targetNodeHostname = targetNodeConfig.getHostname();

            boolean n1Ready = network.nodeReady(targetNodeHostname, targetNodePort); //While loops
            if (n1Ready)
                System.out.println("Child at " + targetNodeHostname + " " + targetNodePort + " is ready for messages");
        }

        System.out.println(ownUID + " at " + ownHostname + ":" + ownPort + " has connected to all neighbors");

        /*
         * Peleg's Algorithm
         */
        int rounds = 1000;
        int numNeighbors = ownNeighbors.size();
        int maxUID = ownUID;

        int diam = 0;

        int breakCount = 0;
        boolean searchBreak = false;

        for (int x = 1; x <= rounds; x++) {
            //System.out.println("ROUND " + x + ": maxUID: " + maxUID + " | maxDiam: " + diam);
            int prevDiam = diam;
            Message message = new Message(maxUID, diam, x);
            network.getMessageList().add(message);

            ArrayList<Message> roundMessages = new ArrayList<Message>();

            // Message all neighbors
            for (int i = 0; i < ownNeighbors.size(); i++) {
                Config targetNodeConfig = nodesByID.get(Integer.toString(ownNeighbors.get(i)));
                int targetNodePort = targetNodeConfig.getPort();
                String targetNodeHostname = targetNodeConfig.getHostname();
                String rsp = network.sendTestMessage(message, targetNodeHostname, targetNodePort);
                //System.out.println("RESP: " + targetNodeHostname + " - " + rsp);
                Message rspMessage = new Message(rsp);
                //System.out.println(rspMessage.getMessageType());
                if(rspMessage.getMessageType().equals("sync")){
                    searchBreak = true;
                }
                roundMessages.add(rspMessage);
            }

            if(searchBreak){
                network.finishedPeleg();
                Message messageDone = new Message("sync",maxUID, diam, x + 1);
                network.getMessageList().add(messageDone);
                break;
            }

            for (Message m : roundMessages) {
                if (m.getMaxUID() > maxUID) {
                    maxUID = m.getMaxUID();
                    diam = m.getMaxDist() + 1;
                }
                if (m.getMaxDist() > diam)
                    diam = m.getMaxDist();
            }

            if(ownUID == maxUID) {
                if (prevDiam == diam) {
                    breakCount++;
                } else {
                    breakCount = 0;
                }
                if (breakCount >= 3) {
                    network.finishedPeleg();
                    Message messageDone = new Message("sync",maxUID, diam, x + 1);
                    network.getMessageList().add(messageDone);
                    break;
                }
            }

         //   System.out.println("ROUND " + x + ": maxUID: " + maxUID + " | maxDiam: " + diam);
        }
//        if(searchBreak){
//            System.out.println("Leader told me to finish");
//        }
//        if(ownUID == maxUID) {
//            System.out.println("I am the maxUID");
//        }
        //System.out.println("The max UID is: " + maxUID + " and my diam is :" + diam);

        /*
         * BFS search
         */

        int bfsRounds = diam * 2;
        int degree = -1;
        int maxSeenDegree = -1;
        boolean broadcast = false;
        String parent = null;
        int parentId = -1;
        ArrayList<Integer> myChildren = new ArrayList<Integer>();

        for (int y = 1; y <= bfsRounds; y++) {
            Message message;
            if(y == 1 && ownUID == maxUID ) { //Leader broadcast
                message = new Message("search",-1, -1, y); //send it out to start if leader
                parent = "root";
                degree = 0;
            } else if (broadcast && y <=diam){
                message = new Message("search",-1, -1, y); //send it out we got a message and less or equal to diam
            } else if (y > diam){
                message = new Message("parent,"+maxSeenDegree,parentId, ownUID, y); // time to broadcast
            } else {
                message = new Message("parent,"+maxSeenDegree,-1, -1, y); // time to broadcast
            }

            network.getBFSMessageList().add(message);

            ArrayList<Message> roundMessages = new ArrayList<Message>();

            // Message all neighbors
            for (int i = 0; i < ownNeighbors.size(); i++) {
                Config targetNodeConfig = nodesByID.get(Integer.toString(ownNeighbors.get(i)));
                int targetNodePort = targetNodeConfig.getPort();
                String targetNodeHostname = targetNodeConfig.getHostname();
                String rsp = network.sendTestMessage(message, targetNodeHostname, targetNodePort);
                Message rspMessage = new Message(rsp);
                if(rspMessage.getMessageType().equals("search") && parent == null) {
                    parent = targetNodeHostname;
                    parentId = ownNeighbors.get(i);
                    broadcast = true;
                    degree = y;
                    maxSeenDegree = degree;
                }
                roundMessages.add(rspMessage);
            }
            //System.out.println("BFS round " + y);

            if(y > diam) {
                for (Message m : roundMessages) {
                    String [] tokens = m.getMessageType().split(",");
                    int mDegree = Integer.parseInt(tokens[1]);
                    if(mDegree > maxSeenDegree) {
                        maxSeenDegree = mDegree;
                    }
                    if(m.getMaxUID() == ownUID){
                        if(!myChildren.contains(m.getMaxDist())){
                            myChildren.add(m.getMaxDist());
                        }
                    }
                }
            }


        }
        System.out.println();
        System.out.println("Parent: " + parentId);
        if(myChildren.size() == 0) {
            System.out.println("No Children");
        } else {
            for (Integer i : myChildren) {
                System.out.println("BFS Child id: " + i);
            }
        }
        System.out.println("Degree in tree: " + degree);
        if(maxUID == ownUID) {
            System.out.println("I, " + ownUID + ", am the leader and the max degree of any node in the BFS : " + maxSeenDegree);

        }


        while (true) { //To keep everything running

        }

    }
}
