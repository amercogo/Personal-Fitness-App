package com.amercogo.fitnessapp.ui.controllers;

import com.amercogo.fitnessapp.db.repositories.WorkoutRepository;
import com.amercogo.fitnessapp.model.Workout;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.Comparator;
import java.util.List;

public class StatsController {

    @FXML private Label totalWorkoutsLabel;
    @FXML private Label totalMinutesLabel;
    @FXML private Label avgDurationLabel;
    @FXML private Label lastDateLabel;
    @FXML private Label statusLabel;

    private final WorkoutRepository repo = new WorkoutRepository();

    @FXML
    public void initialize() {
        loadStatsAsync();
    }

    @FXML
    private void onRefresh() {
        loadStatsAsync();
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) totalWorkoutsLabel.getScene().getWindow();
        stage.close();
    }

    private void loadStatsAsync() {
        statusLabel.setText("Loading...");

        Task<List<Workout>> task = new Task<>() {
            @Override
            protected List<Workout> call() {
                return repo.findAll();
            }
        };

        task.setOnSucceeded(e -> {
            List<Workout> items = task.getValue();

            int total = items.size();
            int totalMinutes = items.stream().mapToInt(Workout::getDurationMinutes).sum();
            int avg = total == 0 ? 0 : (int) Math.round(totalMinutes / (double) total);

            String lastDate = items.stream()
                    .map(Workout::getDate)
                    .filter(d -> d != null)
                    .max(Comparator.naturalOrder())
                    .map(Object::toString)
                    .orElse("-");

            totalWorkoutsLabel.setText(String.valueOf(total));
            totalMinutesLabel.setText(String.valueOf(totalMinutes));
            avgDurationLabel.setText(avg + " min");
            lastDateLabel.setText(lastDate);

            statusLabel.setText("Done.");
        });

        task.setOnFailed(e -> {
            statusLabel.setText("Error.");
            task.getException().printStackTrace();
        });

        new Thread(task, "stats-load").start();
    }
}
