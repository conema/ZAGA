package com.bupsolutions.polaritydetection.model;


public class LabeledText<T extends Label> {
    private String text;
    private T label;

    public LabeledText(String text, T label) {
        this.label = label;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public T getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return "(" + text + ", " + label + ")";
    }
}
