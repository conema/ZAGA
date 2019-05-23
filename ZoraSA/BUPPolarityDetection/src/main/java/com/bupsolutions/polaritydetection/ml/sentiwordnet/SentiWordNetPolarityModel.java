package com.bupsolutions.polaritydetection.ml.sentiwordnet;


import com.bupsolutions.polaritydetection.ml.NegationsHandler;
import com.bupsolutions.polaritydetection.ml.PolarityHandler;
import com.bupsolutions.polaritydetection.ml.PolarityModel;
import com.bupsolutions.polaritydetection.model.Polarity;
import com.bupsolutions.polaritydetection.nlp.NLPProcessor;
import com.bupsolutions.polaritydetection.utils.Times;
import org.joda.time.Duration;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class SentiWordNetPolarityModel extends SentiWordNetEnhancedModel<Polarity> implements PolarityModel {

    public SentiWordNetPolarityModel(PolarityModel model, String sentiWordNetPath) throws IOException {
        super(model, sentiWordNetPath);
    }

    @Override
    public void handleNegations(boolean flag) {
        ((PolarityModel) getModel()).handleNegations(flag);
    }

    @Override
    public Polarity eval(String message) {
        Map<Polarity, Double> map = getProbabilities(message);
        return new PolarityHandler().getPolarity(map);
    }

    @Override
    public Map<Polarity, Double> getProbabilities(String message) {
        Map<Polarity, Double> map = getModel().getProbabilities(message);

        long tic =  System.currentTimeMillis();
        String[] context = NLPProcessor.getInstance().process(message);

        double posProb = map.get(Polarity.POSITIVE);

        if (posProb == 0.5 || NegationsHandler.isNegative(context)) {
            return map;
        }

        List<Double> swnScores = getScores(message);

        double posScore = getPosScore(posProb, swnScores);
        double negScore = 1 - posScore;

        map.put(Polarity.POSITIVE, posScore);
        map.put(Polarity.NEGATIVE, negScore);
        long toc = System.currentTimeMillis();
        Times.sentiwordnet.add(new Duration(tic, toc));
        return map;
    }

    private double getPosScore(double posProb, List<Double> swnScores) {

        if (swnScores == null || swnScores.isEmpty()) {
            return (0.5 + posProb) / 2;
        }

        /*double sumScore = sumScore(swnScores);

        System.out.println("SumScore: " + sumScore);

        double sigmoidScore = sigmoidScore(2.5, sumScore);
        System.out.println("Sigmoid:" + sigmoidScore);

        sigmoidScore = (sigmoidScore / 2);
        if (posProb >= 0.5) {
            sigmoidScore += 0.5;
        }

        System.out.println("SigmoidScaled: " + sigmoidScore);

        double hypSigScore = hyperbolicScore(sigmoidScore);
        System.out.println("HypSigmoid: " + hypSigScore);

        double avgScore = avgScore(0.25, swnScores);
        System.out.println("Avg: " + avgScore);
        avgScore = (avgScore + 1) / 2;
        System.out.println("Avg - [0, 1]: " + avgScore);
        avgScore = (avgScore / 2);
        if (posProb >= 0.5) {
            avgScore += 0.5;
        }

        System.out.println("AvgScaled: " + avgScore);

        double hypAvgScore = hyperbolicScore(avgScore);
        System.out.println("HypAvg: " + hypAvgScore);

        return hypSigScore;*/

        double avgScore = avgScore(0.25, swnScores);

        double sigmoidScore = sigmoidScore(2.5, avgScore);

        sigmoidScore = (sigmoidScore / 2);
        if (posProb >= 0.5) {
            sigmoidScore += 0.5;
        }

        return hyperbolicScore(sigmoidScore);

    }
}
