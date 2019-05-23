package com.bupsolutions.polaritydetection.nlp;

import opennlp.tools.ngram.NGramGenerator;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class NGramExtractor {
    private static final String SEPARATOR = " ";

    public List<String> ngrams(List<String> tokens, int n) {
        return NGramGenerator.generate(tokens, n, SEPARATOR);
    }

    public List<List<String>> mapNGrams(List<List<String>> sentences,  int n) {
        return sentences.stream().map(sentence ->
                ngrams(sentence, n)
        ).collect(Collectors.toList());
    }

    public List<String> flatMapNGrams(List<List<String>> sentences,  int n) {
        return mapNGrams(sentences, n).stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

}
