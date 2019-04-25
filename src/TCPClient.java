import java.io.*;
import java.net.*;
class TCPClient {


    public static void LogIn(BufferedReader inFromUser, DataOutputStream outToServer,  BufferedReader inFromServer) throws Exception{
        System.out.println("Please enter your username: \n");
        String sentence = inFromUser.readLine();
        outToServer.writeBytes("Login " + sentence + "\n");
        String modifiedSentence = inFromServer.readLine();
    }

    public void LogOut() {

    }

    public void LostConnection() {

    }

    //on entering "Show Active"
    public void ActiveUsers() {

    }


    public static void main(String argv[]) throws Exception
    {
        String sentence;
        String modifiedSentence;

        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

        Socket clientSocket = new Socket("172.18.58.157", 5000);
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromServer =new BufferedReader(new InputStreamReader(
                clientSocket.getInputStream()));

        LogIn(inFromUser, outToServer, inFromServer);

        while (true) {

            System.out.println("Please enter the text that you want to send to the server \n");

            sentence = inFromUser.readLine();

            outToServer.writeBytes(sentence + "\r\n");

            modifiedSentence = inFromServer.readLine();

            System.out.println("RECEIVED FROM SERVER: " + modifiedSentence);
        }


    }
}


