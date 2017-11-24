package main.networking;

import com.sun.xml.internal.bind.v2.model.annotation.RuntimeAnnotationReader;
import javafx.application.Platform;
import main.view.AppController;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Worker responsible for making clientWorkers when connection arrive.
 * Created by kkossowski on 20.11.2017.
 */
public class ServerWorker implements Runnable{

    private String ipAddress;
    private int portNumber;
    private AppController appController;
    ServerSocket serverSocket;
    private static ArrayList<ClientHandler> clientsHandlers = new ArrayList<>();


    public ServerWorker(String ipAddress, int portNumber, AppController appController){
        this.ipAddress = ipAddress;
        this.portNumber = portNumber;
        this.appController = appController;
    }


    @Override
    public void run() {
        try {
            //create server listening socket
            InetAddress serverIpAddress = InetAddress.getByName(ipAddress);
            serverSocket = new ServerSocket(portNumber, 10, serverIpAddress);

            //run method will be executed from javafx thread
            Platform.runLater(() -> appController.writeLog(
                    "Server listening: " + ipAddress + ":" + portNumber
            ));


        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        while(true) {
            try {
                Socket clientSocket = serverSocket.accept();

                //run method will be executed from javafx thread
                Platform.runLater(() -> appController.writeLog(
                        "New connection: "
                                + clientSocket.getInetAddress().getHostAddress() + ":"
                                + clientSocket.getPort()
                ));

                if (clientSocket.isConnected()){
                    ClientHandler newClientHandler = new ClientHandler(
                            clientSocket,
                            appController
                    );
                    clientsHandlers.add(newClientHandler);
                    Thread newClientThread = new Thread(newClientHandler);
                    newClientThread.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public void closeServerSocket(){
        try {
            //close all active clients connections
            for (ClientHandler clientHandler : clientsHandlers){
                clientHandler.closeConnection();
            }
            clientsHandlers.clear();

            //close server socket
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    public static void setClientHandlerDeactivation(ClientHandler clientsHandler){
        clientsHandlers.remove(clientsHandler);
    }
}
