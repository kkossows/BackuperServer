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
    private Label lb_activeUsersNumber;
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
    @FXML
    private CheckBox ch_rememberSettings;

    //-------------------Other variables
    private String noDirectorySelectedText = "No Directory selected";
    private boolean areSettingsRemembered;


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
            areSettingsRemembered = true;
            tx_serverIpAddress.setText(globalConfig.getSavedServerIpAddress());
            tx_serverPortNumber.setText(Integer.toString(globalConfig.getSavedServerPortNumber()));
        } else {
            areSettingsRemembered = false;
            tx_serverIpAddress.setText(globalConfig.getDefaultServerIpAddress());
            tx_serverPortNumber.setText(Integer.toString(globalConfig.getDefaultServerPortNumber()));
        }

        //fulfill storagePath - if empty in globalConfig, show first application launch information to user.
        if (areSettingsRemembered){
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
    void btn_clearSettings_OnClick(ActionEvent event){
        if (areSettingsRemembered){
            GlobalConfig newGlobalConfig = new GlobalConfig();
            ConfigDataManager.createGlobalConfig(newGlobalConfig);

            showInformationDialog(
                    "Clear settings result",
                    "Settings cleared."
            );
        }
        else {
            showInformationDialog(
                    "Clear settings result",
                    "No settings saved."
            );
        }

    }
    @FXML
    void btn_quit_OnClick(ActionEvent event) {
        //TO_DO


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

        if ( !storagePool.equals(noDirectorySelectedText) && !serverIp.equals("") && serverPortNumber > 0 ) {
            //save values if Remember values checked
            if (ch_rememberSettings.isSelected()){
                GlobalConfig newGlobalConfig = new GlobalConfig(
                        serverIp,
                        serverPortNumber,
                        storagePool
                );
                ConfigDataManager.createGlobalConfig(newGlobalConfig);
            }

            //set storagePath
            GlobalConfig.storagePath = storagePool;

            //disable all controls
            btn_startServer.setDisable(true);
            btn_chooseFolder.setDisable(true);
            ch_rememberSettings.setDisable(true);
            tx_serverIpAddress.setDisable(true);
            tx_serverPortNumber.setDisable(true);

            //start serverWorker
            ServerWorker serverWorker = new ServerWorker(
                    serverIp,
                    serverPortNumber,
                    this
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
        //TO_DO


        //enable all controls
        btn_startServer.setDisable(false);
        btn_chooseFolder.setDisable(false);
        ch_rememberSettings.setDisable(false);
        tx_serverIpAddress.setDisable(false);
        tx_serverPortNumber.setDisable(false);
    }


    //-------------------Other variables
    private void showInformationDialog(String title, String content){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);

        alert.showAndWait();
    }
    private void showWarningDialog(String title, String content){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(content);

        alert.showAndWait();
    }


    //-------------------Public methods
    //should be invoke in Platform.runLater way
    public void updateNumberOfActiveUsers(int numberOfActiveUsers){
        lb_activeUsersNumber.setText(Integer.toString(numberOfActiveUsers));
    }
    public void writeLog(String logContent){
        ta_logs.appendText(logContent + "\n");
    }
}
