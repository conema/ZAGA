package com.bupsolutions.polaritydetection.reader;


import com.bupsolutions.polaritydetection.model.LabeledText;
import com.bupsolutions.polaritydetection.model.LabeledTextSet;
import com.bupsolutions.polaritydetection.model.Subjectivity;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.stream.Stream;

public class RottenIMDbSubjectivityReader implements DataSetReader<Subjectivity> {

    private String path;

    public RottenIMDbSubjectivityReader(String path) {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }
        this.path = path;
    }

    @Override
    public LabeledTextSet<Subjectivity> read() throws IOException {
        LabeledTextSet<Subjectivity> data = read(path + "subjective", Subjectivity.SUBJECTIVE);
        data.addAll(read(path + "objective", Subjectivity.OBJECTIVE));
        data.shuffle(new Random(39));

        return data;
    }

    private LabeledTextSet<Subjectivity> read(String path, Subjectivity subjectivity) throws IOException {
        LabeledTextSet<Subjectivity> data = new LabeledTextSet<>();

        try (Stream<String> stream = Files.lines(Paths.get(path), Charset.forName("UTF-8"))) {
            stream.map(review -> new LabeledText<>(review, subjectivity))
                    .forEach(data::add);
        }

        return data;
    }
}
