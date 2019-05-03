
import javax.xml.crypto.Data;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.net.*;

public class ClientServer implements Runnable{
    Socket socketSender;

    // HashMap stores the username of teh users connected to the current user via P2P connection and their corresponding buffer
    public static HashMap<String, DataOutputStream> activeUsers = new HashMap<String, DataOutputStream>();


    public ClientServer(Socket socketSender) throws Exception{
        this.socketSender = socketSender;
    }

    /*
    Map the user with the OutPutStream
     */
    public void LogIn(String payload, DataOutputStream os) throws Exception{
        String username = payload.toLowerCase();
        activeUsers.put(username, os);
    }


    public void run() {
        try {
            processRequest();
        } catch (Exception e) {
            System.out.println("Some error occurred: ClientServer");
        }
    }

    private void processRequest() throws Exception {

        //Write into P2P to the other sender
        DataOutputStream osSender = new DataOutputStream(socketSender.getOutputStream());
        //Read from the P2P
        InputStream is = socketSender.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        while (true) {

            String msg = br.readLine();
            System.out.println(msg);
            int indexOfColon = msg.indexOf(':');
            String command = "";
            if (indexOfColon != -1) {
                command = msg.substring(0, indexOfColon);
            } else {
                command = msg;
            }
            String payload = msg.substring(indexOfColon + 1, msg.length());
            // if a message with a Login prefix is received, then log in the yser
            if (command.equals("Login")) {
                LogIn(payload, osSender);
                //All the messages are going to have a user
                //prefix in them , to be able to indetify where the data is coming from
            } else if(activeUsers.containsKey(command)){
                // if the user is loggen in (theoretically, it should always be) sned the message to their output stream
                    activeUsers.get(command).writeBytes(payload + "\n\r\n");
            }
        }
    }
}
