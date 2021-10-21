package TcpClient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    //set port number here
    final static int PORT = 3000;
    //set host address here
    final static String ADDRESS = "localhost";

    public static void main(String[] args) throws IOException {
        Socket s = new Socket(ADDRESS,PORT);
        DataOutputStream outStream = new DataOutputStream(s.getOutputStream());
        DataInputStream inStream = new DataInputStream(s.getInputStream());

        Scanner in = new Scanner(System.in);
        //this loop runs until finish command is given
        while(true){
            //takes command from users
            System.out.println("Enter command for server or use help: ");
            String cmd = in.nextLine();
            //sends to servers
            outStream.writeUTF(cmd);
            outStream.flush();
            //receive and print Response message
            System.out.println("\nResponse");
            System.out.println("---------------------------------------------------------------");
            System.out.println(inStream.readUTF());
            System.out.println("---------------------------------------------------------------");
            if(cmd.equals("finish"))
                break;
        }

        in.close();
        s.close();

    }
}
