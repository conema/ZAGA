package com.bupsolutions.polaritydetection.ml.maxent;

import com.bupsolutions.polaritydetection.features.FeatureStream;
import com.bupsolutions.polaritydetection.ml.PolarityModel;
import com.bupsolutions.polaritydetection.ml.Trainer;
import com.bupsolutions.polaritydetection.model.LabeledTextSet;
import opennlp.maxent.GIS;
import opennlp.maxent.GISModel;
import opennlp.model.OnePassDataIndexer;

import java.io.IOException;

public class MaxEntropyTrainer implements Trainer {

    public MaxEntropyTrainer() {

    }

    public PolarityModel train(LabeledTextSet trainingSet) {
        try {
            FeatureStream trainingStream = new FeatureStream(trainingSet);
            GISModel model = GIS.trainModel(100, new OnePassDataIndexer(trainingStream));
            return new MaxEntropyPolarityModel(model);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
