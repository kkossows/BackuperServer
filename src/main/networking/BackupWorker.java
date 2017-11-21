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
public class BackupWorker implements Runnable{

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private UserConfig currentUserConfig;
    private AppController appController;

    public BackupWorker(
            Socket clientSocket, BufferedReader inputStream, PrintWriter outputStream,
            UserConfig currentUserConfig, AppController appController) {
        this.socket = clientSocket;
        this.in = inputStream;
        this.out = outputStream;
        this.currentUserConfig = currentUserConfig;
        this.appController = appController;
    }


    @Override
    public void run() {
        File emptyFile;
        String filePath;
        String fileVersion;
        long fileSize;

        try {
            out.println(ServerMessage.GET_FILE_PATH.name());
            filePath = in.readLine();
            out.println(ServerMessage.GET_FILE_VERSION.name());
            fileVersion = in.readLine();
            out.println(ServerMessage.GET_FILE_SIZE.name());
            fileSize = Long.parseLong(in.readLine());


            //verify whether sent version is already backup
            int serverFileIndex = currentUserConfig.findServerFile(filePath);
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

                    //send content request
                    out.println(ServerMessage.GET_FILE_CONTENT.name());
                }
            }
            else {
                //file not exist - create it
                ServerFile newServerFile = new ServerFile(
                        currentUserConfig.getUsername(),
                        filePath
                );

                //add new file to list in userConfig
                serverFileIndex = currentUserConfig.addServerFile(newServerFile);

                //create new file version - targetEmptyFile
                emptyFile = currentUserConfig.addAndCreateNewFileVersion(
                        serverFileIndex,
                        fileVersion
                );
            }

            //create streams
            try(DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                FileOutputStream outputStream = new FileOutputStream(emptyFile)) {

                //create variables
                byte[] buffer = new byte[main.config.Properties.bufferSize];
                int numberOfReadBytes;
                long bytesToRead = fileSize;
                long bytesRead = 0;

                while (bytesToRead > 0) {
                    if ((numberOfReadBytes = inputStream.read(buffer, 0, (int) Math.min(bytesToRead, buffer.length))) > 0) {

                        outputStream.write(buffer);
                        bytesToRead -= numberOfReadBytes;
                        bytesRead += numberOfReadBytes;
                    }
                }
            }//close streams
            //verify whether server read all data
            if (in.readLine().equals(ClientMessage.BACKUP_FILE_FINISHED.name())){
                //save changes
                ConfigDataManager.createUserConfig(currentUserConfig);

                //show log
                Platform.runLater(() -> appController.writeLog(
                        "File created: "
                                + "filePath: " + filePath + " "
                                + "version: " + fileVersion
                ));
            }
            else {
                //TO_DO
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
