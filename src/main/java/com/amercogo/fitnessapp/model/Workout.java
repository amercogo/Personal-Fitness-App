package com.amercogo.fitnessapp.model;

import java.time.LocalDate;

public class Workout {

    private String id;
    private String userId;
    private String name;
    private LocalDate date;
    private int durationMinutes;

    public Workout() {
    }

    public Workout(String id, String userId, String name, LocalDate date, int durationMinutes) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.date = date;
        this.durationMinutes = durationMinutes;
    }

    // getters & setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
}
