package com.bupsolutions.polaritydetection;

import com.bupsolutions.polaritydetection.features.WordVectorsManager;
import com.bupsolutions.polaritydetection.ml.Evaluator;
import com.bupsolutions.polaritydetection.ml.rnn.RNNEvaluator;
import com.bupsolutions.polaritydetection.ml.rnn.RNNPolarityModel;
import com.bupsolutions.polaritydetection.ml.rnn.RNNSubjectivityModel;
import com.bupsolutions.polaritydetection.ml.sentiwordnet.SentiWordNetPolarityModel;
import com.bupsolutions.polaritydetection.ml.sentiwordnet.SentiWordNetSubjectivityModel;
import com.bupsolutions.polaritydetection.model.LabeledTextSet;
import com.bupsolutions.polaritydetection.model.Polarity;
import com.bupsolutions.polaritydetection.model.Subjectivity;
import com.bupsolutions.polaritydetection.reader.IMDbReviewsReader;
import com.bupsolutions.polaritydetection.utils.Times;
import org.joda.time.Duration;

import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

public class Main {

    //private static ArgumentsHandler argumentsHandler;

    public static void main(String[] args) throws Exception {

        try {
            interactivePolarityDetection();
        } catch (Exception e) {
            String message = e.getMessage();
            if (message == null) {
                message = "Something went wrong";
            }
            System.out.println(message);
            e.printStackTrace();
        }

        /*
        Settings.lang = "it";
        WordVectorsManager.load("it", Settings.GLOVE_ITA);

        PolarityModel polarityModel = new RNNPolarityModel("en-rnn.zip");
        polarityModel = new SentiWordNetPolarityModel(polarityModel, Settings.SENTI_WORD_NET_PATH);

        LabeledTextSet<Polarity> data = new Sentipolc2016Reader(Settings.SENTIPOLC2016_TRAINING_DATA).read().sample(0, 2000);

        System.out.println("Starting evaluation...");
        long tic = System.currentTimeMillis();
        double acc = new Evaluator<>(polarityModel).accuracy(data);
        long toc = System.currentTimeMillis();
        System.out.println("Tic - toc: " + (tic - toc));

        System.out.println("Acc = " + acc);
        */


    }

    private static void times() throws Exception {
        System.out.println("Loading word vectors...");
        WordVectorsManager.load("en", Settings.GLOVE_6B_300);

        System.out.println("Loading Model...");
        RNNPolarityModel tmpModel = new RNNPolarityModel("en-rnn.zip");
        SentiWordNetPolarityModel model = new SentiWordNetPolarityModel(tmpModel, Settings.SENTI_WORD_NET_PATH);

        LabeledTextSet<Polarity> data = new IMDbReviewsReader(Settings.IMDB_TEST_DATA).read().sample(0, 1000);

        System.out.println("Starting evaluation...");
        long tic = System.currentTimeMillis();
        double acc = new Evaluator<>(model).accuracy(data);
        long toc = System.currentTimeMillis();
        System.out.println("Tic - toc: " + (tic - toc));

        System.out.println("Acc = " + acc);
        double preprocessing = Times.preprocessing.stream().mapToDouble(Duration::getMillis).average().getAsDouble();
        double wordvectors = Times.wordvectors.stream().mapToDouble(Duration::getMillis).average().getAsDouble();
        double classification = Times.classification.stream().mapToDouble(Duration::getMillis).average().getAsDouble();
        double sentiwordnet = Times.sentiwordnet.stream().mapToDouble(Duration::getMillis).average().getAsDouble();
        double lengths = Times.lengths.stream().mapToDouble(x -> x).average().getAsDouble();

        System.out.println(preprocessing);
        System.out.println(wordvectors);
        System.out.println(classification);
        System.out.println(sentiwordnet);
        System.out.println(lengths);
    }

    private static void train() throws IOException {
        System.out.println("Reading training data...");
        LabeledTextSet<Polarity> data = new IMDbReviewsReader(Settings.IMDB_TEST_DATA).read();

        System.out.println("Loading word vectors...");
        WordVectorsManager.load("en", Settings.GLOVE_6B_300);

        System.out.println("Running evaluation...");
        RNNPolarityModel model = new RNNPolarityModel("en-rnn.zip");

        System.out.println("Starting evaluation...");
        new RNNEvaluator<>(model).printEvaluationStats(data);
    }

    private static void interactiveSubjectivityDetection() throws IOException {
        System.out.println("Loading word vectors...");
        WordVectorsManager.load("en", Settings.GLOVE_6B_300);

        System.out.println("Loading Model...");
        RNNSubjectivityModel tmpModel = new RNNSubjectivityModel("subj-rnn.zip");
        SentiWordNetSubjectivityModel model = new SentiWordNetSubjectivityModel(tmpModel, Settings.SENTI_WORD_NET_PATH);

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Insert message: ");
            String message = scanner.nextLine();
            Map<Subjectivity, Double> probabilities = model.getProbabilities(message);

            System.out.println("P(Subjective): " + probabilities.get(Subjectivity.SUBJECTIVE));
            System.out.println("P(Objective): " + probabilities.get(Subjectivity.OBJECTIVE));

            System.out.println();
        }
    }

    private static void interactivePolarityDetection() throws IOException {
        System.out.println("Loading word vectors...");
        WordVectorsManager.load("en", Settings.GLOVE_6B_300);

        System.out.println("Loading Model...");
        RNNPolarityModel tmpModel = new RNNPolarityModel("en-rnn.zip");
        SentiWordNetPolarityModel model = new SentiWordNetPolarityModel(tmpModel, Settings.SENTI_WORD_NET_PATH);

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Insert message: ");
            String message = scanner.nextLine();
            Map<Polarity, Double> probabilities = model.getProbabilities(message);

            System.out.println("P(Positive): " + probabilities.get(Polarity.POSITIVE));
            System.out.println("P(Negative): " + probabilities.get(Polarity.NEGATIVE));

            System.out.println();
        }
    }

}
