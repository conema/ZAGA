package com.bupsolutions.polaritydetection.ml.rnn;

import com.bupsolutions.polaritydetection.model.LabeledTextSet;
import com.bupsolutions.polaritydetection.model.Subjectivity;

public class RNNSubjectivityTrainer extends RNNTrainer<Subjectivity> {

    public RNNSubjectivityTrainer() {
        super(new RNNSubjectivityModel());
    }

    public RNNSubjectivityTrainer(RNNModel<Subjectivity> model) {
        super(model);
    }

    @Override
    public RNNSubjectivityModel train(LabeledTextSet<Subjectivity> trainingSet) {
        return new RNNSubjectivityModel(fit(trainingSet));
    }

}
