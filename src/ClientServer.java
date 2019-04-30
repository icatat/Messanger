
import javax.xml.crypto.Data;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.net.*;

public class ClientServer implements Runnable{
    Socket socketSender;

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

            int indexOfColon = msg.indexOf(':');
            String command = "";
            if (indexOfColon != -1) {
                command = msg.substring(0, indexOfColon);
            } else {
                command = msg;
            }
            String payload = msg.substring(indexOfColon + 1, msg.length());
            if (command.equals("Login")) {
                LogIn(payload, osSender);
            } else {
                String user = payload.substring(0, payload.indexOf(':'));
                if(user.equals(command)) {
                    //Write to Self
                    String message = payload.substring(payload.indexOf(':') + 1, payload.length());
                    activeUsers.get(command.toLowerCase()).writeBytes("Me: " + message + "\n\r\n");
                } else {
                    activeUsers.get(command.toLowerCase()).writeBytes(payload + "\n\r\n");
                }
            }
        }
    }
}
