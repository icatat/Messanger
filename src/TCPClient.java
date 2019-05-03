

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

    /**
     *
     * @param username - the username you are logging in as
     * @param port - the port number you are connecting TO
     * @param ip -- the IP address of the machine on which the server is running and TO which you are connecting
     * @throws Exception
     */

    public TCPClient(String username, int port, String ip) throws Exception{
        this.username = username;
        this.port = port;
        this.addr = InetAddress.getByName(ip); //

        // generate a random Port number to be used as the server port number
        // incoming connecting from P2P connections will send packets to this port in order to access the server
        Random r = new Random();
        this.serverSidePort = r.nextInt(6000) + 3000; //needs to match the server we want to connect to

        // UI components
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
        //////////////////////////////////////////////////////

        try {
            clientSocket = new Socket(this.addr, port); //client socket to receive from the server
            serverSocket = new ServerSocket(serverSidePort); //the server socket to which other P2P clients will send packages
            outToServer = new DataOutputStream(clientSocket.getOutputStream()); //server buffer
            inFromServer =new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // coming from the server
        } catch (Exception e) {
            System.out.println("Problem initiating clientSocket, outToServer, inFromServer");
        }

        //UI attaching messages to the messanger window
        p1.add(tx, BorderLayout.CENTER);
        JButton b1=new JButton("Send");
        p1.add(b1, BorderLayout.EAST);
        activeUsers.add(activeTextArea, BorderLayout.CENTER);
        activeUsers.setBackground(Color.YELLOW);
        p2.add(activeUsers, BorderLayout.EAST);
        p2.add(ta, BorderLayout.CENTER);
        p2.add(p1, BorderLayout.SOUTH);

        f.setContentPane(p2);

        // Send messages to the server when pressing a button
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


    /**
     * Log In function - one logs into the server
     * When the user tries to connect to the MAIN server, it send its LOCAL ADDRESS and SERVER PORT, that would further
     * help other clients can connect to the server when initiating a P2P
     * @throws Exception
     */
    public void LogIn() throws Exception{
        outToServer.writeBytes("Login:" + username + " " + InetAddress.getLocalHost() + " " + serverSidePort + " \r\n");
        outToServer.flush();
    }


    public void run(){
        try{
            // when running the threat, we first log in
            this.LogIn();

            while(true) {
                String serverMsg = "";
                serverMsg = inFromServer.readLine();
                //we read the response from the server and parse it
                int index = serverMsg.indexOf(":");
                if (index != -1) {
                    String command = serverMsg.substring(0, index);
                    //if we receive a list of active users (that means that someone logged in ) we list everyone active
                    if (command.equals("ACTIVE")) {
                        String payload = serverMsg.substring(index + 1, serverMsg.length());
                        String[] commands = payload.split(" ");
                        for (int i = 0; i < commands.length; i++) {
                            activeTextArea.append(commands[i] + "\n"); //listing all the active users received from the server
                        }
                    } else if (command.equals("P2P")) {
                        // if we attempt to connect to a user, a response with the P2P prefix will be send
                        // then, we parse the Local address and the port of the user we are trying to connect with
                        // we initiate a conneciion to that address
                        try {
                            //parse the address of the user
                            String payload = serverMsg.substring(index + 1, serverMsg.length());
                            String[] commands = payload.split(" ");
                            String ipAddress = commands[1].substring(commands[1].indexOf('/') + 1, commands[1].length());
                            // initiate a connection to the other server
                            UserClient client = new UserClient(this.username + "/" + commands[0], this.username, commands[0], InetAddress.getByName(ipAddress), Integer.parseInt(commands[2]), this.serverSidePort);
                            // Start a client threat
                            Thread clientThread = new Thread(client);
                            clientThread.start();
                        } catch (Exception e) {
                            System.out.println("Problem connecting to P2P Server");
                        }
                    }
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
            System.out.println("Error connecting to the MAIN server");
        }

        while (true) {
            System.out.println("Waiting for incoming connection...");
            Socket connection = client.serverSocket.accept(); //the  server socket to receive messages through P2P
            ClientServer request = new ClientServer(connection); // the name of the chat is the name of the person contacted
            Thread thread = new Thread(request);
            thread.start();
        }

    }
}


