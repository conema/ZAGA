package com.bupsolutions.polaritydetection.ml;

import com.bupsolutions.polaritydetection.model.Subjectivity;

import java.util.HashMap;
import java.util.Map;

public class SubjectivityHandler {

    public Map<Subjectivity, Double> mapProbabilities(double subjProb) {
        double objProb = 1 - subjProb;

        Map<Subjectivity, Double> result = new HashMap<>();
        result.put(Subjectivity.SUBJECTIVE, subjProb);
        result.put(Subjectivity.OBJECTIVE, objProb);

        return result;
    }


    public Subjectivity getSubjectivity(Map<Subjectivity, Double> probabilities) {
        Double subj = probabilities.get(Subjectivity.SUBJECTIVE);
        return subj >= 0.5 ? Subjectivity.SUBJECTIVE : Subjectivity.OBJECTIVE;
    }
}
