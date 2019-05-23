package com.bupsolutions.polaritydetection.ml.rnn;

import com.bupsolutions.polaritydetection.ml.Evaluator;
import com.bupsolutions.polaritydetection.model.Label;
import com.bupsolutions.polaritydetection.model.LabeledTextIterator;
import com.bupsolutions.polaritydetection.model.LabeledTextSet;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;

public class RNNEvaluator<T extends Label> extends Evaluator<RNNModel<?>> {
    public RNNEvaluator(RNNModel<T> model) {
        super(model);
    }

    public void printEvaluationStats(LabeledTextSet<T> testSet) {
        LabeledTextIterator<T> testIterator = new LabeledTextIterator<>(testSet, model.getBatchSize(), model.getMaxLength());
        MultiLayerNetwork rnn = model.getNeuralNetwork();

        Evaluation evaluation = new Evaluation();
        while (testIterator.hasNext()) {
            DataSet t = testIterator.next();
            INDArray features = t.getFeatureMatrix();
            INDArray labels = t.getLabels();
            INDArray inMask = t.getFeaturesMaskArray();
            INDArray outMask = t.getLabelsMaskArray();
            INDArray predicted = rnn.output(features, false, inMask, outMask);

            evaluation.evalTimeSeries(labels, predicted, outMask);
        }
        testIterator.reset();

        System.out.println(evaluation.stats());
    }

}
