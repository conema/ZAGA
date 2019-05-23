package com.bupsolutions.polaritydetection.ml.maxent;


import com.bupsolutions.polaritydetection.ml.PolarityHandler;
import com.bupsolutions.polaritydetection.ml.PolarityModel;
import com.bupsolutions.polaritydetection.model.Polarity;
import com.bupsolutions.polaritydetection.nlp.NLPProcessor;
import opennlp.maxent.GISModel;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class MaxEntropyPolarityModel extends MaxEntropyModel<Polarity> implements PolarityModel {

    private PolarityHandler handler = new PolarityHandler();

    public MaxEntropyPolarityModel(GISModel model) throws IOException {
        super(model);
    }

    public MaxEntropyPolarityModel(File model) throws IOException {
        super(model);
    }

    public MaxEntropyPolarityModel(String path) throws IOException {
        super(path);
    }

    @Override
    public Map<Polarity, Double> getProbabilities(String message) {
        String[] context = NLPProcessor.getInstance().process(message);

        double posProb = listProbabilities(context)[0];

        return handler.mapProbabilities(context, posProb);
    }

    @Override
    public Polarity eval(String message) {
        Map<Polarity, Double> probabilities = getProbabilities(message);
        return handler.getPolarity(probabilities);
    }

    public void handleNegations(boolean flag) {
        handler.handleNegations(flag);
    }
}
