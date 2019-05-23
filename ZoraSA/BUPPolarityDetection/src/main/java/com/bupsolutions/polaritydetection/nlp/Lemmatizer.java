package com.bupsolutions.polaritydetection.nlp;

import com.bupsolutions.polaritydetection.model.TaggedWord;
import opennlp.tools.lemmatizer.DictionaryLemmatizer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

public class Lemmatizer {
    DictionaryLemmatizer lemmatizer;

    public Lemmatizer(String path) throws NLPModelLoadingException {
        try {
            InputStream inputStream = new FileInputStream(path);
            lemmatizer = new DictionaryLemmatizer(inputStream);
        } catch (IOException e) {
            throw new NLPModelLoadingException(path, e);
        }
    }

    public Lemmatizer() {

    }

    public List<TaggedWord> lemmatize(List<TaggedWord> taggedWords) {
        if (lemmatizer == null) {
            return taggedWords;
        }

        return taggedWords.stream().map(taggedWord -> {
            String word = taggedWord.getWord();
            String tag = taggedWord.getTag();
            String lemma = lemmatizer.apply(word, tag);
            if (lemma.equals("O")) {
                return new TaggedWord(word, tag);
            } else {
                return new TaggedWord(lemma, tag);
            }
        }).collect(Collectors.toList());
    }

    public List<List<TaggedWord>> lemmatizeAll(List<List<TaggedWord>> taggedWords) {
        return taggedWords.stream()
                .map(this::lemmatize)
                .collect(Collectors.toList());
    }

}
