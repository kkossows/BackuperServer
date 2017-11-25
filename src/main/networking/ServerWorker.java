package main.networking;


import javafx.application.Platform;
import main.view.AppController;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Worker responsible for making clientWorkers when connection arrive.
 * Created by kkossowski on 20.11.2017.
 */
public class ServerWorker implements Runnable{

    private String ipAddress;
    private int portNumber;
    private AppController appController;
    private ServerSocket serverSocket;
    private static ArrayList<ClientHandler> clientsHandlers = new ArrayList<>();
    private static HashMap<Integer, ClientHandler> codeToClientHandlerMap = new HashMap<Integer, ClientHandler>();

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
                //accept client connection
                Socket clientSocket = serverSocket.accept();

                //run method will be executed from javafx thread
                Platform.runLater(() -> appController.writeLog(
                        "New connection: "
                                + clientSocket.getInetAddress().getHostAddress() + ":"
                                + clientSocket.getPort()
                ));

                if (clientSocket.isConnected()){
                    //create streams
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                    //handle init procedure
                    String message = in.readLine();

                    //show log
                    Platform.runLater(() -> appController.writeLog(
                            "[" + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + "]" +
                                    "#SERVER_GET message: " + message
                    ));

                    if (message.equals(ClientMessage.INIT.name())) {
                        //new user
                        out.println(ServerMessage.INIT_CORRECT.name());

                        //create new clientHandler
                        ClientHandler newClientHandler = new ClientHandler(
                                clientSocket,
                                appController
                        );
                        clientsHandlers.add(newClientHandler);

                        //run new thread for new client
                        Thread newClientThread = new Thread(newClientHandler);
                        newClientThread.start();
                    }
                    else if (message.equals(ClientMessage.INIT_WITH_CODE.name())){
                        //user used authenticationCode
                        out.println(ServerMessage.GET_CODE.name());
                        int receivedCode = Integer.parseInt(in.readLine());

                        //verify code
                        if (codeToClientHandlerMap.containsKey(receivedCode)){
                            out.println(ServerMessage.INIT_CORRECT.name());

                            //create new ClientHandler with same currentClientConfig
                            ClientHandler newClientHandler = new ClientHandler(
                                    clientSocket,
                                    appController,
                                    codeToClientHandlerMap.get(receivedCode).getCurrentUserConfig()
                            );
                            clientsHandlers.add(newClientHandler);

                            //run new thread
                            Thread newClientThread = new Thread(newClientHandler);
                            newClientThread.start();

                        } else {
                            out.println(ServerMessage.EXIT.name());
                        }
                    } else {
                        out.println(ServerMessage.EXIT.name());
                    }
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

    public static void addAuthenticationCode(int authenticationCode, ClientHandler clientHandler){
        codeToClientHandlerMap.put(authenticationCode, clientHandler);
    }

    public static void removeAuthenticationCode(int authenticationCode, ClientHandler clientHandler){
        codeToClientHandlerMap.remove(authenticationCode, clientHandler);
    }

    public static HashMap<Integer, ClientHandler> getCodeToClientHandlerMap(){
        return codeToClientHandlerMap;
    }
}
