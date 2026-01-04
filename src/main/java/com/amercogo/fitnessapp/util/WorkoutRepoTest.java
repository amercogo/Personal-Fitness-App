package com.amercogo.fitnessapp.util;

import com.amercogo.fitnessapp.db.repositories.WorkoutRepository;
import com.amercogo.fitnessapp.model.Workout;

import java.time.LocalDate;

public class WorkoutRepoTest {
    public static void main(String[] args) {
        WorkoutRepository repo = new WorkoutRepository();

        // CREATE
        Workout w = new Workout(null, null, "Push", LocalDate.now(), 60);
        repo.save(w);
        System.out.println("Created: " + w.getId());

        // READ ALL
        System.out.println("All workouts: " + repo.findAll().size());

        // UPDATE
        w.setDurationMinutes(75);
        repo.update(w);
        System.out.println("Updated duration to 75");

        // DELETE
        repo.deleteById(w.getId());
        System.out.println("Deleted: " + w.getId());
    }
}
