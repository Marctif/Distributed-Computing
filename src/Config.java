import java.util.ArrayList;

/**
 * Class to hold the configuration values for a node
 */
final class Config {
    private final int UID;
    private final String hostname;
    private final int port;
    private ArrayList<Integer> Neighbors;

    Config(int UID, String hostname, int port, ArrayList<Integer> Neighbors) {
        this.UID = UID;
        this.hostname = hostname;
        this.port = port;
        this.Neighbors = Neighbors;
    }

    public int getUID() {
        return this.UID;
    }

    public String getHostname() {
        return this.hostname;
    }

    public int getPort() {
        return this.port;
    }

    public ArrayList<Integer> getNeighbors() {
        return this.Neighbors;
    }
    public void setNeighbors(ArrayList<Integer> neighbors) { this.Neighbors = neighbors; }

}
