package com.bupsolutions.polaritydetection.ml;

import com.bupsolutions.polaritydetection.model.Label;
import com.bupsolutions.polaritydetection.model.LabeledTextSet;

import java.util.Optional;

public class Evaluator<T extends ClassificationModel<?>> {

    protected final T model;

    public Evaluator(T model) {
        this.model = model;
    }

    public double accuracy(LabeledTextSet<?> testSet) {
        Optional<Integer> correctPrevisions = testSet.stream().map(labeledText -> {
            Label predicted = model.eval(labeledText.getText());
            Label gold = labeledText.getLabel();
            return predicted.equals(gold) ? 1 : 0;
        }).reduce((a, b) -> a + b);

        return correctPrevisions.map(v ->
                (double) v / testSet.size()
        ).orElse(0.0);
    }
}
