package main.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Created by kkossowski on 20.11.2017.
 */
public class ServerApplication extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent rootPane = FXMLLoader.load(getClass().getResource("/fxml/App.fxml"));
        Scene appScene = new Scene(rootPane);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setScene(appScene);
        primaryStage.show();
    }
}
