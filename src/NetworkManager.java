import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;


public class NetworkManager {
    private int portNumber;
    private String hostname;
    private Queue<String> messageQueue;
    private ArrayList<Message> messageList;
    private ArrayList<Message> BFSmessageList;
    private boolean pelegDone = false;
    private boolean beginBFS = false;

    public NetworkManager(String hostname, int portNumber) {
        this.portNumber = portNumber;
        this.hostname = hostname;
        messageQueue = new LinkedList<String>();
        messageList = new ArrayList<Message>();
        BFSmessageList = new ArrayList<Message>();
    }

    public Queue<String> getMessageQueue(){
        return messageQueue;
    }
    public ArrayList<Message> getMessageList() { return messageList; }
    public ArrayList<Message> getBFSMessageList() { return BFSmessageList; }
    public void finishedPeleg(){
        pelegDone = true;
    }


    // Start a thread for the server
    public void startServer(){
        TcpMServer server = new TcpMServer();
        Thread t = new Thread(server);
        t.start();
    }

    //Send a message via TcpClient
    public String sendTestMessage(String message, String targetHost, int targetPort) {
        TcpClient SampleClientObj = new TcpClient();
        return SampleClientObj.go(message, targetHost, targetPort);
    }

    public String sendTestMessage(Message message, String targetHost, int targetPort) {
        TcpClient SampleClientObj = new TcpClient();
        return SampleClientObj.go(message, targetHost, targetPort);
    }

    //Keep sending message to a target until it is up
    public boolean nodeReady(String targetHost, int targetPort) {
        while(true) {
            Message message = new Message(10, 7, 999999);
            String rsp1 = sendTestMessage(message, targetHost, targetPort);
             // System.out.println("Sending ping message to " + targetHost + ":" + targetPort);
            if(rsp1 != null) {
                // System.out.println("Server Response is: " + rsp1);
                break;
            }
        }
        return true;
    }

    class TcpMServer implements Runnable {
        public void run() {
            try	{
                //Create a server socket at port 5000
                ServerSocket serverSock = new ServerSocket(portNumber);

                //Server goes into a permanent loop accepting connections from clients
                while(true)
                {
                    //Listens for a connection to be made to this socket and accepts it
                    //The method blocks until a connection is made
                    ClientManager w;
                    try {
                        w = new ClientManager(serverSock.accept());
                        Thread t = new Thread(w);
                        t.start();
                    } catch(IOException e) {
                        System.out.println("accept failed");
                        System.exit(100);
                    }
                }

            } catch(IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    class ClientManager implements Runnable {

        private Socket client;

        public ClientManager(Socket client) {
            this.client = client;
        }

        public void run() {
            String line;
            BufferedReader in = null;
            PrintWriter out = null;

            try {
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out = new PrintWriter(client.getOutputStream(), true);
                line = in.readLine();

                while(line != null) {

                    // System.out.println(line);
                    // send data back to the client
                    Message m = new Message((line));
                    if (m.getRoundNumber() != 999999)  { //Ignore test message TODO better handeling
                        //System.out.println("SERVER " + line);
                        messageQueue.add(line);

                    if(m.getMessageType().equals("default") || m.getMessageType().equals("sync")) {
                        while (true) { //peleg logic
                            try {
                                if (!pelegDone)
                                    out.println(messageList.get(m.getRoundNumber() - 1));
                                else
                                    out.println(messageList.get(messageList.size() - 1));
                                break;
                            } catch (IndexOutOfBoundsException ex) {

                            }
                        }
                    } else { //bfs logic
                        while (true) { //peleg logic
                            try {
                                out.println(BFSmessageList.get(m.getRoundNumber() - 1));
                                break;
                            } catch (IndexOutOfBoundsException ex) {

                            }
                        }
                        //System.out.println(m);
                        //System.out.println("BFS LOGIC *******************");

                    }
                    } else {
                        out.println(line);
                    }
                    //out.println(line);
                    // System.out.println("Message sent to client");

                    line = in.readLine();
                }


            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    class TcpClient {

        public String go(String input, String targetHost, int targetPort) {

            String ServerMessage;
            BufferedReader reader = null;
            PrintWriter writer = null;

            try {
                // Create a client socket and connect to server at @targetHost and @targetPort
                Socket clientSocket = new Socket(targetHost, targetPort);

                /* Create BufferedReader to read messages from server. Input stream is in bytes.
                    They are converted to characters by InputStreamReader.
                    Characters from the InputStreamReader are converted to buffered characters by BufferedReader.
                    This is done for efficiency purpose.
                */
                reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                // PrintWriter is a bridge between character data and the socket's low-level output stream
                writer = new PrintWriter(clientSocket.getOutputStream(), true);
            } catch (IOException ex) {
                if (!(ex instanceof ConnectException)) {
                    ex.printStackTrace();
                }
                return null;
            }

            try {
                writer.println(input);
                ServerMessage = reader.readLine(); // Server response
                return ServerMessage;
            } catch(IOException e) {
                System.out.println("Read failed");
            }
            return null;
        }

        public String go(Message message, String targetHost, int targetPort) {

            String ServerMessage;
            BufferedReader reader = null;
            PrintWriter writer = null;

            try {
                // Create a client socket and connect to server at @targetHost and @targetPort

                Socket clientSocket = new Socket(targetHost, targetPort);

                /* Create BufferedReader to read messages from server. Input stream is in bytes.
                    They are converted to characters by InputStreamReader.
                    Characters from the InputStreamReader are converted to buffered characters by BufferedReader.
                    This is done for efficiency purpose.
                */
                reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                // PrintWriter is a bridge between character data and the socket's low-level output stream
                writer = new PrintWriter(clientSocket.getOutputStream(), true);
            } catch (IOException ex) {
                if (!(ex instanceof ConnectException)) {
                    ex.printStackTrace();
                }
                return null;
            }

            try {
                writer.println(message);
                ServerMessage = reader.readLine(); // Server response
                Message receivedMessage = new Message(ServerMessage);                

                return ServerMessage;
            } catch(IOException e) {
                System.out.println("Read failed");
            }
            return null;
        }
    }
}
