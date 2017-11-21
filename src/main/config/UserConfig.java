package main.config;

import main.utils.ServerFile;

import java.io.File;
import java.io.IOException;
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

    public int addServerFile(ServerFile newServerFile){
        this.serverFiles.add(newServerFile);
        return this.serverFiles.size() - 1;
    }
    public void deleteServerFile(String clientAbsolutePath){
        //find correct ServerFile
        int index;
        index = findServerFile(clientAbsolutePath);
        //delete correct ServerFile
        if (index != -1)
            serverFiles.remove(index);
    }

    public File addAndCreateNewFileVersion(int serverFileIndex, String fileVersion){
        //add version to list
        this.serverFiles.get(serverFileIndex).addFileVersion(fileVersion);

        //define file path
        File newFile = new File(
                serverFiles.get(serverFileIndex).getServerAbsolutePath()
                        + "/"
                        + fileVersion
                        + serverFiles.get(serverFileIndex).getFileExtension()
        );
        //create empty file
        try {
            newFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newFile;
    }

    public int findServerFile(String clientAbsolutePath){
        int index = -1;
        for(int i =0; i < serverFiles.size(); i++){
            if (serverFiles.get(i).getClientAbsolutePath().equals(clientAbsolutePath)){
                index = 1;
                break;
            }
        }
        return index;
    }
    public int findServerFileVersion(int serverFileIndex, String version){
        int index = -1;
        ServerFile serverFile = serverFiles.get(serverFileIndex);
        for (int i = 0; i< serverFile.getFileVersions().size(); i++){
            if (serverFile.getFileVersions().get(i).equals(version))
                index = i;
        }
        return index;
    }


}
