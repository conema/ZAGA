package com.bupsolutions.polaritydetection.nlp;


import com.bupsolutions.polaritydetection.Settings;

import java.io.FileNotFoundException;

public class EnglishNLPProcessor extends NLPProcessor {
    public EnglishNLPProcessor() {
        try {
            sentenceDetector = new SentenceDetector(Settings.EN_SENTENCE_DETECTOR);
            tokenizer = new Tokenizer(Settings.EN_TOKENIZER);
            posTagger = new POSTagger(Settings.EN_POS_TAGGER);
            lemmatizer = new Lemmatizer();
            stopWordsFilter = new StopWordsFilter(Settings.EN_STOPWORDS);
        } catch (NLPModelLoadingException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
