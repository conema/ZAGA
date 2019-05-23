package com.bupsolutions.polaritydetection.ml;


import java.util.Arrays;

public class NegationsHandler {

    private static final String[] negations = {"not", "nt", "no", "n't", "non"};

    public static boolean isNegative(String[] tokens) {
        int count = 0;
        for (String token : tokens) {
            if (Arrays.stream(negations).anyMatch(token::equals)) {
                count++;
            }
        }

        return (count % 2) == 1;
    }

    public static double fixPositiveProbability(double posProb, String[] context) {
        if (context.length == 0) {
            return 0.5;
        }

        if (isNegative(context) && context.length <= 8) {
            if (posProb > 0.7) {
                return 0.4 * posProb;
            } else if (posProb < 0.3) {
                double negProb = 1 - posProb;
                negProb *= 0.4;
                return 1 - negProb;
            }
        }

        return posProb;
    }
}
