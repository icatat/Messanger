
import javax.xml.crypto.Data;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.net.*;

public class ClientServer implements Runnable{
    Socket socketSender;
    Socket socketReceiver;

    public static HashMap<String, DataOutputStream> activeUsers = new HashMap<String, DataOutputStream>();


    public ClientServer(Socket socketSender, Socket socketReceiver) throws Exception{
        this.socketReceiver = socketSender;
        this.socketSender = socketReceiver;
    }

    public void LogIn(String payload, DataOutputStream os) throws Exception{
        String [] payloadInfo = payload.split(" ");
        String username = payloadInfo[0].toLowerCase();
        Socket socket = new Socket(InetAddress.getLocalHost(), Integer.parseInt(payloadInfo[2]));
        activeUsers.put(username, os);
    }


    public void run() {
        try {
            processRequest();
        } catch (Exception e) {
            System.out.println("Some error occurred");
        }
    }

    private void processRequest() throws Exception {

        DataOutputStream osSender = new DataOutputStream(socketSender.getOutputStream());
        DataOutputStream osReceiver = new DataOutputStream(socketReceiver.getOutputStream());

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
            String username = "";
            if (command.equals("Login")) {
                LogIn(payload, osSender);
            } else {
                String user = payload.substring(0, payload.indexOf(':'));
                if(user.equals(command)) {
                    String message = payload.substring(payload.indexOf(':') + 1, payload.length());
                    activeUsers.get(command.toLowerCase()).writeBytes("Me: " + message + "\n\r\n");
                } else {
                    activeUsers.get(command.toLowerCase()).writeBytes(payload + "\n\r\n");
                }
            }
//
//            String outputMsg = msg.toUpperCase();
//            os.writeBytes(outputMsg);
//            os.writeBytes("\r\n");
//            System.out.println("Sent to client: " + outputMsg);
//            os.flush();
        }
    }
}
