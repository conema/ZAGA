package com.bupsolutions.polaritydetection.reader;


import com.bupsolutions.polaritydetection.model.LabeledText;
import com.bupsolutions.polaritydetection.model.Polarity;
import com.bupsolutions.polaritydetection.model.LabeledTextSet;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

public class IMDbReviewsReader implements DataSetReader<Polarity> {

    private String path;

    public IMDbReviewsReader(String path) {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }
        this.path = path;
    }

    public LabeledTextSet<Polarity> read() {
        LabeledTextSet<Polarity> data = readPositives();
        data.addAll(readNegatives());
        data.shuffle(new Random(39));
        return data;
    }

    private LabeledTextSet<Polarity> readPositives() {
        List<String> reviews = read("pos");
        return reviews.stream()
            .map(s -> new LabeledText<>(s, Polarity.POSITIVE))
            .collect(LabeledTextSet.collector());
    }

    private LabeledTextSet<Polarity> readNegatives() {
        List<String> reviews = read("neg");
        return reviews.stream()
                .map(s -> new LabeledText<>(s, Polarity.NEGATIVE))
                .collect(LabeledTextSet.collector());
    }

    private List<String> read(String polarity) {
        File dir = new File(path + "/" + polarity);
        File[] positiveReviews = dir.listFiles();

        if (positiveReviews == null) {
            return new ArrayList<>();
        }

        return Arrays.stream(positiveReviews)
            .map(r -> {
                try {
                    return FileUtils.readFileToString(r, (Charset) null);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            })
            .collect(Collectors.toList());
    }
}
