package com.bupsolutions.polaritydetection.ml.rnn;

import com.bupsolutions.polaritydetection.model.LabeledTextSet;
import com.bupsolutions.polaritydetection.model.Polarity;

public class RNNPolarityTrainer extends RNNTrainer<Polarity> {

    public RNNPolarityTrainer() {
        super(new RNNPolarityModel());
    }

    public RNNPolarityTrainer(RNNModel<Polarity> model) {
        super(model);
    }

    @Override
    public RNNPolarityModel train(LabeledTextSet<Polarity> trainingSet) {
        return new RNNPolarityModel(fit(trainingSet));
    }
}
