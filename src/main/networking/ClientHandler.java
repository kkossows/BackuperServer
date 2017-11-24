package main.networking;

import javafx.application.Platform;
import main.config.*;
import main.utils.ServerFile;
import main.view.AppController;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * Created by kkossowski on 20.11.2017.
 */
public class ClientHandler implements Runnable {
    public static int numberOfActiveUsers = 0;

    private Socket clientSocket;
    private int clientPortNumber;
    private String clientIpAddress;
    private BufferedReader in;
    private PrintWriter out;
    private AppController appController;
    private UserConfig currentUserConfig;
    private boolean isConnectionAlive;

    public ClientHandler(Socket clientSocket, AppController appController) {
        this.clientSocket = clientSocket;
        this.appController = appController;

        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.clientPortNumber = clientSocket.getPort();
        this.clientIpAddress = clientSocket.getInetAddress().getHostAddress();
    }

    @Override
    public void run() {
        isConnectionAlive = true;
        ClientMessage message = null;
        while (isConnectionAlive) {

            try {
                message = getClientMessage();
            } catch (SocketException e) {
                System.out.println("tut");
                //if server closed socket
                isConnectionAlive = false;
            }


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
            } else {
                //if server receive null from readLine, it means that client close the connection socket
                if (isConnectionAlive) {
                    //end thread
                    isConnectionAlive = false;
                    //disable active client handler
                    ServerWorker.setClientHandlerDeactivation(this);
                    //close the connection
                    closeConnection();
                }
            }
        }
    }

    /**
     * Method get message from input stream and convert it to protocol message.
     * If there was no protocol message, return null;
     * Method used only when in main method.
     *
     * @return
     */
    private ClientMessage getClientMessage() throws java.net.SocketException {
        try {
            String message = in.readLine();
            if (message != null) {
                //show log
                Platform.runLater(() -> appController.writeLog(
                        "[" + clientIpAddress + ":" + clientPortNumber + "]" +
                        "#GET message: " + message
                ));

                return ClientMessage.valueOf(message);
            } else {
                //if server receive null from readLine, it means that client close the connection socket
                //show log
                Platform.runLater(() -> appController.writeLog(
                        "[" + clientIpAddress + ":" + clientPortNumber + "]" +
                        "Client closed connection"
                ));
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void handleRegisterMessage() {
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
            ConfigDataManager.createUsersLoginCredentials(usersLoginCredentials);

            if (result) {
                //create new UserConfigFile
                ConfigDataManager.createUserConfig(new UserConfig(
                        username
                ));

                //send final response
                out.println(ServerMessage.USER_CREATED.name());

                //show log
                Platform.runLater(() -> appController.writeLog(
                        "[" + clientIpAddress + ":" + clientPortNumber + "]" +
                        "New user registered: "
                                + "username: " + username + " "
                                + "password: " + password
                ));

            } else {
                out.println(ServerMessage.USER_EXISTS.name());

                //show log
                Platform.runLater(() -> appController.writeLog(
                        "[" + clientIpAddress + ":" + clientPortNumber + "]" +
                        "User exist: "
                                + "username: " + username
                ));
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

                        //show log
                        Platform.runLater(() -> appController.writeLog(
                                "[" + clientIpAddress + ":" + clientPortNumber + "]" +
                                "Login correct: "
                                        + "username: " + username
                        ));

                        //change view counter
                        numberOfActiveUsers++;
                        Platform.runLater(() -> appController.updateNumberOfActiveUsers(
                                numberOfActiveUsers)
                        );

                        out.println(ServerMessage.LOGIN_SUCCESS.name());
                    }
                }
            } else {
                //show log
                Platform.runLater(() -> appController.writeLog(
                        "[" + clientIpAddress + ":" + clientPortNumber + "]" +
                        "Login failed: "
                                + "username: " + username
                ));

                out.println(ServerMessage.LOGIN_FAILED.name());
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

        //show log
        Platform.runLater(() -> appController.writeLog(
                "[" + clientIpAddress + ":" + clientPortNumber + "]" +
                        "#Sending backup files list finished"
        ));
    }

    private void handleGetAllFileVersionsMessage() {
        String targetFilePath;

        try {
            out.println(ServerMessage.GET_FILE_PATH.name());
            targetFilePath = in.readLine();
            out.println(ServerMessage.SENDING_FILE_VERSIONS.name());

            for (ServerFile serverFile : currentUserConfig.getServerFiles()) {
                if (serverFile.getClientAbsolutePath().equals(targetFilePath)) {
                    //server file found

                    for (String version : serverFile.getFileVersions()) {
                        //send version one by one
                        out.println(version);
                    }

                    //finish procedure
                    out.println(ServerMessage.SENDING_FILE_VERSIONS_FINISHED.name());

                    //show log
                    Platform.runLater(() -> appController.writeLog(
                            "[" + clientIpAddress + ":" + clientPortNumber + "]" +
                                    "#Sending file versions finished"
                    ));

                    //leave method
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteUserMessage() {
        if (ConfigDataManager.isUsersLoginCredentialsFileExists()) {
            //delete user from usersLoginCredentialsFile
            UsersLoginCredentials usersLoginCredentials = ConfigDataManager.readUsersLoginCredentials();
            usersLoginCredentials.removeUserCredentials(currentUserConfig.getUsername());
            ConfigDataManager.createUsersLoginCredentials(usersLoginCredentials);

            //delete all data associated with deletedUser
            for (ServerFile serverFileToDelete : currentUserConfig.getServerFiles()) {
                currentUserConfig.deleteServerFile(serverFileToDelete.getServerAbsolutePath());
            }

            //delete userConfigFile
            ConfigDataManager.deleteUserConfig(currentUserConfig);

            //show log
            Platform.runLater(() -> appController.writeLog(
                    "[" + clientIpAddress + ":" + clientPortNumber + "]" +
                    "User deleted: " + "username: " + currentUserConfig.getUsername()
            ));

            //send final response
            out.println(ServerMessage.DELETE_USER_FINISHED.name());
        }
    }

    private void handleLogOutMessage() {
        if (ConfigDataManager.isUserConfigFileExists(currentUserConfig.getUsername())) {
            //change flag
            currentUserConfig.setAlreadyLogin(false);
            //save
            ConfigDataManager.createUserConfig(currentUserConfig);

            //show log
            Platform.runLater(() -> appController.writeLog(
                    "[" + clientIpAddress + ":" + clientPortNumber + "]" +
                    "User logout: " + "username: " + currentUserConfig.getUsername()
            ));

            //send final response
            out.println(ServerMessage.LOG_OUT_FINISHED.name());

            //change view counter
            numberOfActiveUsers--;
            Platform.runLater(() -> appController.updateNumberOfActiveUsers(
                    numberOfActiveUsers)
            );

            //connection will be closed automatically, message will receive null value
        }
    }

    private void handleBackupFileMessage() {
        BackupWorker backupWorker = new BackupWorker(
                clientSocket, in, out,
                currentUserConfig, appController);
//        Thread backupThread = new Thread(backupWorker);
//        backupThread.start();
        backupWorker.run();
    }

    private void handleRestoreFileMessage() {
        RestoreWorker restoreWorker = new RestoreWorker(
                clientSocket, in, out,
                currentUserConfig, appController);
        Thread restoreThread = new Thread(restoreWorker);
        restoreThread.start();
    }

    private void handleRemoveFileMessage() {
        String filePath;

        try {
            out.println(ServerMessage.GET_FILE_PATH.name());
            filePath = in.readLine();

            //delete file
            currentUserConfig.deleteServerFile(filePath);

            //save userConfig
            ConfigDataManager.createUserConfig(currentUserConfig);

            //show log
            Platform.runLater(() -> appController.writeLog(
                    "[" + clientIpAddress + ":" + clientPortNumber + "]" +
                    "File deleted with all versions: " + "filePath: " + filePath
            ));

            //inform about operation success
            out.println(ServerMessage.FILE_REMOVED.name());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRemoveFileVersionMessage() {
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

            //show log
            Platform.runLater(() -> appController.writeLog(
                    "[" + clientIpAddress + ":" + clientPortNumber + "]" +
                    "Version deleted: "
                            + "filePath: " + filePath + " "
                            + "version: " + fileVersion
            ));

            //inform about operation success
            out.println(ServerMessage.FILE_VERSION_REMOVED.name());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            //user may be login and server may close connection
            if (currentUserConfig.isAlreadyLogin()) {
                //change view counter
                numberOfActiveUsers--;
                Platform.runLater(() -> appController.updateNumberOfActiveUsers(
                        numberOfActiveUsers)
                );
            }

            //show log
            Platform.runLater(() -> appController.writeLog(
                    "[" + clientIpAddress + ":" + clientPortNumber + "]" +
                            "Connection closed"
                    )
            );

            //delete from list of active handlers
            ServerWorker.setClientHandlerDeactivation(this);

            //close streams
            out.close();
            in.close();
            clientSocket.close();
        } catch (java.net.SocketException ex) {
            ex.printStackTrace();
            return ;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }
}
