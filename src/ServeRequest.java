import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.*;

public class ServeRequest implements Runnable {
    Socket socket;
    public static HashMap<String, DataOutputStream> activeUsers = new HashMap<String, DataOutputStream>();
    //Key -> saves the one to one convesrations for each user
    //ArrayList<String> -> the list of conversations for each user //use the string to get the corresponding socket from the above HM
    public static HashMap<Socket, String> oneToOneChats = new HashMap<>();

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

    public void sendOneToOneMessage( String to, String message) throws Exception {
        message = message + "\r\n";
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
            String[] commands = msg.split(" ");
            if (commands[0].equals("Login")) {
                curName = commands[1];
                LogIn(curName, os);
            }
            if (commands[0].equals("Active")) {
                printActiveUsers(os);
            }
            if (commands[0].equals("Connects")) {
                sendOneToOneMessage(commands[1], commands[2]);
            }

            if (commands[0].equals("SendAll")) {
                sendToGroup(commands[1], curName);
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