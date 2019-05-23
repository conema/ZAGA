package com.bupsolutions.polaritydetection.nlp;


import com.bupsolutions.polaritydetection.Settings;

import java.io.FileNotFoundException;

public class ItalianNLPProcessor extends NLPProcessor {
    public ItalianNLPProcessor() {
        try {
            System.out.println("Using italian models");
            sentenceDetector = new SentenceDetector(Settings.IT_SENTENCE_DETECTOR);
            tokenizer = new Tokenizer(Settings.IT_TOKENIZER);
            posTagger = new POSTagger(Settings.IT_POS_TAGGER);
            lemmatizer = new Lemmatizer();
            stopWordsFilter = new StopWordsFilter(Settings.IT_STOPWORDS);
        } catch (NLPModelLoadingException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
