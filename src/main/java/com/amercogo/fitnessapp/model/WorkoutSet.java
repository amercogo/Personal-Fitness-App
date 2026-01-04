package com.amercogo.fitnessapp.model;

public class WorkoutSet {

    private String id;
    private String workoutId;
    private String exerciseId;
    private int setNo;
    private int reps;
    private double weightKg;

    public WorkoutSet() {
    }

    public WorkoutSet(String id,
                      String workoutId,
                      String exerciseId,
                      int setNo,
                      int reps,
                      double weightKg) {
        this.id = id;
        this.workoutId = workoutId;
        this.exerciseId = exerciseId;
        this.setNo = setNo;
        this.reps = reps;
        this.weightKg = weightKg;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWorkoutId() {
        return workoutId;
    }

    public void setWorkoutId(String workoutId) {
        this.workoutId = workoutId;
    }

    public String getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(String exerciseId) {
        this.exerciseId = exerciseId;
    }

    public int getSetNo() {
        return setNo;
    }

    public void setSetNo(int setNo) {
        this.setNo = setNo;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(double weightKg) {
        this.weightKg = weightKg;
    }
}
