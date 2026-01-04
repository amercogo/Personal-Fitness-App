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
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/workout-form.fxml")
            );

            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load(), 420, 260);

            WorkoutFormController controller = loader.getController();
            controller.setOnSavedCallback(this::loadDataAsync);

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Add Workout");
            stage.initOwner(table.getScene().getWindow());
            stage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            stage.setScene(scene);
            stage.showAndWait();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void onExportPdf() {
        // Uzimamo trenutno prikazane podatke iz tabele (može i repo.findAll(), ali ovo je bolje)
        var items = table.getItems();
        if (items == null || items.isEmpty()) {
            return;
        }

        javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
        fc.setTitle("Save PDF");
        fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("PDF files", "*.pdf"));
        fc.setInitialFileName("workouts.pdf");

        java.io.File file = fc.showSaveDialog(table.getScene().getWindow());
        if (file == null) return;

        javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<>() {
            @Override
            protected Void call() throws Exception {
                exportWorkoutsToPdf(items, file);
                return null;
            }
        };

        task.setOnRunning(e -> setBusy(true));
        task.setOnSucceeded(e -> setBusy(false));
        task.setOnFailed(e -> {
            setBusy(false);
            task.getException().printStackTrace();
        });

        new Thread(task, "pdf-export").start();
    }

    private void exportWorkoutsToPdf(java.util.List<Workout> items, java.io.File file) throws Exception {
        try (org.apache.pdfbox.pdmodel.PDDocument doc = new org.apache.pdfbox.pdmodel.PDDocument()) {
            org.apache.pdfbox.pdmodel.PDPage page = new org.apache.pdfbox.pdmodel.PDPage();
            doc.addPage(page);

            try (org.apache.pdfbox.pdmodel.PDPageContentStream cs =
                         new org.apache.pdfbox.pdmodel.PDPageContentStream(doc, page)) {

                cs.beginText();
                cs.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD, 16);
                cs.newLineAtOffset(50, 750);
                cs.showText("Workouts Export");
                cs.endText();

                float y = 720;

                // Header
                cs.beginText();
                cs.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD, 11);
                cs.newLineAtOffset(50, y);
                cs.showText("Name");
                cs.newLineAtOffset(220, 0);
                cs.showText("Date");
                cs.newLineAtOffset(120, 0);
                cs.showText("Duration");
                cs.endText();

                y -= 18;

                cs.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA, 11);

                for (Workout w : items) {
                    if (y < 60) {
                        cs.close();
                        page = new org.apache.pdfbox.pdmodel.PDPage();
                        doc.addPage(page);
                        y = 750;
                    }

                    try (org.apache.pdfbox.pdmodel.PDPageContentStream rowCs =
                                 new org.apache.pdfbox.pdmodel.PDPageContentStream(doc, page,
                                         org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode.APPEND, true)) {

                        rowCs.beginText();
                        rowCs.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA, 11);
                        rowCs.newLineAtOffset(50, y);

                        String name = w.getName() == null ? "" : w.getName();
                        String date = w.getDate() == null ? "" : w.getDate().toString();
                        String dur = String.valueOf(w.getDurationMinutes());

                        rowCs.showText(truncate(name, 28));
                        rowCs.newLineAtOffset(220, 0);
                        rowCs.showText(date);
                        rowCs.newLineAtOffset(120, 0);
                        rowCs.showText(dur);

                        rowCs.endText();
                    }

                    y -= 16;
                }
            }

            doc.save(file);
        }
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }


    @FXML
    private void onOpenStats() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/stats.fxml")
            );
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load(), 420, 240);

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Stats");
            stage.initOwner(table.getScene().getWindow());
            stage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            stage.setScene(scene);
            stage.showAndWait();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    @FXML
    private void onOpenSets() {
        Workout selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/workout-sets.fxml")
            );

            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load(), 1050, 420);

            WorkoutSetsController controller = loader.getController();
            controller.setWorkout(selected);

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Sets - " + selected.getName());
            stage.initOwner(table.getScene().getWindow());
            stage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            stage.setScene(scene);
            stage.showAndWait();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}
