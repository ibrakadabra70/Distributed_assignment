package TcpServer;

import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    static int counter = 0;
    //set port number here
    final static int PORT = 3000;

    public static void main(String[] args) {



        try {
            //Part1.Server Runs on Port 3000 or port given in args
            ServerSocket ss = new ServerSocket(PORT);
            System.out.println("Server is running on port: "+PORT);
            //making instance of current class
            Server ms = new Server();
            while (true) {
                Socket s = ss.accept();// establishes connection
                //accepting a connection and giving it a thread
                Thread t = new Thread(new Responder(s));
                counter++;
                t.start();
                System.out.println("Connection Number "+counter+" Accepted");

            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }





}