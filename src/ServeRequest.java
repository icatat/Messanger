import javax.xml.crypto.Data;
import java.io.*;
import java.net.Socket;
import java.util.*;


public class ServeRequest implements Runnable {
    Socket socket;
    public static HashMap<String, DataOutputStream> activeUsers = new HashMap<String, DataOutputStream>();
    public static HashMap<String, String> IPs = new HashMap<String, String>();
    public static HashMap<String, Integer> portNumbers = new HashMap<String, Integer>();

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

    /**
     * Helps coordinate the P2P connection between 2 users
     * @param to - the username of the user that we want to connect TO
     * @param from - the username of the user that innitiates the connection
     * @throws Exception
     */
    public void connects( String to, String from) throws Exception {
        //Generate the messages to be sent to each of the users, so that each can connect to the other's server
        String P2PconnectionTo = "P2P:" + to + " " + IPs.get(to) + " " + portNumbers.get(to) + "\n\r\n"; //send to the user initiating the connection
        String P2PconnectionFrom = "P2P:" + from + " " + IPs.get(from) + " " + portNumbers.get(from) + "\n\r\n"; //send to the user we are conneting to

        // send the P2P message to both user of the 2 connections
        activeUsers.get(to).writeBytes(P2PconnectionFrom);
        activeUsers.get(to).flush();
        activeUsers.get(from).writeBytes(P2PconnectionTo);
        activeUsers.get(from).flush();
    }


    /**
     * Broadcast a message to all the users connected (Logged in) to the server
     * @param message
     * @param currentUser
     * @throws Exception
     */
    public void sendAll(String message, String currentUser) throws Exception{
        message = message + "\n\r\n";

        //format the message
        String toMessage = currentUser + ": " + message + "\n\r\n";
        String fromMessage = "Me: " + message + "\n\r\n";

        // Iterate through the list of all the users connected to this server
        // Get each of their outstream buffers and write to it, so that each
        // can receive the message on their end
        for(String name: activeUsers.keySet()) {
            if(name.equals(currentUser)) {
                activeUsers.get(name).writeBytes(fromMessage);
            } else {
                activeUsers.get(name).writeBytes(toMessage);
            }
        }
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

        //parse the messages received from the connected clients
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

            // if a Login prefix is received, log that user in
            if (command.equals("Login")) {
                String [] payloadInfo = payload.split(" ");
                curName = payloadInfo[0];
                LogIn(payload, os);
            //if a Connects prefix is received, then help that user connect to the other one
            } else if (command.equals("Connects")) {
                String to = msg.substring(indexOfColon + 1, msg.length());
                String from = curName;
                connects(to, from);
            // if a SendAll prefix is received, broadcast that messa to everyone in the network
            } else if (command.equals("SendAll")) {
                sendAll(payload, curName);
            //if a user is talking to themselves, point that out in a funny way :D
            } else {
                String outputMsg = msg.toUpperCase();
                os.writeBytes("Are you talking to yourself? HAHA: " + outputMsg);
                os.writeBytes("\r\n");
            }
        }
    }
}