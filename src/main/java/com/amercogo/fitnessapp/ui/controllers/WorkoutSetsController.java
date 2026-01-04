package com.amercogo.fitnessapp.ui.controllers;

import com.amercogo.fitnessapp.db.repositories.ExerciseRepository;
import com.amercogo.fitnessapp.db.repositories.WorkoutSetRepository;
import com.amercogo.fitnessapp.model.Exercise;
import com.amercogo.fitnessapp.model.Workout;
import com.amercogo.fitnessapp.model.WorkoutSet;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkoutSetsController {

    // Header / form
    @FXML private Label titleLabel;
    @FXML private Label statusLabel;
    @FXML private ComboBox<Exercise> exerciseCombo;
    @FXML private Spinner<Integer> setNoSpinner;
    @FXML private Spinner<Integer> repsSpinner;
    @FXML private TextField kgField;

    // Table
    @FXML private TableView<WorkoutSetView> table;
    @FXML private TableColumn<WorkoutSetView, Integer> setNoCol;
    @FXML private TableColumn<WorkoutSetView, String> exerciseCol;
    @FXML private TableColumn<WorkoutSetView, Integer> repsCol;
    @FXML private TableColumn<WorkoutSetView, Double> kgCol;

    private final ExerciseRepository exerciseRepo = new ExerciseRepository();
    private final WorkoutSetRepository setRepo = new WorkoutSetRepository();

    private Workout workout; // selected workout (set from outside)
    private Map<String, String> exerciseNameById = new HashMap<>();

    public void setWorkout(Workout workout) {
        this.workout = workout;
        if (titleLabel != null && workout != null) {
            titleLabel.setText("Workout Sets: " + workout.getName() + " (" + workout.getDate() + ")");
        }
    }

    @FXML
    public void initialize() {
        setNoCol.setCellValueFactory(new PropertyValueFactory<>("setNo"));
        exerciseCol.setCellValueFactory(new PropertyValueFactory<>("exerciseName"));
        repsCol.setCellValueFactory(new PropertyValueFactory<>("reps"));
        kgCol.setCellValueFactory(new PropertyValueFactory<>("kg"));

        setNoSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 1));
        repsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 200, 10));
        kgField.setText("0");

        loadExercisesAsync();
    }

    private void setBusy(boolean busy) {
        if (table != null && table.getScene() != null) {
            table.getScene().setCursor(busy ? Cursor.WAIT : Cursor.DEFAULT);
        }
        if (table != null) table.setDisable(busy);
    }

    private void loadExercisesAsync() {
        statusLabel.setText("Loading exercises...");
        Task<List<Exercise>> task = new Task<>() {
            @Override
            protected List<Exercise> call() {
                return exerciseRepo.findAll();
            }
        };

        task.setOnRunning(e -> setBusy(true));

        task.setOnSucceeded(e -> {
            List<Exercise> ex = task.getValue();
            exerciseCombo.setItems(FXCollections.observableArrayList(ex));
            if (!ex.isEmpty()) exerciseCombo.getSelectionModel().select(0);

            exerciseNameById.clear();
            for (Exercise a : ex) exerciseNameById.put(a.getId(), a.getName());

            statusLabel.setText("Done.");
            setBusy(false);

            // ako je workout već postavljen – učitaj setove
            if (workout != null) {
                titleLabel.setText("Workout Sets: " + workout.getName() + " (" + workout.getDate() + ")");
                loadSetsAsync();
            }
        });

        task.setOnFailed(e -> {
            setBusy(false);
            statusLabel.setText("Error loading exercises.");
            task.getException().printStackTrace();
        });

        new Thread(task, "exercises-load").start();
    }

    private void loadSetsAsync() {
        if (workout == null) {
            statusLabel.setText("No workout selected.");
            return;
        }

        statusLabel.setText("Loading sets...");
        Task<List<WorkoutSet>> task = new Task<>() {
            @Override
            protected List<WorkoutSet> call() {
                return setRepo.findByWorkoutId(workout.getId());
            }
        };

        task.setOnRunning(e -> setBusy(true));

        task.setOnSucceeded(e -> {
            List<WorkoutSet> sets = task.getValue();
            var views = sets.stream().map(s -> {
                String exName = exerciseNameById.getOrDefault(s.getExerciseId(), "Exercise");
                return new WorkoutSetView(s.getId(), s.getSetNo(), exName, s.getReps(), s.getWeightKg());
            }).toList();

            table.setItems(FXCollections.observableArrayList(views));
            statusLabel.setText("Done.");
            setBusy(false);
        });

        task.setOnFailed(e -> {
            setBusy(false);
            statusLabel.setText("Error loading sets.");
            task.getException().printStackTrace();
        });

        new Thread(task, "sets-load").start();
    }

    @FXML
    private void onRefresh() {
        loadSetsAsync();
    }

    @FXML
    private void onAddSet() {
        if (workout == null) {
            statusLabel.setText("No workout selected.");
            return;
        }
        Exercise ex = exerciseCombo.getSelectionModel().getSelectedItem();
        if (ex == null) {
            statusLabel.setText("Select exercise.");
            return;
        }

        int setNo = setNoSpinner.getValue() == null ? 1 : setNoSpinner.getValue();
        int reps = repsSpinner.getValue() == null ? 1 : repsSpinner.getValue();

        double kg;
        try {
            kg = Double.parseDouble((kgField.getText() == null ? "0" : kgField.getText().trim()).replace(",", "."));
        } catch (Exception err) {
            statusLabel.setText("Kg must be number.");
            return;
        }

        statusLabel.setText("Saving set...");

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                WorkoutSet s = new WorkoutSet(null, workout.getId(), ex.getId(), setNo, reps, kg);
                setRepo.save(s);
                return null;
            }
        };

        task.setOnRunning(e -> setBusy(true));
        task.setOnSucceeded(e -> {
            setBusy(false);
            statusLabel.setText("Saved.");
            loadSetsAsync();
        });
        task.setOnFailed(e -> {
            setBusy(false);
            statusLabel.setText("Error saving set.");
            task.getException().printStackTrace();
        });

        new Thread(task, "set-save").start();
    }

    @FXML
    private void onDeleteSelected() {
        WorkoutSetView selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        statusLabel.setText("Deleting...");

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                setRepo.deleteById(selected.getId());
                return null;
            }
        };

        task.setOnRunning(e -> setBusy(true));
        task.setOnSucceeded(e -> {
            setBusy(false);
            statusLabel.setText("Deleted.");
            loadSetsAsync();
        });
        task.setOnFailed(e -> {
            setBusy(false);
            statusLabel.setText("Error deleting.");
            task.getException().printStackTrace();
        });

        new Thread(task, "set-delete").start();
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) table.getScene().getWindow();
        stage.close();
    }

    // Table row model (UI only)
    public static class WorkoutSetView {
        private final String id;
        private final int setNo;
        private final String exerciseName;
        private final int reps;
        private final double kg;

        public WorkoutSetView(String id, int setNo, String exerciseName, int reps, double kg) {
            this.id = id;
            this.setNo = setNo;
            this.exerciseName = exerciseName;
            this.reps = reps;
            this.kg = kg;
        }

        public String getId() { return id; }
        public int getSetNo() { return setNo; }
        public String getExerciseName() { return exerciseName; }
        public int getReps() { return reps; }
        public double getKg() { return kg; }
    }
}
