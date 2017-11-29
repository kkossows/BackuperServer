package main.networking;

import com.sun.deploy.config.ClientConfig;
import javafx.application.Platform;
import main.config.*;
import main.utils.ServerFile;
import main.view.AppController;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Random;

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
    private boolean isSecondSocketUsed;

    private Random codeGenerator;
    int authenticationCode;


    public ClientHandler(Socket clientSocket, AppController appController) {
        initialize(clientSocket, appController);
        isSecondSocketUsed = false;
    }

    public ClientHandler(Socket clientSocket, AppController appController, UserConfig currentUserConfig) {
        this.currentUserConfig = currentUserConfig;
        initialize(clientSocket, appController);
        isSecondSocketUsed = true;
    }

    private void initialize(Socket clientSocket, AppController appController) {
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

        this.codeGenerator = new Random();
    }

    @Override
    public void run() {
        isConnectionAlive = true;
        ClientMessage message = null;


        while (isConnectionAlive) {
            try {
                message = getClientMessage();
            }
            catch (SocketException e) {
                //client closed connection
                message = null;
            }
            catch (Exception ex){
                message = null;
            }
            try {
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
                    //end thread
                    isConnectionAlive = false;
                    //close the connection
                    closeConnection();
                }
            }

            catch (IOException e) {
                //during handle procedure
                //show log
                Platform.runLater(() -> appController.writeLog(
                        "[" + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + "]"
                                + "connection failed"
                ));
                return;
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
    private ClientMessage getClientMessage() throws Exception {
        //read line from input stream
        String message = in.readLine();

        if (message != null) {
            //show log
            Platform.runLater(() -> appController.writeLog(
                    "[" + clientIpAddress + ":" + clientPortNumber + "]" +
                            "#GET message: " + message
            ));

            return ClientMessage.valueOf(message);
        } else {
            return null;
        }
    }

    private void handleRegisterMessage() throws IOException {
        String username;
        String password;

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
    }

    private void handleLoginMessage() throws IOException {
        String username;
        String password;

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

                    //send information about result
                    out.println(ServerMessage.LOGIN_SUCCESS.name());

                    //calculate authenticationCode
                    authenticationCode = codeGenerator.nextInt(Properties.codeGeneratorRange);
                    while (ServerWorker.getCodeToClientHandlerMap().containsKey(authenticationCode))
                        authenticationCode = codeGenerator.nextInt(Properties.codeGeneratorRange);

                    //send authenticationCode used in backup and receive process
                    out.println(authenticationCode);

                    //add authenticationCode to list
                    ServerWorker.addAuthenticationCode(authenticationCode, this);
                }
            }
        } else {
            //show log
            Platform.runLater(() -> appController.writeLog(
                    "[" + clientIpAddress + ":" + clientPortNumber + "]" +
                            "Login failed: "
                            + "username: " + username
            ));

            //send information about result
            out.println(ServerMessage.LOGIN_FAILED.name());
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

    private void handleGetAllFileVersionsMessage() throws IOException {
        String targetFilePath;

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
    }

    private void handleDeleteUserMessage() {
        if (ConfigDataManager.isUsersLoginCredentialsFileExists()) {
            //delete user from usersLoginCredentialsFile
            UsersLoginCredentials usersLoginCredentials = ConfigDataManager.readUsersLoginCredentials();
            usersLoginCredentials.removeUserCredentials(currentUserConfig.getUsername());
            ConfigDataManager.createUsersLoginCredentials(usersLoginCredentials);

            //create temp list
            ArrayList<ServerFile> serverFilesToDeleteList = new ArrayList<>(currentUserConfig.getServerFiles());

            //delete all data associated with deletedUser
            for (ServerFile serverFileToDelete : serverFilesToDeleteList) {
                currentUserConfig.deleteServerFile(serverFileToDelete.getClientAbsolutePath());
            }

            //delete userConfigFile
            ConfigDataManager.deleteUserConfig(currentUserConfig);

            //delete empty user folder
            currentUserConfig.getStoragePool().delete();

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

            //remove authenticationCode from map
            ServerWorker.removeAuthenticationCode(authenticationCode, this);

            //connection will be closed automatically, message will receive null value
        }
    }

    private void handleBackupFileMessage() {
        BackupWorker backupWorker = new BackupWorker(
                clientSocket, in, out,
                currentUserConfig, appController);
        backupWorker.runWorker();
    }

    private void handleRestoreFileMessage() {
        RestoreWorker restoreWorker = new RestoreWorker(
                clientSocket, in, out,
                currentUserConfig, appController);
        restoreWorker.runWorker();
    }

    private void handleRemoveFileMessage() throws IOException {
        String filePath;

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
    }

    private void handleRemoveFileVersionMessage() throws IOException {
        String filePath;
        String fileVersion;

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
    }


    public void closeConnection() {
        //user may be login and server may close connection
        if (currentUserConfig != null
                && currentUserConfig.isAlreadyLogin()
                && !isSecondSocketUsed) {
            //change view counter
            numberOfActiveUsers--;
            Platform.runLater(() -> appController.updateNumberOfActiveUsers(
                    numberOfActiveUsers)
            );

            currentUserConfig.setAlreadyLogin(false);
            ConfigDataManager.createUserConfig(currentUserConfig);
        }

        //show log
        Platform.runLater(() -> appController.writeLog(
                "[" + clientIpAddress + ":" + clientPortNumber + "]" +
                        "Connection closed"
                )
        );

        //delete from list of active handlers
        ServerWorker.setClientHandlerDeactivation(this);

        //delete thread
        ServerWorker.setThreadInactive(Thread.currentThread());

        try {
            //close streams
            out.close();
            in.close();
            clientSocket.close();
        } catch (Exception e) {
            return;
        }
    }

    public UserConfig getCurrentUserConfig(){
        return currentUserConfig;
    }
}
