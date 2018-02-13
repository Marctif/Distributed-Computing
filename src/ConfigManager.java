import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.StringTokenizer;

public class ConfigManager {
    public static void main(String args[]) throws IOException {
        String filepath = args[0];  // this may be relative path as well
        System.out.println(String.format("Input config file to read: %s", filepath));

        // we need the hostname of machine to assign node ID
        String hostname = InetAddress.getLocalHost().getHostName();
        System.out.println(String.format("I am running on machine %s", hostname));

        // more about Path and Paths: https://docs.oracle.com/javase/tutorial/essential/io/pathOps.html
        // using it here so that we get proper error when config file is missing
        Path p = Paths.get(filepath);
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
                    int UID = 0;
                    String HostName = "";
                    int Port = 0;
                    List<String> Neighbors = new ArrayList<>();

                    String delimeter = " ";
                    StringTokenizer st = new StringTokenizer(contents, delimeter);

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
                                Neighbors.add(st.nextToken());
                        }
                        count++;
                    }

                    // compare Hostname string with hostname we obtained earlier and if they match,
                    // TODO: launch HomeworkDriver class
                    if (hostname.toLowerCase().contains(HostName.toLowerCase())) {
                        System.out.println(String.format("Launching with UID %d, will listen on port %d", UID, Port));
                        System.out.println(String.format("Neighboring nodes are %s", Neighbors.toString()));
                    }
                }
            }
        } catch (NoSuchFileException x) {
            System.err.format("%s: no such" + "file or directory %n", p.toAbsolutePath());
        }
    }
}
