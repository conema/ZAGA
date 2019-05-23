package com.bupsolutions.polaritydetection.ml.sentiwordnet;

import com.bupsolutions.polaritydetection.ml.ClassificationModel;
import com.bupsolutions.polaritydetection.model.Label;
import com.bupsolutions.polaritydetection.model.TaggedWord;
import com.bupsolutions.polaritydetection.nlp.NLPProcessor;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class SentiWordNetEnhancedModel<T extends Label> implements ClassificationModel<T> {

    private ClassificationModel<T> model;
    private SentiWordNet sentiWordNet;

    public SentiWordNetEnhancedModel(ClassificationModel<T> model, String sentiWordNetPath) throws IOException {
        if (model == null) {
            throw new IllegalArgumentException("Model cannot be null");
        }

        this.model = model;
        this.sentiWordNet = new SentiWordNet(sentiWordNetPath);
    }

    @Override
    public void save(String path) throws IOException {
        model.save(path);
    }

    public ClassificationModel<T> getModel() {
        return model;
    }

    private String pos(String tag) {
        if (tag.startsWith("J")) {
            return "a";
        }
        return tag.substring(0, 1).toLowerCase();
    }

    protected List<Double> getScores(String text) {
        List<TaggedWord> tags = NLPProcessor.getInstance().tag(text);
        return tags.stream().map(t -> sentiWordNet.extract(t.getWord(), pos(t.getTag())))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    protected double infScore(List<Double> scores) {
        if (scores == null || scores.isEmpty()) {
            return 0.0;
        }
        double max = Collections.max(scores);
        double min = Collections.min(scores);

        if (Math.abs(min) > max) {
            return min;
        }

        return max;
    }

    protected double euclScore(List<Double> scores) {
        double sum = 0;
        int sign;
        for (double score : scores) {
            sign = score >= 0 ? 1 : -1;
            double square = score * score;
            sum += sign * square;
        }

        sign = sum >= 0 ? 1 : -1;
        return sign * Math.sqrt(Math.abs(sum));
    }

    protected double sumScore(List<Double> scores) {
        return scores.stream().reduce((a, b) -> a + b).orElse(0.0);
    }

    protected double avgScore(double threshold, List<Double> scores) {
        List<Double> filterd = scores.stream()
                .filter(s -> Math.abs(s) >= threshold)
                .collect(Collectors.toList());

        double size = filterd.size();

        if (size == 0) {
            return 0;
        }

        return filterd.stream().reduce((a, b) -> a + b).orElse(0.0) / size;
    }

    protected double sigmoidScore(double k, double x) {
        if (k > 0) {
            k = -k;
        }

        double e = 2.718281828459;

        return 1 / (1 + Math.pow(e, k * x));
    }

    protected double hyperbolicScore(double x) {
        double k = 0.08;
        double d = 1.0;
        double c = -d / (1 + k);
        double a = 3.0 / 2.0 * c + d;
        double b = d / 2 + c / 4 - a / 2;

        if (x < 0.5) {
            double x2 = 1 - x;
            return 1 - (a * x2 + b) / (c * x2 + d);
        }

        return (a * x + b) / (c * x + d);
    }
}
