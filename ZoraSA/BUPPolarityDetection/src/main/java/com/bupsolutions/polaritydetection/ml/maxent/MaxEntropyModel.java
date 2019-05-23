package com.bupsolutions.polaritydetection.ml.maxent;

import com.bupsolutions.polaritydetection.ml.ClassificationModel;
import com.bupsolutions.polaritydetection.model.Label;
import opennlp.maxent.GISModel;
import opennlp.maxent.io.GISModelWriter;
import opennlp.maxent.io.SuffixSensitiveGISModelReader;
import opennlp.maxent.io.SuffixSensitiveGISModelWriter;

import java.io.File;
import java.io.IOException;

public abstract class MaxEntropyModel<T extends Label> implements ClassificationModel<T> {

    private GISModel model;
    private boolean handleNegations = true;

    public MaxEntropyModel(GISModel model) throws IOException {
        if (model == null) {
            throw new IllegalArgumentException("Model cannot be null");
        }
        this.model = model;
    }

    public MaxEntropyModel(File model) throws IOException {
        this((GISModel) new SuffixSensitiveGISModelReader(model).getModel());
    }

    public MaxEntropyModel(String path) throws IOException {
        this(new File(path));
    }

    public double[] listProbabilities(String[] context) {
        return model.eval(context);
    }

    @Override
    public void save(String path) throws IOException {
        File outputFile = new File(path);
        GISModelWriter writer = new SuffixSensitiveGISModelWriter(model, outputFile);
        writer.persist();
    }

}
