
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import java.util.Random;


class UserClient implements Runnable{

    public JTextField tx;
    public JTextArea ta;
    public JTextArea activeTextArea;

    public Socket clientSocket;
    public Socket serverSocket;
    public DataOutputStream outToP2PDest;
    public DataOutputStream outToP2PCur;
    public BufferedReader inFromP2P;
    public String username;
    public String usernameTo;
    public String usernameFrom;
    public int portDest;
    public int portCur;
    public InetAddress addr;

    public UserClient(String username, String from, String to, InetAddress addr, int portDest, int portCur) throws Exception {

        this.username = username;
        this.usernameTo = (from + "to" + to).toLowerCase();
        this.usernameFrom = (to + "to" + from).toLowerCase();
        this.portDest = portDest;
        this.portCur = portCur;
        this.addr = addr;

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
        JScrollPane scrollPane = new JScrollPane(activeUsers);
        try {
            clientSocket = new Socket(InetAddress.getLocalHost(), portDest); //where you are sending the data to the P2P server
            serverSocket = new Socket(addr, portCur); //send the other server
            outToP2PCur = new DataOutputStream(clientSocket.getOutputStream()); //communicate with the other one
            outToP2PDest = new DataOutputStream(serverSocket.getOutputStream()); //communicate with yourself
            inFromP2P = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));//the messages that come through the connection from the client Socket
        } catch (Exception e) {
            System.out.println("Problem initiating clientSocket, outToServer, inFromServer");
        }

        p1.add(tx, BorderLayout.CENTER);
        JButton b1 = new JButton("Send");
        p1.add(b1, BorderLayout.EAST);
        activeUsers.add(activeTextArea, BorderLayout.CENTER);
        activeUsers.setBackground(Color.YELLOW);
        p2.add(activeUsers, BorderLayout.EAST);

        p2.add(ta, BorderLayout.CENTER);
        p2.add(p1, BorderLayout.SOUTH);


        f.setContentPane(p2);

        b1.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent ev) {
                     String cur = usernameFrom + ":" + usernameFrom + ":" + tx.getText() + "\r\n";
                     String dest = usernameTo+ ":"  + usernameFrom + ":" + tx.getText() + "\r\n";
                     tx.setText("");
                     try {
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
            this.LogIn();

            while (true) {
                String serverMsg = "";
                serverMsg = this.inFromP2P.readLine();
                this.ta.append(serverMsg + "\n");
            }
        }catch (Exception e) {
            System.out.println("Error in starting user client");
        }


    }

    public void LogIn() throws Exception{
        outToP2PDest.writeBytes("Login:" + usernameFrom + "\r\n");
        outToP2PDest.flush();

        outToP2PCur.writeBytes("Login:" + usernameFrom + "\r\n");
        outToP2PCur.flush();

    }


}



