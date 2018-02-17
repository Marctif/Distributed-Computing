import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.StringTokenizer;
import java.util.HashMap;

public class ConfigReader {
    public static HashMap<String, HashMap<String, Config>> getConfig(String filepath) throws IOException {
        System.out.println(String.format("Input config file to read: %s", filepath));

        // more about Path and Paths: https://docs.oracle.com/javase/tutorial/essential/io/pathOps.html
        // using it here so that we get proper error when config file is missing
        Path p = Paths.get(filepath);

        // we will be returning these
        int UID = 0;
        String HostName = "";
        int Port = 0;

        HashMap<String, HashMap<String, Config>> Nodes = new HashMap<>();
        HashMap<String, Config> NodesByHostname = new HashMap<>();
        HashMap<String, Config> NodesByID = new HashMap<>();

        Config c;
        try {
            Path fp = p.toRealPath();

            // reading all lines
            List<String> lines;
            lines = Files.readAllLines(fp);

            // flag to ignore first line
            boolean foundFirst = false;

            for (String s : lines) {
                // ignoring blank lines & those starting with a hash
                if (!s.trim().startsWith("#") && s.length() > 0) {
                    // first value is number of nodes. This isn't used anywhere
                    if (!foundFirst) {
                        foundFirst = true;
                        continue;
                    }

                    // cleaning the string a bit
                    // removing extra spaces & tabs
                    String contents = s.replaceAll(" +", " ");
                    contents = contents.replace("\t", " ");

                    /*
                     * Config file Format:
                     * UID  Hostname    Port    Neighbours
                     *
                     * By now, we have config file without extra spaces and tabs.
                     * So, split by spaces and assign first part to UID, second to Hostname
                     *  and third to Port. Rest go to an array, neighbours.
                     * If Hostname is in current machine's hostname (retrieved earlier),
                     *  launch HomeworkDriver
                     */

                    String delimeter = " ";
                    StringTokenizer st = new StringTokenizer(contents, delimeter);

                    // Neighbors are initialised here because it needs to reset every loop
                    ArrayList<Integer> Neighbors = new ArrayList<>();

                    int count = 0;
                    while (st.hasMoreTokens()) {
                        switch (count) {
                            case 0:
                                UID = Integer.parseInt(st.nextToken());
                                break;
                            case 1:
                                HostName = st.nextToken();
                                break;
                            case 2:
                                Port = Integer.parseInt(st.nextToken());
                                break;
                            default:
                                Neighbors.add(Integer.parseInt(st.nextToken()));
                        }
                        count++;
                    }

                    // add our config object to list of nodes
                    NodesByHostname.put(HostName.toLowerCase(), new Config(UID, HostName, Port, Neighbors));
                    NodesByID.put(Integer.toString(UID), new Config(UID, HostName, Port, Neighbors));
                }
            }
        } catch (NoSuchFileException x) {
            System.err.format("%s: no such" + "file or directory %n", p.toAbsolutePath());
        }

        Nodes.put("hostname", NodesByHostname);
        Nodes.put("id", NodesByID);

        return Nodes;
    }
}
