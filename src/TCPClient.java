import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

class TCPClient implements Runnable{

    public JTextField tx;
    public JTextArea ta;
    public String sentence;
    public String modifiedSentence;

    public Socket clientSocket;
    public DataOutputStream outToServer;
    public BufferedReader inFromServer;

    public String username;

    public TCPClient(String username) {
        this.username = username;
        JFrame f=new JFrame("my chat");
        f.setSize(400,400);

        JPanel p1=new JPanel();
        p1.setLayout(new BorderLayout());

        JPanel p2=new JPanel();
        p2.setLayout(new BorderLayout());


        tx = new JTextField();
        ta = new JTextArea();

        try {
            clientSocket = new Socket("172.18.58.157", 5000);
            outToServer = new DataOutputStream(clientSocket.getOutputStream());
            inFromServer =new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        } catch (Exception e) {
            System.out.println("oops, error");

        }

        p1.add(tx, BorderLayout.CENTER);

        JButton b1=new JButton("Send");
        p1.add(b1, BorderLayout.EAST);


        p2.add(ta, BorderLayout.CENTER);
        p2.add(p1, BorderLayout.SOUTH);

        f.setContentPane(p2);

        b1.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent ev){
                 String s = tx.getText() + "\r\n";
                 tx.setText("");
                 try{
                     outToServer.writeBytes(s);
                     outToServer.flush();
                 }catch(Exception e){e.printStackTrace();}
             }
         }
        );

        f.setVisible(true);
    }


    public void LogIn(String username) throws Exception{
        outToServer.writeBytes("Login:" + username + "\r\n");
        outToServer.flush();
         String modifiedSentence = inFromServer.readLine();
         ta.append(modifiedSentence);
    }

    public void LogOut() {

    }

    public void LostConnection() {

    }

    //on entering "Show Active"
    public void ActiveUsers() {

    }


    public void run(){
        try{
            this.LogIn(username);
            while(true) {
                String serverMsg = "";
                serverMsg = inFromServer.readLine();
                ta.append(serverMsg + "\n");
            }

        }catch(Exception e){e.printStackTrace();}
    }


    public static void main(String argv[]) throws Exception
    {
        String username = argv[0];
        try {
            TCPClient client = new TCPClient(username);
            Thread chat = new Thread(client);
            chat.start();
        } catch (Exception e) {
            System.out.println("Oppps");
        }



    }
}


