package main.networking;

import com.sun.xml.internal.bind.v2.model.annotation.RuntimeAnnotationReader;
import main.view.AppController;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Worker responsible for making clientWorkers when connection arrive.
 * Created by kkossowski on 20.11.2017.
 */
public class ServerWorker implements Runnable{

    private String ipAddress;
    private int portNumber;
    private AppController appController;


    public ServerWorker(String ipAddress, int portNumber, AppController appController){
        this.ipAddress = ipAddress;
        this.portNumber = portNumber;
        this.appController = appController;
    }


    @Override
    public void run() {
        //create server listening socket
        ServerSocket serverSocket = null;
        try {
            InetAddress serverIpAddress = InetAddress.getByName(ipAddress);
            serverSocket = new ServerSocket(portNumber, 10, serverIpAddress);
            appController.writeLog(
                    "Server listening: " + ipAddress + ":" + portNumber
            );

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        while(true) {
            try {
                Socket clientSocket = serverSocket.accept();
                appController.writeLog(
                        "Connection with client established"
                );
                if (clientSocket.isConnected()){
                    ClientHandler newClientHandler = new ClientHandler(
                            clientSocket,
                            appController
                    );
                    Thread newClientThread = new Thread(newClientHandler);
                    newClientThread.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
