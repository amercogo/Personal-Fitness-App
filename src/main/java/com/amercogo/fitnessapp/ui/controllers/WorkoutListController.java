package com.amercogo.fitnessapp.ui.controllers;

import com.amercogo.fitnessapp.db.repositories.WorkoutRepository;
import com.amercogo.fitnessapp.model.Workout;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.List;

public class WorkoutListController {

    @FXML
    private TableView<Workout> table;

    @FXML
    private TableColumn<Workout, String> nameCol;

    @FXML
    private TableColumn<Workout, Object> dateCol;

    @FXML
    private TableColumn<Workout, Integer> durationCol;

    private final WorkoutRepository repo = new WorkoutRepository();

    @FXML
    public void initialize() {
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        durationCol.setCellValueFactory(new PropertyValueFactory<>("durationMinutes"));

        loadDataAsync();
    }

    private void setBusy(boolean busy) {
        if (table != null && table.getScene() != null) {
            table.getScene().setCursor(busy ? Cursor.WAIT : Cursor.DEFAULT);
        }
        if (table != null) {
            table.setDisable(busy);
        }
    }

    private void loadDataAsync() {
        Task<List<Workout>> task = new Task<>() {
            @Override
            protected List<Workout> call() {
                return repo.findAll();
            }
        };

        task.setOnRunning(e -> setBusy(true));

        task.setOnSucceeded(e -> {
            table.setItems(FXCollections.observableArrayList(task.getValue()));
            setBusy(false);
        });

        task.setOnFailed(e -> {
            setBusy(false);
            task.getException().printStackTrace();
        });

        new Thread(task, "workouts-load").start();
    }

    @FXML
    private void onRefresh() {
        loadDataAsync();
    }

    @FXML
    private void onDelete() {
        Workout selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                repo.deleteById(selected.getId());
                return null;
            }
        };

        task.setOnRunning(e -> setBusy(true));
        task.setOnSucceeded(e -> loadDataAsync());
        task.setOnFailed(e -> {
            setBusy(false);
            task.getException().printStackTrace();
        });

        new Thread(task, "workouts-delete").start();
    }

    @FXML
    private void onAdd() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                Workout w = new Workout(null, null, "Demo Workout", LocalDate.now(), 45);
                repo.save(w);
                return null;
            }
        };

        task.setOnRunning(e -> setBusy(true));
        task.setOnSucceeded(e -> loadDataAsync());
        task.setOnFailed(e -> {
            setBusy(false);
            task.getException().printStackTrace();
        });

        new Thread(task, "workouts-add").start();
    }
}
