package com.example.smartpantrymanagement;

public class Recipe {

    private final int id;
    private final String name;
    private final String steps;

    public Recipe(int id, String name, String steps) {
        this.id = id;
        this.name = name;
        this.steps = steps;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSteps() {
        return steps;
    }
}
