import java.util.PriorityQueue;

/**
 * Created by Marc Tifrea on 2/6/2018.
 */
public class HomeworkDriver {
    public static void main(String args[]) {
        NetworkManager network = new NetworkManager("localhost",5000);
        network.startServer();
        network.sendTestMessage("Test hello");
        network.sendTestMessage("Test hello #2");
        PriorityQueue<String> queue = network.getMessageQueue();
        while(true){
            if(queue.peek() != null){
                System.out.println(queue.poll());
            }
        }
    }
}
