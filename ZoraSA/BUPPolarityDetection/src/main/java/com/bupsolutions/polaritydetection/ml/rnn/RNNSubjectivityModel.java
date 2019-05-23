package com.bupsolutions.polaritydetection.ml.rnn;

import com.bupsolutions.polaritydetection.features.WordVectorsManager;
import com.bupsolutions.polaritydetection.ml.SubjectivityHandler;
import com.bupsolutions.polaritydetection.ml.SubjectivityModel;
import com.bupsolutions.polaritydetection.model.Subjectivity;
import com.bupsolutions.polaritydetection.nlp.NLPProcessor;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class RNNSubjectivityModel extends RNNModel<Subjectivity> implements SubjectivityModel {

    private SubjectivityHandler handler = new SubjectivityHandler();

    public RNNSubjectivityModel() {
    }

    public RNNSubjectivityModel(MultiLayerNetwork rnn) {
        super(rnn);
    }

    public RNNSubjectivityModel(File model) throws IOException {
        super(model);
    }

    public RNNSubjectivityModel(String path) throws IOException {
        super(path);
    }

    @Override
    public Subjectivity eval(String message) {
        Map<Subjectivity, Double> probabilities = getProbabilities(message);
        return handler.getSubjectivity(probabilities);
    }

    @Override
    public Map<Subjectivity, Double> getProbabilities(String message) {
        String[] context = NLPProcessor.getInstance().process(message);
        INDArray features = WordVectorsManager.getVectors().extractFeatures(context);

        INDArray probabilitiesAtLastWord = probabilitiesAtLastInstance(features);

        double subjProb = probabilitiesAtLastWord.getDouble(Subjectivity.SUBJECTIVE.asInt());

        return handler.mapProbabilities(subjProb);
    }
}
