package com.bupsolutions.polaritydetection.ml;

import com.bupsolutions.polaritydetection.model.Label;

import java.io.IOException;
import java.util.Map;

public interface ClassificationModel<T extends Label> {
    void save(String path) throws IOException;
    T eval(String message);
    Map<T, Double> getProbabilities(String message);
}
