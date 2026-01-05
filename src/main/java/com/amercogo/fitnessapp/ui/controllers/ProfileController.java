package com.amercogo.fitnessapp.ui.controllers;

import com.amercogo.fitnessapp.auth.SessionManager;
import com.amercogo.fitnessapp.auth.SupabaseAuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ProfileController {

    @FXML private Label emailLabel;
    @FXML private Label userIdLabel;
    @FXML private Label statusLabel;

    private final SupabaseAuthService auth = new SupabaseAuthService();

    @FXML
    public void initialize() {
        emailLabel.setText(SessionManager.getEmail() == null ? "-" : SessionManager.getEmail());
        userIdLabel.setText(SessionManager.getUserId() == null ? "-" : SessionManager.getUserId());
    }

    @FXML
    private void onLogout() {
        auth.signOut();
        statusLabel.setText("Logged out.");
        closeWindow();
    }

    @FXML
    private void onClose() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) emailLabel.getScene().getWindow();
        stage.close();
    }
}
