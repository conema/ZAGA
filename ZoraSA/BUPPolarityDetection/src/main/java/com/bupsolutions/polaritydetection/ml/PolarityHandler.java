package com.bupsolutions.polaritydetection.ml;


import com.bupsolutions.polaritydetection.model.Polarity;

import java.util.HashMap;
import java.util.Map;

public class PolarityHandler {

    private boolean handleNegations = true;

    public Map<Polarity, Double> mapProbabilities(String[] context, double posProb) {

        if (handleNegations) {
            posProb = NegationsHandler.fixPositiveProbability(posProb, context);
        }
        double negProb = 1 - posProb;

        if (context.length <= 2) {
            if (posProb > 0.9) {
                posProb *= 0.82;
                negProb = 1 - posProb;
            }
        }

        Map<Polarity, Double> result = new HashMap<>();
        result.put(Polarity.POSITIVE, posProb);
        result.put(Polarity.NEGATIVE, negProb);

        return result;
    }

    public Polarity getPolarity(Map<Polarity, Double> probabilities) {
        Double pos = probabilities.get(Polarity.POSITIVE);
        return pos >= 0.5 ? Polarity.POSITIVE : Polarity.NEGATIVE;
    }

    public void handleNegations(boolean flag) {
        this.handleNegations = flag;
    }
}
