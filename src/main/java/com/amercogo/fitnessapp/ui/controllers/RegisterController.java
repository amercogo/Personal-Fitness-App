package com.amercogo.fitnessapp.ui.controllers;

import com.amercogo.fitnessapp.auth.SupabaseAuthService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    private final SupabaseAuthService auth = new SupabaseAuthService();

    @FXML
    private void onRegister() {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String pass = passwordField.getText() == null ? "" : passwordField.getText();

        if (email.isBlank() || pass.isBlank()) {
            statusLabel.setText("Email & password required.");
            return;
        }

        statusLabel.setText("Creating...");

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                auth.signUp(email, pass);
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            statusLabel.setText("Account created. Check email to confirm (if enabled).");
        });

        task.setOnFailed(e -> {
            String msg = task.getException() == null ? "Register error" : task.getException().getMessage();
            statusLabel.setText(msg);
            task.getException().printStackTrace();
        });

        new Thread(task, "auth-register").start();
    }

    @FXML
    private void onBack() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.close();
    }
}
