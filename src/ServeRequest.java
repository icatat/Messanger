import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.*;

public class ServeRequest implements Runnable {
    Socket socket;
    public static HashMap<String, DataOutputStream> activeUsers = new HashMap<String, DataOutputStream>();
    //Key -> saves the one to one convesrations for each user
    //ArrayList<String> -> the list of conversations for each user //use the string to get the corresponding socket from the above HM
    public static HashMap<String, String> oneToOneChats = new HashMap<>();

    //Socket -> the person for who we are tracking the groupChats
    // HM -> stores the name of the group chats and the name of the users in that given group chat
    public static HashMap<Socket, HashMap<String, ArrayList<String>>> groupChats = new HashMap<>();

    public void LogIn(String username, DataOutputStream os) {
        activeUsers.put(username, os);
    }

    /**
     * Formats a list of all the active users and sends it to the client, whoever that person is
     * @param os
     * @throws Exception
     */
    public void printActiveUsers(DataOutputStream os) throws Exception{
        StringBuffer currentActiveUsers = new StringBuffer();
        for (String name : activeUsers.keySet()) {
            currentActiveUsers.append(name);
            currentActiveUsers.append(" ");
        }
        os.writeBytes(currentActiveUsers.toString());
    }

    public void connects( String to, String from) throws Exception {
        oneToOneChats.put(to, from);
        oneToOneChats.put(from, to);
    }

    public void sendOneToOneMessage(String message, String currentUser) throws Exception{
            message = currentUser + ": " + message + "\r\n";
            String to = oneToOneChats.get(currentUser);
            activeUsers.get(to).writeBytes(message);
    }

    public void sendToGroup(String message, String curName) throws Exception{
        for(String name: activeUsers.keySet()) {
            if(name.equals(curName)) continue;
            message = message + "\r\n";
            activeUsers.get(name).writeBytes(message);
        }
    }
    // Constructor
    public ServeRequest(Socket socket) throws Exception {
        this.socket = socket;
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
// Get a reference to the socket's input and output streams.

        InputStream is = socket.getInputStream();
        DataOutputStream os = new DataOutputStream(socket.getOutputStream());
// Set up input streams
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String curName = "";

        while (true) {
// Get the incoming message from the client (read from socket)
            String msg = br.readLine();
            int indexOfColon = msg.indexOf(':');
            String command = "";

            if (indexOfColon != -1) {
                command = msg.substring(0, indexOfColon);
            } else {
                command = msg;
            }

            System.out.println(command);
            String commandMessage = msg.substring(indexOfColon + 1, msg.length());
            if (command.equals("Login")) {
                curName = msg.substring(indexOfColon + 1, msg.length());
                LogIn(curName, os);
            } else if (command.equals("Active")) {
                printActiveUsers(os);
            } else if (command.equals("Connects")) {
                String to = msg.substring(indexOfColon + 1, msg.length());
                String from = curName;
                connects(to, from);
            } else if (command.equals("SendAll")) {
                sendToGroup(command, curName);
            } else {
                sendOneToOneMessage(command, curName);
            }


//Print message received from client
            System.out.println("Received from client: ");
            System.out.println(msg);
//convert message to upper case
            String outputMsg = msg.toUpperCase();
//Send modified msg back to client (write to socket)
            os.writeBytes(outputMsg);
            os.writeBytes("\r\n");
            System.out.println("Sent to client: ");
        }
    }
}