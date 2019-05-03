

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import java.util.HashMap;
import java.util.Random;


class TCPClient implements Runnable{

    public JTextField tx;
    public JTextArea ta;
    public JTextArea activeTextArea;

    public Socket clientSocket;
    public ServerSocket serverSocket;
    public DataOutputStream outToServer;
    public BufferedReader inFromServer;
    public String username;
    public int port;
    public InetAddress addr;
    public int serverSidePort;

    public TCPClient(String username, int port, String ip) throws Exception{

        this.username = username;
        this.port = port;
        this.addr = InetAddress.getByName(ip); //

        Random r = new Random();
        this.serverSidePort = r.nextInt(6000) + 3000; //needs to match the server we want to connect to

        JFrame f=new JFrame(username);
        f.setSize(600,400);

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
            clientSocket = new Socket(this.addr, port);
            serverSocket = new ServerSocket(serverSidePort);
            outToServer = new DataOutputStream(clientSocket.getOutputStream());
            inFromServer =new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        } catch (Exception e) {
            System.out.println("Problem initiating clientSocket, outToServer, inFromServer");
        }

        p1.add(tx, BorderLayout.CENTER);
        JButton b1=new JButton("Send");
        p1.add(b1, BorderLayout.EAST);
        activeUsers.add(activeTextArea, BorderLayout.CENTER);
        activeUsers.setBackground(Color.YELLOW);
        p2.add(activeUsers, BorderLayout.EAST);

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


    public void LogIn() throws Exception{
        outToServer.writeBytes("Login:" + username + " " + InetAddress.getLocalHost() + " " + serverSidePort + " \r\n");
        outToServer.flush();
    }


    public void run(){
        try{
            this.LogIn();

            while(true) {
                String serverMsg = "";
                serverMsg = inFromServer.readLine();
                int index = serverMsg.indexOf(":");
                if (index != -1) {
                    String command = serverMsg.substring(0, index);
                    if (command.equals("ACTIVE")) {
                        String payload = serverMsg.substring(index + 1, serverMsg.length());
                        String[] commands = payload.split(" ");
                        for (int i = 0; i < commands.length; i++) {
                            activeTextArea.append(commands[i] + "\n");
                        }
                    } else if (command.equals("P2P")) {
                        ta.append(serverMsg + "\n");
                        try {
                            String payload = serverMsg.substring(index + 1, serverMsg.length());
                            String[] commands = payload.split(" ");
                            String ipAddress = commands[1].substring(commands[1].indexOf('/') + 1, commands[1].length());
                            System.out.println(ipAddress);
                            UserClient client = new UserClient(this.username + "/" + commands[0], this.username, commands[0], InetAddress.getByName(ipAddress), Integer.parseInt(commands[2]), this.serverSidePort);
                            Thread clientThread = new Thread(client);
                            clientThread.start();
                        } catch (Exception e) {
                            System.out.println("Problem connecting to P2P Server");
                        }
                    }

                    ta.append(serverMsg + "\n");
                } else {
                    ta.append(serverMsg + "\n");
                }
            }

        }catch(Exception e){e.printStackTrace();}
    }


    public static void main(String argv[]) throws Exception
    {
        String username = argv[0];
        String ip = argv[1];
        //Client thread to start  connecting to the server
        TCPClient client = new TCPClient(username,5000, ip);

        try {
            Thread chat = new Thread(client);
            chat.start();
        } catch (Exception e) {
            System.out.println("Oppps");
        }

        while (true) {
            System.out.println("Waiting...");
            Socket connection = client.serverSocket.accept(); //the  server socket to receive messages through P2P
            //clientSocket
            ClientServer request = new ClientServer(connection); // the name of the chat is the name of the person contacted
            Thread thread = new Thread(request);
            thread.start();
        }

    }
}


