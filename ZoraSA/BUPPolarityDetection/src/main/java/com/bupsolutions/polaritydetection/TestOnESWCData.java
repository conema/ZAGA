package com.bupsolutions.polaritydetection;

import com.bupsolutions.polaritydetection.features.WordVectorsManager;
import com.bupsolutions.polaritydetection.ml.rnn.RNNEvaluator;
import com.bupsolutions.polaritydetection.ml.rnn.RNNPolarityModel;
import com.bupsolutions.polaritydetection.ml.rnn.RNNPolarityTrainer;
import com.bupsolutions.polaritydetection.model.LabeledTextSet;
import com.bupsolutions.polaritydetection.model.Polarity;
import com.bupsolutions.polaritydetection.reader.AmazonXMLReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class TestOnESWCData {
    public static void main(String[] args) throws Exception {
        LabeledTextSet<Polarity> train = loadData("../data/en/eswc2017_challenge/Task1/train");
        LabeledTextSet<Polarity> test = loadData("../data/en/eswc2017_challenge/Task1/test");
        train.addAll(test);
        long pos = train.stream().filter(x -> x.getLabel().equals(Polarity.POSITIVE)).count();
        long neg = train.stream().filter(x -> x.getLabel().equals(Polarity.NEGATIVE)).count();
        long size = train.size();

        System.out.println(pos + " | " + neg +  " | " + size);
    }

    private static void testESWC17() throws IOException {
        Random prg = new Random(31);
        LabeledTextSet<Polarity> train = loadData("../data/en/eswc2017_challenge/Task1/train");
        LabeledTextSet<Polarity> test = loadData("../data/en/eswc2017_challenge/Task1/test");

        train.shuffle(prg);
        test.shuffle(prg);
        train = train.sample(0, 25000);
        test = test.sample(0, 25000);

        System.out.println("Loading word vectors...");
        WordVectorsManager.load("en", Settings.GLOVE_6B_300);

        System.out.println("Loading Model...");
        RNNPolarityModel model = new RNNPolarityModel("en-rnn.zip");

        System.out.println("Training...");
        model = new RNNPolarityTrainer(model).train(train);
        model.save("new-rnn");

        System.out.println("Running Evaluation...");
        new RNNEvaluator<>(model).printEvaluationStats(test);
    }

    private static LabeledTextSet<Polarity> loadData(String path) {
        LabeledTextSet<Polarity> data = new LabeledTextSet<>();

        FileUtils.listFiles(new File(path),
                FileFilterUtils.suffixFileFilter(".xml"),
                TrueFileFilter.INSTANCE).forEach(file -> {
                    try {
                        data.addAll(
                                new AmazonXMLReader(file.getAbsolutePath(), "text", "polarity").read());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
        return data;
    }
}
