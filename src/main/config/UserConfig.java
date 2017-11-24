package main.config;

import main.utils.ServerFile;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Class represents one User in service - each user has own config file.
 * (if we would like to make normal users and super users, this kind of class is useful)
 *
 *
 *    e.g
 *    globalConfig.getStoragePath :   D:/dane
 *    storagePool                     D:/dane/user_1/
 *    serverAbsolutePath              D:/dane/user_1/C/Documents/doc_1
 *    file path                       D:/dane/user_1/C/Documents/doc_1/10-10-2015_12-12-40.txt
 *
 *    globalConfigFile                <..>/APPDATA//backuper-server//globalConfig.dat
 *    usersLoginCredentials           <..>/APPDATA//backuper-server//usersConfig.dat
 *    userConfigFile:                 <..>//APPDATA//backuper-server//user_1.dat
 *
 * Created by kkossowski on 20.11.2017.
 */
public class UserConfig implements Serializable{

    private String username;
    //variable needed to not allow login to one account on multiple sockets at ones
    private boolean isAlreadyLogin;
    private File storagePool;
    private File userConfigFile;
    private ArrayList<ServerFile> serverFiles;

    public UserConfig(String username){
        this.username = username;

        this.userConfigFile = new File(Properties.appDataDir + username + ".dat");
        this.storagePool = new File(GlobalConfig.storagePath + "/" + username);
        this.isAlreadyLogin = false;
        this.serverFiles = new ArrayList<>();

        //create folder storagePool and empty file uerConfigFile;
        try {
            userConfigFile.createNewFile();
            storagePool.mkdir();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String getUsername() {
        return username;
    }
    public File getUserConfigFile(){
        return userConfigFile;
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

    public int addServerFile(ServerFile newServerFile){
        this.serverFiles.add(newServerFile);
        //return index od new ServerFile in serverFiles list
        return this.serverFiles.size() - 1;
    }
    public void deleteServerFile(String clientAbsolutePath){
        //find correct ServerFile
        int serverFileIndex = findServerFile(clientAbsolutePath);
        String serverFileAbsolutePath = serverFiles.get(serverFileIndex).getServerAbsolutePath();
        String fileExtension = serverFiles.get(serverFileIndex).getFileExtension();

        //delete correct ServerFile
        if (serverFileIndex != -1) {
            //delete all versions from server storage pool
            for(String version : serverFiles.get(serverFileIndex).getFileVersions()){
                //define file path
                File newFile = new File(
                        serverFileAbsolutePath
                                + "/"
                                + version + fileExtension
                );
                //delete physical data
                if (newFile.exists())
                    newFile.delete();

                //no need to remove it from list, list will not be used any more
            }

            //delete folder structure connected to that file
            removeEmptyFolderLoop(
                    new File (serverFileAbsolutePath),
                    storagePool.getAbsolutePath()
            );

            //delete serverFile from list
            serverFiles.remove(serverFileIndex);
        }
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
    public File getFileVersion(int serverFileIndex, String fileVersion){
        //define file path
        File targetFile = new File(
                serverFiles.get(serverFileIndex).getServerAbsolutePath()
                        + "/"
                        + fileVersion
                        + serverFiles.get(serverFileIndex).getFileExtension()
        );
        return targetFile;
    }
    public void deleteFileVersion(String clientAbsolutePath, String versionToDelete){
        //find correct ServerFile
        int serverFileIndex = findServerFile(clientAbsolutePath);
        String serverFileAbsolutePath = serverFiles.get(serverFileIndex).getServerAbsolutePath();
        String fileExtension = serverFiles.get(serverFileIndex).getFileExtension();

        for(String version : serverFiles.get(serverFileIndex).getFileVersions()){
            if (version.equals(versionToDelete)) {
                //define file path
                File newFile = new File(
                        serverFileAbsolutePath
                                + "/"
                                + version + fileExtension
                );
                //delete physical data
                if (newFile.exists())
                    newFile.delete();

                //delete version from list
                serverFiles.get(serverFileIndex).removeFileVersion(version);
            }
        }
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

    /**
     * Method use path.getParentFile() repeatedly to get all components of a path.
     * Remove only empty folders.
     * Method require that startFolder will be alone in tree structure and we can delete it without verifications.
     * @param startFolder
     * @param startFolder - we do not want to clear all folders, it is username folder storage
     * @return
     */
    private void removeEmptyFolderLoop(File startFolder, String finishFolder){
        String parentFolder = startFolder.getName();
        File lowerFolder = startFolder.getParentFile();

        if(startFolder.getName().equals(finishFolder)){
            return;

        } else {
            //remove Parent Folder
            startFolder.delete();

            //verify whether lower folder is alone in structure
            if (lowerFolder.listFiles().length > 0){
                //lower Folder is used by different ServerFile
                return;

            } else{
                //lower folder is not used by any other ServerFile
                removeEmptyFolderLoop(lowerFolder, finishFolder);
            }
        }
    }
}
