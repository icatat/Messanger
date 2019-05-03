
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Class that manages the client side of a P2P connection
 * the UserClient are started in the TCPClient class, when we want to connect to another user via a P2P connection
 * UserClient will connect to a ClientServer, that can be found in the TCPClient of the other user we want to connect to
 * the window that pops up is cause by this class, and hence we will use this class to connect and send messages to the others
 */

class UserClient implements Runnable{

    public JTextField tx;
    public JTextArea ta;
    public JTextArea activeTextArea;

    public Socket clientSocket;
    public Socket serverSocket;
    public DataOutputStream outToP2PDest;
    public DataOutputStream outToP2PCur;
    public BufferedReader inFromP2P;
    public BufferedReader inFromP2PCur;
    public String username;
    public String usernameTo;
    public String usernameFrom;
    public int portDest;
    public int portCur;
    public InetAddress addr;


    /**
     *
     * @param username - is of the format 'from/to' and gives the name of the new opening chat. The 'from' indicates the current
     *                 user for which we are opening the chat, and 'to' indicates the user to which the current user is sending mesages
     * @param from - the current user sending messages
     * @param to - the user we are sending messages to
     * @param addr - the IP address of the 'to' user (the user we are sending messages to)
     * @param portDest - the port number of the 'to' user, to which we are sending packages
     * @param portCur - the port number of the 'from' (current) user, so that we can send the messages to their own server as well, to be shown on the screen
     * @throws Exception
     */

    public UserClient(String username, String from, String to, InetAddress addr, int portDest, int portCur) throws Exception {

        this.username = username;
        this.usernameTo = (from + "/" + to).toLowerCase();
        this.usernameFrom = (to + "/" + from).toLowerCase();
        this.portDest = portDest;
        this.portCur = portCur;
        this.addr = addr; //the other user's address


        JFrame f = new JFrame(username);
        f.setSize(600, 400);

        JPanel p1 = new JPanel();
        p1.setLayout(new BorderLayout());

        JPanel p2 = new JPanel();
        p2.setLayout(new BorderLayout());

        JPanel activeUsers = new JPanel();
        activeUsers.setBackground(Color.YELLOW);
        activeUsers.setAlignmentX(200);


        tx = new JTextField();
        ta = new JTextArea();
        activeTextArea = new JTextArea(10, 10);

        try {
            clientSocket = new Socket(addr, portDest); //where you are sending the data to the P2P server
            serverSocket = new Socket(InetAddress.getLocalHost(), portCur); //send the other server
            outToP2PDest = new DataOutputStream(clientSocket.getOutputStream()); // one output stream that goes to the current user
            outToP2PCur = new DataOutputStream(serverSocket.getOutputStream()); ///one output stream that goes to the other user
            inFromP2P = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));//the messages that come through the connection from the client Socket
            inFromP2PCur = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
        } catch (Exception e) {
            System.out.println("Problem initiating clientSocket, outToServer, inFromServer");
        }

        // UI
        p1.add(tx, BorderLayout.CENTER);
        JButton b1 = new JButton("Send");
        p1.add(b1, BorderLayout.EAST);
        activeUsers.add(activeTextArea, BorderLayout.CENTER);
        activeUsers.setBackground(Color.YELLOW);
        p2.add(activeUsers, BorderLayout.EAST);

        p2.add(ta, BorderLayout.CENTER);
        p2.add(p1, BorderLayout.SOUTH);

        f.setContentPane(p2);
        ////////////////////////////////////////////////////////////////////////

        // make the button send messages to both the to and the cur user
        b1.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent ev) {
                     //Format the message for the appropriate user
                     String dest = usernameFrom + ":" + "Me" + ":" + tx.getText() + "\r\n";
                     String cur = usernameFrom  + ":"  + from + ":" + tx.getText() + "\r\n";
                     tx.setText("");
                     try {
                         // Send the message to both the current user and to the user to the other end
                         outToP2PCur.writeBytes(cur);
                         outToP2PCur.flush();
                         outToP2PDest.writeBytes(dest);
                         outToP2PDest.flush();

                     } catch (Exception e) {
                         e.printStackTrace();
                     }
                 }
             }
        );

        f.setVisible(true);
    }


        public void run () {

        try {
            // connect the user to the other user via P2P, so that the other user knows where the message is coming from
            this.LogIn();

            while (true) {
                // read the messaged to the input stream
                String serverMsg = "";
                serverMsg = this.inFromP2P.readLine();
                this.ta.append(serverMsg + "\n");
            }
        }catch (Exception e) {
            System.out.println("Error in starting user client");
        }


    }

    /**
     * Log in the users, so that we can easily indetiyfy a buffer to send messages to
     * @throws Exception
     */
    public void LogIn() throws Exception{
        outToP2PDest.writeBytes("Login:" + usernameFrom + "\r\n");
        outToP2PDest.flush();

        outToP2PDest.writeBytes("Login:" + usernameTo+ "\r\n");
        outToP2PDest.flush();


    }


}



