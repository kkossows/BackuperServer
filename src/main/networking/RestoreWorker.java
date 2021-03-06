package main.networking;

import javafx.application.Platform;
import main.config.UserConfig;
import main.view.AppController;

import java.io.*;
import java.net.Socket;

/**
 * Created by kkossowski on 20.11.2017.
 */
public class RestoreWorker {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private UserConfig currentUserConfig;
    private AppController appController;

    public RestoreWorker(
            Socket clientSocket, BufferedReader in, PrintWriter out,
            UserConfig currentUserConfig, AppController appController) {

        this.socket = clientSocket;
        this.in = in;
        this.out = out;
        this.currentUserConfig = currentUserConfig;
        this.appController = appController;
    }

    public void runWorker() {
        String filePath;
        String fileVersion;

        try {
            //handle protocol messages
            out.println(ServerMessage.GET_FILE_PATH.name());
            filePath = in.readLine();
            out.println(ServerMessage.GET_FILE_VERSION.name());
            fileVersion = in.readLine();

            //find target File
            int serverFileIndex = -1;
            int serverFileVersionIndex = -1;

            serverFileIndex = currentUserConfig.findServerFile(filePath);
            if (serverFileIndex != -1) {
                serverFileVersionIndex = currentUserConfig.findServerFileVersion(
                        serverFileIndex,
                        fileVersion
                );

                if (serverFileVersionIndex != -1) {
                    //target file and file version exist, transfer data

                    File targetFile = currentUserConfig.getFileVersion(serverFileIndex, fileVersion);
                    long fileSize = targetFile.length();

                    //inform client about transferring data
                    out.println(ServerMessage.SENDING_FILE_SIZE.name());
                    out.println(fileSize);
                    out.println(ServerMessage.SENDING_FILE.name());

                    //create proper output stream
                    DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

                    try (FileInputStream inputStream = new FileInputStream(targetFile)) {

                        byte[] buffer = new byte[main.config.Properties.bufferSize];
                        int numberOfReadBytes = 0;
                        long bytesToSend = fileSize;
                        long bytesSent = 0;

                        //-1 if there is no more data because the end of the file has been reached
                        while (bytesToSend > 0
                                && (numberOfReadBytes = inputStream.read(buffer, 0, (int) Math.min(buffer.length, bytesToSend))) > 0) {

                            outputStream.write(buffer,0,numberOfReadBytes);
                            bytesToSend -= numberOfReadBytes;
                            bytesSent += numberOfReadBytes;
                        }
                    }//close file stream

                    //inform client about transfer finished
                    out.println(ServerMessage.SENDING_FILE_FINISHED.name());

                    //show log
                    Platform.runLater(() -> appController.writeLog(
                            "[" + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + "]" +
                                    "[SecondThread-restore] "
                                    + "File sent to client: "
                                    + "filePath: " + filePath + " "
                                    + "version: " + fileVersion
                    ));

                    //close connection (socket will be closed if stream close)
                    outputStream.close();
                } else {
                    //target file version not found
                    //TO-DO
                }
            }
        } catch (IOException e) {
            //show log
            Platform.runLater(() -> appController.writeLog(
                    "[" + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + "]" +
                            "[SecondThread-restore] "
                            + "connection failed"
            ));
            return;
        }
    }
}
