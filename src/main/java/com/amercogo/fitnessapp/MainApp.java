package com.amercogo.fitnessapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Scene scene = new Scene(loader.load(), 1040, 660); // <-- VEĆE

        scene.getStylesheets().add(getClass().getResource("/css/login.css").toExternalForm());

        stage.initStyle(StageStyle.UNDECORATED);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/css/logo.png")));

        scene.setFill(Color.rgb(0, 0, 0, 0.01));

        stage.setTitle("Personal Fitness App – Login");
        stage.setScene(scene);

        // ✅ dozvoli resize + minimum (da se ne “raspadne”)
        stage.setResizable(true);
        stage.setMinWidth(900);
        stage.setMinHeight(560);

        stage.centerOnScreen(); // centriraj
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
