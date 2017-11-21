package main.networking;

import main.config.*;
import main.utils.ServerFile;
import main.view.AppController;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by kkossowski on 20.11.2017.
 */
public class ClientHandler implements Runnable{
    public static int numberOfActiveUsers = 0;

    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private AppController appController;
    private UserConfig currentUserConfig;

    public ClientHandler(Socket clientSocket, AppController appController){
        this.clientSocket = clientSocket;
        this.appController = appController;

        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while(true) {
            ClientMessage message = getClientMessage();
            if (message != null) {
                switch (message) {
                    case REGISTER:
                        handleRegisterMessage();
                        break;

                    case LOG_IN:
                        handleLoginMessage();
                        break;

                    case GET_BACKUP_FILES_LIST:
                        handleGetBackupFilesListMessage();
                        break;

                    case GET_ALL_FILE_VERSIONS:
                        handleGetAllFileVersionsMessage();
                        break;

                    case DELETE_USER:
                        handleDeleteUserMessage();
                        break;

                    case LOG_OUT:
                        handleLogOutMessage();
                        break;

                    case BACKUP_FILE:
                        handleBackupFileMessage();
                        break;

                    case RESTORE_FILE:
                        handleRestoreFileMessage();
                        break;

                    case REMOVE_FILE:
                        handleRemoveFileMessage();
                        break;

                    case REMOVE_FILE_VERSION:
                        handleRemoveFileVersionMessage();
                        break;

                    default:
                        break;
                }
            }
        }
    }

    /**
     * Method get message from input stream and convert it to protocol message.
     * If there was no protocol message, return null;
     * Methode used only when in main method.
     * @return
     */
    private ClientMessage getClientMessage(){
        try {
            String message = in.readLine();
            return ClientMessage.valueOf(message);
        }
        catch (IllegalArgumentException e) {
            return null;
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void handleRegisterMessage(){
        String username;
        String password;

        try {
            out.println(ServerMessage.GET_USERNAME.name());
            username = in.readLine();
            out.println(ServerMessage.GET_PASSWORD.name());
            password = in.readLine();

            //create userCredentials
            UserCredentials newUserCredentials = new UserCredentials(
                    username,
                    password
            );

            //verify whether usersLoginCredentials file exists - if not, create empty one
            if (!ConfigDataManager.isUsersLoginCredentialsFileExists()) {
                ConfigDataManager.createUsersLoginCredentials(
                        new UsersLoginCredentials(
                                new ArrayList<>()
                        )
                );
            }

            //add new userCredentials
            boolean result;
            UsersLoginCredentials usersLoginCredentials = ConfigDataManager.readUsersLoginCredentials();
            result = usersLoginCredentials.addUserCredentials(newUserCredentials);

            if (result) {
                //create new UserConfigFile
                ConfigDataManager.createUserConfig(new UserConfig(
                        username
                ));

                //send final response
                out.println(ServerMessage.USER_CREATED.name());
            } else {
                out.println(ServerMessage.USER_EXISTS.name());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void handleLoginMessage() {
        String username;
        String password;

        try {
            out.println(ServerMessage.GET_USERNAME.name());
            username = in.readLine();
            out.println(ServerMessage.GET_PASSWORD.name());
            password = in.readLine();

            //verify whether usersLoginCredentials file exists - if not, create empty one
            if (!ConfigDataManager.isUsersLoginCredentialsFileExists()) {
                ConfigDataManager.createUsersLoginCredentials(
                        new UsersLoginCredentials(
                                new ArrayList<>()
                        )
                );
            }

            //authenticate user
            boolean areCredentialsCorrect;
            UsersLoginCredentials usersCredentials = ConfigDataManager.readUsersLoginCredentials();
            areCredentialsCorrect = usersCredentials.authenticateUserCredentials(
                    new UserCredentials(
                            username,
                            password
                    )
            );

            if (areCredentialsCorrect) {
                //get user configuration
                if (ConfigDataManager.isUserConfigFileExists(username)) {
                    currentUserConfig = ConfigDataManager.readUserConfig(username);

                    if (currentUserConfig.isAlreadyLogin()) {
                        //user is already login, only one session in one time available
                        out.println(ServerMessage.LOGIN_FAILED.name());
                    } else {
                        //change flag in user to true
                        currentUserConfig.setAlreadyLogin(true);

                        //save change
                        // - if user would like to login from many sessions at once, it will be detected and rejected
                        ConfigDataManager.createUserConfig(currentUserConfig);

                        //change view counter
                        numberOfActiveUsers++;
                        appController.updateNumberOfActiveUsers(numberOfActiveUsers);

                        out.println(ServerMessage.LOGIN_SUCCESS.name());
                    }
                } else {
                    out.println(ServerMessage.LOGIN_FAILED.name());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void handleGetBackupFilesListMessage() {
        out.println(ServerMessage.SENDING_BACKUP_FILES_LIST.name());
        for (ServerFile serverFile : currentUserConfig.getServerFiles()) {
            out.println(serverFile.getClientAbsolutePath());
        }
        out.println(ServerMessage.SENDING_BACKUP_FILES_LIST_FINISHED.name());
    }
    private void handleGetAllFileVersionsMessage(){
        String targetFilePath;

        try{
            out.println(ServerMessage.GET_FILE_PATH.name());
            targetFilePath = in.readLine();
            out.println(ServerMessage.SENDING_FILE_VERSIONS.name());

            for (ServerFile serverFile : currentUserConfig.getServerFiles()) {
                if (serverFile.getClientAbsolutePath().equals(targetFilePath)){
                    //server file found

                    for (String version : serverFile.getFileVersions()){
                        //send version one by one
                        out.println(version);
                    }

                    //finish procedure
                    out.println(ServerMessage.SENDING_FILE_VERSIONS_FINISHED.name());

                    //leave method
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void handleDeleteUserMessage() {
        if(ConfigDataManager.isUsersLoginCredentialsFileExists()) {
            //delete user from usersLoginCredentialsFile
            UsersLoginCredentials usersLoginCredentials = ConfigDataManager.readUsersLoginCredentials();
            usersLoginCredentials.removeUserCredentials(currentUserConfig.getUsername());
            ConfigDataManager.createUsersLoginCredentials(usersLoginCredentials);

            //delete all data associated with deletedUser
            for (ServerFile serverFileToDelete : currentUserConfig.getServerFiles()){
                currentUserConfig.deleteServerFile(serverFileToDelete.getServerAbsolutePath());
            }

            //delete userConfigFile
            ConfigDataManager.deleteUserConfig(currentUserConfig);

            //send final response
            out.println(ServerMessage.DELETE_USER_FINISHED.name());
        }
    }
    private void handleLogOutMessage(){
        if (ConfigDataManager.isUserConfigFileExists(currentUserConfig.getUsername())){
            //change flag
            UserConfig userConfig = ConfigDataManager.readUserConfig(currentUserConfig.getUsername());
            userConfig.setAlreadyLogin(false);

            //save
            ConfigDataManager.createUserConfig(userConfig);

            //change number on display view
            numberOfActiveUsers--;
            appController.updateNumberOfActiveUsers(numberOfActiveUsers);

            //send final response
            out.println(ServerMessage.LOG_OUT_FINISHED.name());

            closeConnection();
        }
    }
    private void handleBackupFileMessage(){
        BackupWorker backupWorker = new BackupWorker(
                clientSocket, in, out,
                currentUserConfig, appController );
        Thread backupThread = new Thread(backupWorker);
        backupThread.start();
    }
    private void handleRestoreFileMessage(){
        RestoreWorker restoreWorker = new RestoreWorker(
                clientSocket, in, out,
                currentUserConfig, appController );
        Thread restoreThread = new Thread(restoreWorker);
        restoreThread.start();
    }
    private void handleRemoveFileMessage(){
        String filePath;

        try {
            out.println(ServerMessage.GET_FILE_PATH.name());
            filePath = in.readLine();

            //delete file
            currentUserConfig.deleteServerFile(filePath);

            //save userConfig
            ConfigDataManager.createUserConfig(currentUserConfig);

            //inform about operation success
            out.println(ServerMessage.FILE_REMOVED.name());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void handleRemoveFileVersionMessage(){
        String filePath;
        String fileVersion;

        try {
            out.println(ServerMessage.GET_FILE_PATH.name());
            filePath = in.readLine();

            out.println(ServerMessage.GET_FILE_VERSION.name());
            fileVersion = in.readLine();

            //delete file
            currentUserConfig.deleteFileVersion(filePath, fileVersion);

            //save userConfig
            ConfigDataManager.createUserConfig(currentUserConfig);

            //inform about operation success
            out.println(ServerMessage.FILE_VERSION_REMOVED.name());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeConnection(){
        try {
            out.close();
            in.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
