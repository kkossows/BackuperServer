package main.utils;

import java.util.ArrayList;

/**
 * Class represents structure of backup file on server
 * Created by kkossowski on 20.11.2017.
 */
public class ServerFile {
    private String clientFileName;
    private String serverAbsolutePath;
    private String clientAbsolutePath;
    private ArrayList<String> fileVersions;

    //version format: MM-dd-yyyy_HH-mm-ss

    public ServerFile(String serverAbsolutePath, String clientAbsolutePath){
        this.serverAbsolutePath = serverAbsolutePath;
        this.clientAbsolutePath = clientAbsolutePath;
    }

    public String getServerAbsolutePath() {
        return serverAbsolutePath;
    }
    public String getClientAbsolutePath() {
        return clientAbsolutePath;
    }
    public ArrayList<String> getFileVersions(){
        return fileVersions;
    }
}
