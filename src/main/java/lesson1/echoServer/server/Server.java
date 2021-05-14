package lesson1.echoServer.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    public Server(){

        ExecutorService service = Executors.newFixedThreadPool(4);


        try (ServerSocket server = new ServerSocket(6789)){

            while (true){
                Socket socket = server.accept();
                service.execute(new ClientHandler(socket));
                System.out.println("Client accepted - "+ socket);
            }



        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        new Server();
    }
}
