package main.networking;

import javafx.application.Platform;
import main.config.ConfigDataManager;
import main.config.UserConfig;
import main.utils.ServerFile;
import main.view.AppController;

import java.io.*;
import java.net.Socket;
import java.util.List;

/**
 * Created by kkossowski on 20.11.2017.
 */
public class BackupWorker {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private UserConfig currentUserConfig;
    private AppController appController;


    public BackupWorker(
            Socket clientSocket, BufferedReader in, PrintWriter out,
            UserConfig currentUserConfig, AppController appController) {
        this.socket = clientSocket;
        this.in = in;
        this.out = out;
        this.currentUserConfig = currentUserConfig;
        this.appController = appController;
    }


    public void runWorker() {
        ServerFile serverFile = null;
        File emptyFile = null;
        String filePath = null;
        String fileVersion = null;
        long fileSize;
        int serverFileIndex;

        try {
            //handle protocol messages
            out.println(ServerMessage.GET_FILE_PATH.name());
            filePath = in.readLine();
            out.println(ServerMessage.GET_FILE_VERSION.name());
            fileVersion = in.readLine();
            out.println(ServerMessage.GET_FILE_SIZE.name());
            fileSize = Long.parseLong(in.readLine());

            //verify whether sent version is already backup
            serverFileIndex = currentUserConfig.findServerFile(filePath);
            if (serverFileIndex != -1){
                //file exists - verify version

                int serverFileVersionIndex = currentUserConfig.findServerFileVersion(serverFileIndex, fileVersion);
                if (serverFileVersionIndex != -1){
                    //version already on server - not download it from client
                    out.println(ServerMessage.FILE_VERSION_EXISTS.name());
                    return;
                }
                else {
                    //create new file version - targetEmptyFile
                    emptyFile = currentUserConfig.addAndCreateNewFileVersion(
                            serverFileIndex,
                            fileVersion
                    );
                }
            }
            else {
                //file not exist - create it
                serverFile = new ServerFile(
                        currentUserConfig.getUsername(),
                        filePath
                );

                //add new file to list in userConfig
                serverFileIndex = currentUserConfig.addServerFile(serverFile);

                //save will be done when version will be created and saved

                //create new file version - targetEmptyFile
                emptyFile = currentUserConfig.addAndCreateNewFileVersion(
                        serverFileIndex,
                        fileVersion
                );
            }

            //send content request
            out.println(ServerMessage.GET_FILE_CONTENT.name());

            //create proper input stream
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());

            try(FileOutputStream outputStream = new FileOutputStream(emptyFile)) {

                //create variables
                byte[] buffer = new byte[main.config.Properties.bufferSize];
                int numberOfReadBytes;
                long bytesToRead = fileSize;
                long bytesRead = 0;

                while (bytesToRead > 0) {
                    if ((numberOfReadBytes = inputStream.read(buffer, 0, (int) Math.min(bytesToRead, buffer.length))) > 0) {

                        outputStream.write(buffer,0,numberOfReadBytes);
                        bytesToRead -= numberOfReadBytes;
                        bytesRead += numberOfReadBytes;
                    }
                }
            }//close file stream
            //verify whether server read all data
            String message = in.readLine().trim();
            if (message.equals(ClientMessage.BACKUP_FILE_FINISHED.name())){
                //save changes
                ConfigDataManager.createUserConfig(currentUserConfig);

                //show log
                StringBuilder sb = new StringBuilder();
                sb.append("File created: ");
                sb.append("filePath: ");
                sb.append(filePath);
                sb.append(" version: ");
                sb.append(fileVersion);
                Platform.runLater(() -> appController.writeLog(
                        "[" + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + "]" +
                                "[SecondThread-backup] " +
                                sb.toString()
                ));
            }
            else {
                //TO_DO
            }
        } catch (IOException e) {
            //show log
            Platform.runLater(() -> appController.writeLog(
                    "[" + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + "]" +
                            "[SecondThread-backup] "
                            + "connection failed"
            ));

            //remove not completed files
            if (serverFile == null) {
                //serverFile was before process begin

                //delete only version
                currentUserConfig.deleteFileVersion(
                        filePath,
                        fileVersion
                );

                //save new currentUserConfig
                ConfigDataManager.createUserConfig(currentUserConfig);

            } else {
                //serverFile was created

                //remove serverFile with new version and empty folders
                currentUserConfig.deleteServerFile(serverFile.getClientAbsolutePath());

                //save new currentUserConfig
                ConfigDataManager.createUserConfig(currentUserConfig);
            }
            return;
        }
    }
}
