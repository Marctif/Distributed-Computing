import java.util.PriorityQueue;

/**
 * Created by Marc Tifrea on 2/6/2018.
 */
public class HomeworkDriver {
    public static void main(String args[]) {
        String hostName = args[0];
        int ownPort = Integer.parseInt(args[1]);
        int targetPort = Integer.parseInt(args[2]);

        NetworkManager network = new NetworkManager(hostName,ownPort);
        network.startServer();

        boolean n1Ready = network.nodeReady(hostName, targetPort); //While loops
        if(n1Ready)
            System.out.println("Child at " + hostName + " " + targetPort + " is ready for messages");
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
