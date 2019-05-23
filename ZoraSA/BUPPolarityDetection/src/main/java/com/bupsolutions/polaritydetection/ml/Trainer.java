package com.bupsolutions.polaritydetection.ml;


import com.bupsolutions.polaritydetection.model.Label;
import com.bupsolutions.polaritydetection.model.LabeledTextSet;

public interface Trainer<T extends Label> {
    ClassificationModel<T> train(LabeledTextSet<T> trainingSet);
}
