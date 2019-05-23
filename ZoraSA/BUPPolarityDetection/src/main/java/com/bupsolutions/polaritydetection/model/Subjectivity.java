package com.bupsolutions.polaritydetection.model;


public enum Subjectivity implements Label {
    SUBJECTIVE(0, "Subjective"),
    OBJECTIVE(1, "Objective");

    private int value;
    private String label;

    Subjectivity(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public int asInt() {
        return value;
    }

    @Override
    public String toString() {
        return label;
    }

    public static Subjectivity valueOf(int value) {
        for (Subjectivity subjectivity : Subjectivity.values()) {
            if (subjectivity.asInt() == value) {
                return subjectivity;
            }
        }

        throw new IllegalArgumentException("Subjectivity not foud for value: " + value);
    }

    public Label[] labels() {
        return Subjectivity.values();
    }
}
