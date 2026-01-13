package com.amercogo.fitnessapp.ui.controllers;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private StackPane root;
    @FXML private VBox windowShell;

    @FXML private HBox titleBar;
    @FXML private Button minBtn;
    @FXML private Button closeBtn;

    @FXML private StackPane cardHost;

    private double dragOffsetX;
    private double dragOffsetY;

    private boolean minimizing = false;
    private boolean restoring = false;
    private long lastMinimizeAt = 0L;

    private boolean stageHooksInstalled = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> showLoginCard(false));

        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) return;
            Platform.runLater(() -> installStageHooksOnce(newScene));
        });
    }

    private void installStageHooksOnce(Scene scene) {
        if (stageHooksInstalled) return;

        Stage stage = (Stage) scene.getWindow();
        if (stage == null) return;

        stageHooksInstalled = true;
        setupWindowDrag(stage);
        setupRestoreHandler(stage);
    }

    private void setupWindowDrag(Stage stage) {
        titleBar.setOnMousePressed(e -> {
            dragOffsetX = e.getScreenX() - stage.getX();
            dragOffsetY = e.getScreenY() - stage.getY();
        });

        titleBar.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() - dragOffsetX);
            stage.setY(e.getScreenY() - dragOffsetY);
        });
    }

    private void setupRestoreHandler(Stage stage) {
        stage.iconifiedProperty().addListener((obs, wasIcon, isIcon) -> {
            if (!isIcon) {
                if (restoring) return;
                restoring = true;

                Platform.runLater(() -> {
                    windowShell.setOpacity(0.0);
                    windowShell.setScaleX(0.985);
                    windowShell.setScaleY(0.985);

                    ParallelTransition in = new ParallelTransition(
                            fade(windowShell, 0.0, 1.0, 160),
                            scale(windowShell, 0.985, 1.0, 160)
                    );
                    in.setOnFinished(e -> {
                        restoring = false;
                        minimizing = false;
                    });
                    in.playFromStart();
                });
            }
        });
    }

    @FXML
    private void onMinimize() {
        if (minimizing) return;

        long now = System.currentTimeMillis();
        if (now - lastMinimizeAt < 250) return;
        lastMinimizeAt = now;

        minimizing = true;

        Stage stage = (Stage) root.getScene().getWindow();

        ParallelTransition out = new ParallelTransition(
                fade(windowShell, windowShell.getOpacity(), 0.0, 140),
                scale(windowShell, 1.0, 0.985, 140)
        );

        out.setOnFinished(ev -> Platform.runLater(() -> stage.setIconified(true)));
        out.playFromStart();
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) root.getScene().getWindow();

        ParallelTransition out = new ParallelTransition(
                fade(windowShell, windowShell.getOpacity(), 0.0, 160),
                scale(windowShell, 1.0, 0.985, 160)
        );
        out.setOnFinished(e -> stage.close());
        out.playFromStart();
    }

    public void showLoginCard(boolean animate) {
        swapCard("/fxml/login_card.fxml", animate);
    }

    public void showRegisterCard(boolean animate) {
        swapCard("/fxml/register_card.fxml", animate);
    }

    private void swapCard(String fxmlPath, boolean animate) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof LoginCardController lc) {
                lc.setHost(this);
            }
            if (controller instanceof RegisterController rc) {
                rc.setHostController(this);
            }

            if (cardHost.getChildren().isEmpty() || !animate) {
                cardHost.getChildren().setAll(view);
                playCardIntro(view);
                return;
            }

            Parent old = (Parent) cardHost.getChildren().get(0);

            FadeTransition out = new FadeTransition(Duration.millis(140), old);
            out.setFromValue(1);
            out.setToValue(0);

            out.setOnFinished(e -> {
                cardHost.getChildren().setAll(view);

                view.setOpacity(0);
                FadeTransition in = new FadeTransition(Duration.millis(200), view);
                in.setFromValue(0);
                in.setToValue(1);
                in.play();

                playCardIntro(view);
            });

            out.play();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void playCardIntro(Parent view) {
        view.setOpacity(0.0);
        view.setTranslateY(14.0);

        ParallelTransition in = new ParallelTransition(
                fade(view, 0.0, 1.0, 240),
                translateY(view, 14.0, 0.0, 240)
        );
        in.playFromStart();
    }

    // poziva login card kad se uspješno loguje
    public void openWorkouts() {
        try {
            Stage stage = (Stage) root.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/workout-list.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 650);
            scene.getStylesheets().add(getClass().getResource("/css/login.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private FadeTransition fade(javafx.scene.Node n, double f, double t, int ms) {
        FadeTransition ft = new FadeTransition(Duration.millis(ms), n);
        ft.setFromValue(f);
        ft.setToValue(t);
        ft.setInterpolator(Interpolator.EASE_OUT);
        return ft;
    }

    private TranslateTransition translateY(javafx.scene.Node n, double f, double t, int ms) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(ms), n);
        tt.setFromY(f);
        tt.setToY(t);
        tt.setInterpolator(Interpolator.EASE_OUT);
        return tt;
    }

    private ScaleTransition scale(javafx.scene.Node n, double f, double t, int ms) {
        ScaleTransition st = new ScaleTransition(Duration.millis(ms), n);
        st.setFromX(f);
        st.setFromY(f);
        st.setToX(t);
        st.setToY(t);
        st.setInterpolator(Interpolator.EASE_OUT);
        return st;
    }
}
