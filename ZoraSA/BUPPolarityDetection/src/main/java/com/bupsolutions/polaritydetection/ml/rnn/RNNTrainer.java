package com.bupsolutions.polaritydetection.ml.rnn;


import com.bupsolutions.polaritydetection.ml.Trainer;
import com.bupsolutions.polaritydetection.model.Label;
import com.bupsolutions.polaritydetection.model.LabeledTextIterator;
import com.bupsolutions.polaritydetection.model.LabeledTextSet;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;


public abstract class RNNTrainer<T extends Label> implements Trainer<T> {

    private int epochs = 1;
    private RNNModel<T> model;

    public RNNTrainer(RNNModel<T> model) {
        this.model = model;
    }

    public MultiLayerNetwork fit(LabeledTextSet<T> trainingData) {
        MultiLayerNetwork rnn = model.getNeuralNetwork();
        LabeledTextIterator<T> trainIterator = new LabeledTextIterator<>(trainingData, model.getBatchSize(), model.getMaxLength());

        for (int i = 0; i < epochs; i++) {
            rnn.fit(trainIterator);
            trainIterator.reset();
        }

        return rnn;
    }

    public void setEpochs(int epochs) {
        this.epochs = epochs;
    }
}
