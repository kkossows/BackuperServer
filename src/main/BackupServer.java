package main;

import javafx.application.Application;
import main.config.ConfigDataManager;
import main.view.ServerApplication;

public class BackupServer {

    public static void main(String[] args) {
        //create configuration directory
        if (!ConfigDataManager.isAppDirExists())
            ConfigDataManager.createAppDir();

        Application.launch(ServerApplication.class, args);
    }
}
