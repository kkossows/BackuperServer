package main.config;

import main.utils.ServerFile;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Class represents one User in service - each user has own config file.
 * (if we would like to make normal users and super users, this kind of class is useful)
 * Created by kkossowski on 20.11.2017.
 */
public class UserConfig implements Serializable{

    private String username;
    //variable needed to not allow login to one account on multiple sockets at ones
    private boolean isAlreadyLogin;
    private File storageFolder;
    private ArrayList<ServerFile> serverFiles;

    public UserConfig(String username, File storageFolder){
        this.username = username;
        this.storageFolder = storageFolder;
        this.isAlreadyLogin = false;
        this.serverFiles = new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }
    public File getStorageFolder() {
        return storageFolder;
    }

    public boolean isAlreadyLogin() {
        return isAlreadyLogin;
    }
    public void setAlreadyLogin(boolean alreadyLogin) {
        isAlreadyLogin = alreadyLogin;
    }

    public ArrayList<ServerFile> getServerFiles() {
        return serverFiles;
    }
    public void setServerFiles(ArrayList<ServerFile> serverFiles) {
        this.serverFiles = serverFiles;
    }
}
