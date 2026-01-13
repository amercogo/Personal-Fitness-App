package com.amercogo.fitnessapp.ui.controllers;

import com.amercogo.fitnessapp.auth.SupabaseAuthService;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class LoginCardController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private ToggleButton togglePasswordBtn;

    @FXML private Label statusLabel;

    @FXML private Button loginBtn;
    @FXML private Button registerBtn;
    @FXML private VBox actionsBox;
    @FXML private ProgressIndicator loading;

    private final SupabaseAuthService auth = new SupabaseAuthService();

    private LoginController host; // shell controller

    public void setHost(LoginController host) {
        this.host = host;
    }

    @FXML
    public void initialize() {
        passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());

        Runnable validate = () -> {
            String email = emailField.getText() == null ? "" : emailField.getText().trim();
            String pass = passwordField.getText() == null ? "" : passwordField.getText();
            loginBtn.setDisable(email.isBlank() || pass.isBlank());
        };

        emailField.textProperty().addListener((o,a,b) -> validate.run());
        passwordField.textProperty().addListener((o,a,b) -> validate.run());
        validate.run();

        addHoverScale(loginBtn, 1.02);
        addHoverScale(registerBtn, 1.02);
        addHoverScale(togglePasswordBtn, 1.04);
        addHoverScale(emailField, 1.01);
        addHoverScale(passwordField, 1.01);
        addHoverScale(passwordVisibleField, 1.01);
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
    private void onOpenRegister() {
        if (host != null) host.showRegisterCard(true);
    }

    @FXML
    private void onLogin() {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String pass = passwordField.getText() == null ? "" : passwordField.getText();

        if (email.isBlank() || pass.isBlank()) {
            statusLabel.setText("Email i lozinka su obavezni.");
            return;
        }

        setLoading(true);
        statusLabel.setText("Prijava u toku...");

        javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<>() {
            @Override
            protected Void call() throws Exception {
                auth.signInWithPassword(email, pass);
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            statusLabel.setText("Uspješno.");
            if (host != null) host.openWorkouts(); // prebacivanje na main screen
        });

        task.setOnFailed(e -> {
            String msg = task.getException() == null ? "Greška pri prijavi." : task.getException().getMessage();
            statusLabel.setText(msg);
            setLoading(false);
            shake(actionsBox);
            if (task.getException() != null) task.getException().printStackTrace();
        });

        Thread t = new Thread(task, "auth-login");
        t.setDaemon(true);
        t.start();
    }

    private void setLoading(boolean v) {
        loading.setVisible(v);
        loading.setManaged(v);

        emailField.setDisable(v);
        passwordField.setDisable(v);
        passwordVisibleField.setDisable(v);
        togglePasswordBtn.setDisable(v);

        registerBtn.setDisable(v);
        loginBtn.setDisable(v);
        if (!v) {
            String email = emailField.getText() == null ? "" : emailField.getText().trim();
            String pass = passwordField.getText() == null ? "" : passwordField.getText();
            loginBtn.setDisable(email.isBlank() || pass.isBlank());
        }
    }

    private void addHoverScale(Node n, double s) {
        n.setOnMouseEntered(e -> scale(n, 1.0, s, 120).play());
        n.setOnMouseExited(e -> scale(n, s, 1.0, 140).play());
    }

    private ScaleTransition scale(Node n, double f, double t, int ms) {
        ScaleTransition st = new ScaleTransition(Duration.millis(ms), n);
        st.setFromX(f);
        st.setFromY(f);
        st.setToX(t);
        st.setToY(t);
        st.setInterpolator(Interpolator.EASE_OUT);
        return st;
    }

    private void shake(Node n) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(70), n);
        tt.setFromX(0);
        tt.setToX(8);
        tt.setAutoReverse(true);
        tt.setCycleCount(6);
        tt.setInterpolator(Interpolator.EASE_OUT);
        tt.playFromStart();
    }
}
