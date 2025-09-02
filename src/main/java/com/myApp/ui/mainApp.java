package com.myApp.ui;
import com.myApp.modelos.*;
import com.myApp.logicaDeNegocio.*;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.io.IOException;


public class mainApp extends Application{

    public static void main(String[] args) {

        launch(args);

    }

    @Override
    public void start(Stage stage) {
        FXMLLoader load = new FXMLLoader(getClass().getResource("/Menu.fxml"));
        try {
            Scene scene = new Scene(load.load());
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
