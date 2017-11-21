package main.utils;

import main.config.GlobalConfig;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * Class represents structure of backup file on server
 * Created by kkossowski on 20.11.2017.
 */
public class ServerFile {
    private String username;
    private String serverAbsolutePath;  //folder with all versions
    private String clientAbsolutePath;
    private String fileExtension;       //added to version files
    private ArrayList<String> fileVersions;

    //version format: MM-dd-yyyy_HH-mm-ss

    public ServerFile(String username, String clientAbsolutePath){
        this.username = username;
        this.clientAbsolutePath = clientAbsolutePath;
        this.serverAbsolutePath = generateServerAbsolutePath();
        this.fileExtension = readExtensionFromPath();
        //create file pool (all version files are stored in folder with path serverAbsolutePath)
        new File(this.serverAbsolutePath).mkdirs();
    }

    private String generateServerAbsolutePath(){
        //delete file
        String path = new File(clientAbsolutePath).getParent();

        //get root element
        Path root = new File(clientAbsolutePath).toPath().getRoot();

        //delete root element
        String subfolders = path.replace(root.toString(), "");

        //make first folder name
        String firstFolder = root.toString().substring(0,1);

        //generate final path
        path = GlobalConfig.storagePath +
                username + "/" +
                firstFolder + "/" +
                subfolders;

        return path;
    }
    private String readExtensionFromPath(){
        String path = new File(clientAbsolutePath).getName();
        path = "." + path.split("\\.")[1];
        return path;
    }

    public String getServerAbsolutePath() {
        return serverAbsolutePath;
    }
    public String getClientAbsolutePath() {
        return clientAbsolutePath;

    }
    public String getFileExtension(){
        return fileExtension;
    }

    public ArrayList<String> getFileVersions(){
        return fileVersions;
    }
    public int addFileVersion(String newVersion){
        fileVersions.add(newVersion);
        return fileVersions.size() - 1;
    }
    public void removeFileVersion(String oldVersio){
        fileVersions.remove(oldVersio);
    }

    public String getUsername(){
        return username;
    }
}
