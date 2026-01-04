package com.amercogo.fitnessapp.ui.controllers;

import com.amercogo.fitnessapp.db.repositories.WorkoutRepository;
import com.amercogo.fitnessapp.model.Workout;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class WorkoutFormController {

    @FXML private TextField nameField;
    @FXML private DatePicker datePicker;
    @FXML private Spinner<Integer> durationSpinner;
    @FXML private Label statusLabel;

    private final WorkoutRepository repo = new WorkoutRepository();

    // callback da nakon Save osvježi listu
    private Runnable onSavedCallback;

    public void setOnSavedCallback(Runnable onSavedCallback) {
        this.onSavedCallback = onSavedCallback;
    }

    @FXML
    public void initialize() {
        datePicker.setValue(LocalDate.now());
        durationSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 600, 60));
    }

    @FXML
    private void onSave() {
        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        LocalDate date = datePicker.getValue();
        int duration = durationSpinner.getValue() == null ? 0 : durationSpinner.getValue();

        if (name.isBlank()) {
            statusLabel.setText("Name is required.");
            return;
        }
        if (date == null) {
            statusLabel.setText("Date is required.");
            return;
        }

        statusLabel.setText("Saving...");

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                Workout w = new Workout(null, null, name, date, duration);
                repo.save(w);
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            statusLabel.setText("Saved.");
            if (onSavedCallback != null) onSavedCallback.run();
            closeWindow();
        });

        task.setOnFailed(e -> {
            statusLabel.setText("Error while saving.");
            task.getException().printStackTrace();
        });

        new Thread(task, "workout-save").start();
    }

    @FXML
    private void onCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}
