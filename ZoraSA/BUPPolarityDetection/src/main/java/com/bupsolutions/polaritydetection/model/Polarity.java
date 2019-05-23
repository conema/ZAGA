package com.bupsolutions.polaritydetection.model;


public enum Polarity implements Label {
    POSITIVE(0, "Positive"),
    NEGATIVE(1, "Negative");

    private int value;
    private String label;

    Polarity(int value, String label) {
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

    public static Polarity valueOf(int value) {
        for (Polarity polarity : Polarity.values()) {
            if (polarity.asInt() == value) {
                return polarity;
            }
        }

        throw new IllegalArgumentException("Polarity not foud for value: " + value);
    }

    public Label[] labels() {
        return Polarity.values();
    }

}
