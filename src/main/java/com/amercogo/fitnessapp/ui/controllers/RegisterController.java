package com.amercogo.fitnessapp.ui.controllers;

import com.amercogo.fitnessapp.auth.SupabaseAuthService;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class RegisterController {

    @FXML private VBox root;

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private ToggleButton togglePasswordBtn;

    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loading;
    @FXML private Button createBtn;
    @FXML private Button backBtn;

    private final SupabaseAuthService auth = new SupabaseAuthService();

    private LoginController hostController;

    public void setHostController(LoginController hostController) {
        this.hostController = hostController;
    }

    @FXML
    public void initialize() {
        passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());
    }

    @FXML
    private void onTogglePassword() {
        boolean show = togglePasswordBtn.isSelected();

        passwordVisibleField.setVisible(show);
        passwordVisibleField.setManaged(show);

        passwordField.setVisible(!show);
        passwordField.setManaged(!show);
    }

    @FXML
    private void onRegister() {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String pass = passwordField.getText() == null ? "" : passwordField.getText();

        if (email.isBlank() || pass.isBlank()) {
            statusLabel.setText("Email i lozinka su obavezni.");
            return;
        }

        setLoading(true);
        statusLabel.setText("Kreiram račun...");

        javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<>() {
            @Override
            protected Void call() throws Exception {
                auth.signUp(email, pass);
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            statusLabel.setText("Uspješno! Provjeri email za potvrdu.");
            setLoading(false);
        });

        task.setOnFailed(e -> {
            String msg = task.getException() == null
                    ? "Greška pri registraciji."
                    : task.getException().getMessage();
            statusLabel.setText(msg);
            setLoading(false);
            shake(root);
            if (task.getException() != null) task.getException().printStackTrace();
        });

        Thread t = new Thread(task, "auth-register");
        t.setDaemon(true);
        t.start();
    }

    @FXML
    private void onBack() {
        if (hostController != null) {
            hostController.showLoginCard(true);
        }
    }

    private void setLoading(boolean v) {
        loading.setVisible(v);
        loading.setManaged(v);

        emailField.setDisable(v);
        passwordField.setDisable(v);
        passwordVisibleField.setDisable(v);
        togglePasswordBtn.setDisable(v);

        createBtn.setDisable(v);
        backBtn.setDisable(v);
    }

    private void shake(javafx.scene.Node n) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(70), n);
        tt.setFromX(0);
        tt.setToX(8);
        tt.setAutoReverse(true);
        tt.setCycleCount(6);
        tt.setInterpolator(Interpolator.EASE_OUT);
        tt.playFromStart();
    }
}
