package com.bupsolutions.polaritydetection.ml;


import com.bupsolutions.polaritydetection.model.Polarity;

public interface PolarityModel extends ClassificationModel<Polarity> {
    void handleNegations(boolean flag);
}
