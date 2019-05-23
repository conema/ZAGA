package com.bupsolutions.polaritydetection.ml.sentiwordnet;


import com.bupsolutions.polaritydetection.ml.ClassificationModel;
import com.bupsolutions.polaritydetection.ml.SubjectivityHandler;
import com.bupsolutions.polaritydetection.ml.SubjectivityModel;
import com.bupsolutions.polaritydetection.model.Subjectivity;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class SentiWordNetSubjectivityModel extends SentiWordNetEnhancedModel<Subjectivity> implements SubjectivityModel {

    public SentiWordNetSubjectivityModel(ClassificationModel<Subjectivity> model, String sentiWordNetPath) throws IOException {
        super(model, sentiWordNetPath);
    }

    @Override
    public Subjectivity eval(String message) {
        Map<Subjectivity, Double> map = getProbabilities(message);
        return new SubjectivityHandler().getSubjectivity(map);
    }

    @Override
    public Map<Subjectivity, Double> getProbabilities(String message) {
        Map<Subjectivity, Double> map = getModel().getProbabilities(message);
        double subScore = map.get(Subjectivity.SUBJECTIVE);
        List<Double> swnScores = getScores(message);
        double infScore = Math.abs(infScore(swnScores));
        double euclScore = Math.abs(euclScore(swnScores));
        if (infScore > 0.4 || euclScore > 0.4) {
            infScore = (infScore + 1) / 2;
            subScore = 0.15 * subScore + 0.85 * infScore;
        }
        double objScore = 1 - subScore;
        map.put(Subjectivity.SUBJECTIVE, subScore);
        map.put(Subjectivity.OBJECTIVE, objScore);
        return map;
    }
}
