package com.bupsolutions.polaritydetection.nlp;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class SentenceDetector {
    private SentenceDetectorME detector;

    public SentenceDetector(String path) throws NLPModelLoadingException {
        try {
            InputStream inputStream = new FileInputStream(path);
            SentenceModel model = new SentenceModel(inputStream);
            detector = new SentenceDetectorME(model);
        } catch (IOException e) {
            throw new NLPModelLoadingException(path, e);
        }
    }

    public List<String> detectSentences(String text) {
        String[] sentences = detector.sentDetect(text);
        return Arrays.asList(sentences);
    }
}
