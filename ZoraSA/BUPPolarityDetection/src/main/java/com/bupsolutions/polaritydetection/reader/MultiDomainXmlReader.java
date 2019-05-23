package com.bupsolutions.polaritydetection.reader;

import com.bupsolutions.polaritydetection.model.LabeledTextSet;
import com.bupsolutions.polaritydetection.model.Polarity;

import java.util.Arrays;
import java.util.Random;

public class MultiDomainXmlReader implements DataSetReader<Polarity> {

    private String path;

    public MultiDomainXmlReader(String path) {
        this.path = path;
    }

    public LabeledTextSet<Polarity> read() {
        String[] domains = {"books", "dvd", "electronics", "kitchen_&_housewares"};

        LabeledTextSet<Polarity> dataset = Arrays.stream(domains).map(this::readReviews)
                .reduce((a, b) -> {
                    a.addAll(b);
                    return a;
                }).orElse(new LabeledTextSet<>());

        dataset.shuffle(new Random(39));
        return dataset;
    }

    public LabeledTextSet<Polarity> readReviews(String domain) {
        String[] files = {"positive.review", "negative.review", "unlabeled.review"};

        LabeledTextSet<Polarity> dataset = Arrays.stream(files).map(file -> {
            try {
                return new AmazonXMLReader(path + file, "review_text", "rating").read();
            } catch (Exception e) {
                System.out.println("Cannot read " + path + file);
                return new LabeledTextSet<Polarity>();
            }
        })
        .reduce((a, b) -> {
            a.addAll(b);
            return a;
        }).orElse(new LabeledTextSet<>());

        dataset.shuffle(new Random(39));

        return dataset;

    }

}
