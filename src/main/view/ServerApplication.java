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
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/fxml/App.fxml"));
        Parent rootPane = loader.load();
        Scene appScene = new Scene(rootPane);
        ((AppController) loader.getController()).makeDraggable(appScene, primaryStage);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setScene(appScene);
        primaryStage.show();
    }
}
