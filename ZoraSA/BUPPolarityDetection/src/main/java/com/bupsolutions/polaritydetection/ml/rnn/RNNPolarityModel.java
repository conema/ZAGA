package com.bupsolutions.polaritydetection.ml.rnn;

import com.bupsolutions.polaritydetection.features.WordVectorsManager;
import com.bupsolutions.polaritydetection.ml.PolarityHandler;
import com.bupsolutions.polaritydetection.ml.PolarityModel;
import com.bupsolutions.polaritydetection.model.Polarity;
import com.bupsolutions.polaritydetection.nlp.NLPProcessor;
import com.bupsolutions.polaritydetection.utils.Times;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.joda.time.Duration;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class RNNPolarityModel extends RNNModel<Polarity> implements PolarityModel {

    PolarityHandler handler = new PolarityHandler();

    public RNNPolarityModel() {
    }

    public RNNPolarityModel(MultiLayerNetwork rnn) {
        super(rnn);
    }

    public RNNPolarityModel(File model) throws IOException {
        super(model);
    }

    public RNNPolarityModel(String path) throws IOException {
        super(path);
    }

    public Map<Polarity, Double> getProbabilities(String message) {


        long start = System.currentTimeMillis();
        String[] context = NLPProcessor.getInstance().process(message);
        long preprocess = System.currentTimeMillis();
        INDArray features = WordVectorsManager.getVectors().extractFeatures(context);
        long wordvectors = System.currentTimeMillis();
        INDArray probabilitiesAtLastWord = probabilitiesAtLastInstance(features);
        double posProb = probabilitiesAtLastWord.getDouble(Polarity.POSITIVE.asInt());
        long classification = System.currentTimeMillis();

        Times.lengths.add(message.length());
        Times.preprocessing.add(new Duration(start, preprocess));
        Times.wordvectors.add(new Duration(preprocess, wordvectors));
        Times.classification.add(new Duration(wordvectors, classification));

        return handler.mapProbabilities(context, posProb);
    }

    @Override
    public Polarity eval(String message) {
        Map<Polarity, Double> map = getProbabilities(message);
        return handler.getPolarity(map);
    }

    @Override
    public void handleNegations(boolean flag) {
        handler.handleNegations(flag);
    }
}
