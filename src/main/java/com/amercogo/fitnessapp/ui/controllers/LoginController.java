package com.amercogo.fitnessapp.ui.controllers;

import com.amercogo.fitnessapp.auth.SupabaseAuthService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    private final SupabaseAuthService auth = new SupabaseAuthService();

    @FXML
    private void onLogin() {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String pass = passwordField.getText() == null ? "" : passwordField.getText();

        if (email.isBlank() || pass.isBlank()) {
            statusLabel.setText("Email & password required.");
            return;
        }

        statusLabel.setText("Logging in...");

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                auth.signInWithPassword(email, pass);
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            statusLabel.setText("Success.");
            openWorkoutsInSameStage();
        });

        task.setOnFailed(e -> {
            String msg = task.getException() == null ? "Login error" : task.getException().getMessage();
            statusLabel.setText(msg);
            task.getException().printStackTrace();
        });

        new Thread(task, "auth-login").start();
    }

    @FXML
    private void onOpenRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
            Scene scene = new Scene(loader.load(), 420, 220);

            Stage stage = new Stage();
            stage.setTitle("Register");
            stage.initOwner(emailField.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setScene(scene);
            stage.showAndWait();

        } catch (Exception ex) {
            ex.printStackTrace();
            statusLabel.setText("Cannot open register.");
        }
    }

    private void openWorkoutsInSameStage() {
        try {
            Stage stage = (Stage) emailField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/workout-list.fxml"));
            Scene scene = new Scene(loader.load(), 900, 520);
            stage.setTitle("Personal Fitness App");
            stage.setScene(scene);
        } catch (Exception ex) {
            ex.printStackTrace();
            statusLabel.setText("Cannot open main screen.");
        }
    }
}
