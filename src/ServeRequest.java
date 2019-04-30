import javax.xml.crypto.Data;
import java.io.*;
import java.net.Socket;
import java.util.*;


public class ServeRequest implements Runnable {
    Socket socket;
    public static HashMap<String, DataOutputStream> activeUsers = new HashMap<String, DataOutputStream>();
    public static HashMap<String, String> IPs = new HashMap<String, String>();
    public static HashMap<String, Integer> portNumbers = new HashMap<String, Integer>();

    //String Key -> the person for which we track the group
    //String Value ->
    public static HashMap<String, String> groupChatsForUser = new HashMap<>();
    // String -> name of the group
    //ArrayList -> the list of the members of that group
    public static HashMap<String, ArrayList<String>> groupMembers= new HashMap<>();


    // Constructor
    public ServeRequest(Socket socket) throws Exception {
        this.socket = socket;
    }

    public void LogIn(String payload, DataOutputStream os) throws Exception{
        String[] userInformation = payload.split(" ");
        String username = userInformation[0];
        String ipAddress = userInformation[1];
        int port = Integer.parseInt(userInformation[2]);

        activeUsers.put(username, os);
        IPs.put(username, ipAddress);
        portNumbers.put(username, port); //serverSidePort
        printActiveUsers(username);
    }

    /**
     * Formats a list of all the active users and sends it to the client, whoever that person is
     * @param currentUser
     * @throws Exception
     */
    public void printActiveUsers(String currentUser) throws Exception {
        StringBuffer currentActiveUsers = new StringBuffer();
        for (String name : activeUsers.keySet()) {
            currentActiveUsers.append(name);
            currentActiveUsers.append(" ");
        }

        for (String name : activeUsers.keySet()) {
            activeUsers.get(name).writeBytes("ACTIVE: " + currentActiveUsers + "\r\n");
        }
    }

    public void connects( String to, String from, DataOutputStream os) throws Exception {
        String P2PconnectionTo = "P2P:" + to + " " + IPs.get(to) + " " + portNumbers.get(to) + "\n\r\n";
        String P2PconnectionFrom = "P2P:" + from + " " + IPs.get(from) + " " + portNumbers.get(from) + "\n\r\n";

        activeUsers.get(to).writeBytes(P2PconnectionFrom);
        activeUsers.get(to).flush();
        activeUsers.get(from).writeBytes(P2PconnectionTo);
        activeUsers.get(from).flush();
    }



    public void sendAll(String message, String currentUser) throws Exception{
        message = message + "\n\r\n";

        String toMessage = currentUser + ": " + message + "\n\r\n";
        String fromMessage = "Me: " + message + "\n\r\n";

        for(String name: activeUsers.keySet()) {
            if(name.equals(currentUser)) {
                activeUsers.get(name).writeBytes(fromMessage);
            } else {
                activeUsers.get(name).writeBytes(toMessage);
            }
        }
    }

    public void createGroup(String groupName, String curUser) {
        if (!groupMembers.keySet().contains(groupName)) {
            groupMembers.put(groupName, new ArrayList<>());
            joinGroup(groupName, curUser);
        }
    }

    public void joinGroup(String groupName, String curUser) {
        if (groupMembers.containsKey(groupName)) {
            groupMembers.get(groupName).add(curUser);
        }
    }

    public void sendMessageToGroup(String groupName, String message) {

    }


    // Implement the run() method of the Runnable interface.
    public void run() {
        try {
            processRequest();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void processRequest() throws Exception {
        DataOutputStream os = new DataOutputStream(socket.getOutputStream());
        InputStream is = socket.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String curName = "";

        while (true) {

            String msg = br.readLine();
            int indexOfColon = msg.indexOf(':');
            String command = "";

            if (indexOfColon != -1) {
                command = msg.substring(0, indexOfColon);

            } else {
                command = msg;

            }

            String payload = msg.substring(indexOfColon + 1, msg.length());

            if (command.equals("Login")) {
                String [] payloadInfo = payload.split(" ");
                curName = payloadInfo[0];
                LogIn(payload, os);

            } else if (command.equals("Active")) {
//                printActiveUsers(os);

            } else if (command.equals("Connects")) {
                String to = msg.substring(indexOfColon + 1, msg.length());
                String from = curName;
                connects(to, from, os);

            } else if (command.equals("SendAll")) {
                sendAll(payload, curName);

            } else if (command.equals("Group")) {
                String groupName = msg.substring(indexOfColon + 1, msg.length());
                createGroup(groupName, curName);

            } else if (command.equals("Join")) {

            } else if (command.equals("Send group")) {

            } else {

            String outputMsg = msg.toUpperCase();
            os.writeBytes("Are you talking to yourself? HAHA: " + outputMsg);
            os.writeBytes("\r\n");
            }
        }
    }
}