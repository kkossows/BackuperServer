package main.networking;

import java.net.Socket;

/**
 * Created by kkossowski on 20.11.2017.
 */
public class ClientHandler implements Runnable{

    private Socket clientSocket;


    public ClientHandler(Socket clientSocket){
        this.clientSocket = clientSocket;
    }


    @Override
    public void run() {
        initializeUser();
    }

    private void initializeUser(){

    }
}
