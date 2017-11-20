package main.view;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import main.config.ConfigDataManager;
import main.config.GlobalConfig;
import main.networking.ServerWorker;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by kkossowski on 20.11.2017.
 */
public class AppController implements Initializable {

    //-------------------FXML variables
    @FXML
    private Label lb_folderPath;
    @FXML
    private TextField tx_serverIpAddress;
    @FXML
    private TextField tx_serverPortNumber;

    @FXML
    private Button btn_startServer;
    @FXML
    private Button btn_stopServer;
    @FXML
    private Button btn_chooseFolder;
    @FXML
    private Button btn_quit;

    @FXML
    private TextArea ta_logs;


    //-------------------Other variables
    String noDirectorySelectedText = "No Directory selected";


    //-------------------FXML methods
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //if global config file not exist
        // - create it with default variables
        if (!ConfigDataManager.isGlobalConfigFileExists()) {
            ConfigDataManager.createGlobalConfig(new GlobalConfig());
        }

        //fulfill variables with config of default values
        GlobalConfig globalConfig = ConfigDataManager.readGlobalConfig();
        if (globalConfig.areConfigVariablesDifferentThanDefault()) {
            tx_serverIpAddress.setText(globalConfig.getSavedServerIpAddress());
            tx_serverPortNumber.setText(Integer.toString(globalConfig.getSavedServerPortNumber()));
        } else {
            tx_serverIpAddress.setText(globalConfig.getDefaultServerIpAddress());
            tx_serverPortNumber.setText(Integer.toString(globalConfig.getDefaultServerPortNumber()));
        }

        //fulfill storagePath - if empty in globalConfig, show first application launch information to user.
        if (globalConfig.isStoragePathChoosen()){
            lb_folderPath.setText(globalConfig.getSavedStoragePath());
        }
        else{
            showInformationDialog(
                    "First application launch information",
                    "1) Please, choose folder for application storage pool."
                            + "2) Please, change default ip address and port number if are improper for you."
            );
            lb_folderPath.setText(noDirectorySelectedText);
        }
    }

    @FXML
    void btn_chooseFolder_OnClick(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose directory for application storage pool");
        File selectedDirectory =
                directoryChooser.showDialog((Stage) ((Node) event.getSource()).getScene().getWindow());

        if(selectedDirectory == null){
            lb_folderPath.setText(noDirectorySelectedText);
        }else{
            lb_folderPath.setText(selectedDirectory.getAbsolutePath());
        }
    }

    @FXML
    void btn_quit_OnClick(ActionEvent event) {
        //close application
        Platform.exit();
    }

    @FXML
    void btn_startServer_OnClick(ActionEvent event) {
        //verify whether all variables are correct
        String serverIp;
        int serverPortNumber;
        String storagePool;

        storagePool = lb_folderPath.getText().trim();
        serverIp = tx_serverIpAddress.getText().trim();
        try {
            serverPortNumber = Integer.parseInt(tx_serverPortNumber.getText().trim());
        }
        catch (NumberFormatException e) {
            showWarningDialog(
                    "WRONG PORT VALUE",
                    "Please, enter only numbers as port value."
            );
            return;
        }

        if ( !storagePool.equals(noDirectorySelectedText) && serverIp != "" && serverPortNumber > 0 ) {
            ServerWorker serverWorker = new ServerWorker(
                    serverIp,
                    serverPortNumber
            );
            Thread serverThread = new Thread(serverWorker);
            serverWorker.run();
        }
        else {
            showWarningDialog(
                    "WRONG VARIABLES",
                    "Please, enter correct variables."
            );
        }
    }

    @FXML
    void btn_stopServer_OnClick(ActionEvent event) {

    }


    //-------------------Other variables
    void showInformationDialog(String title, String content){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);

        alert.showAndWait();
    }
    void showWarningDialog(String title, String content){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(content);

        alert.showAndWait();
    }

    //-------------------Public methods
    public static void writeLog(String logContent){
        //https://stackoverflow.com/questions/24116858/most-efficient-way-to-log-messages-to-javafx-textarea-via-threads-with-simple-cu
    }
}
