package com.bupsolutions.polaritydetection.nlp;


import com.bupsolutions.polaritydetection.Settings;
import com.bupsolutions.polaritydetection.model.TaggedWord;

import java.util.*;
import java.util.stream.Collectors;

public abstract class NLPProcessor {

    SentenceDetector sentenceDetector;
    Tokenizer tokenizer;
    POSTagger posTagger;
    Lemmatizer lemmatizer;
    StopWordsFilter stopWordsFilter;

    private static Map<String, NLPProcessor> map = new HashMap<>();

    public static NLPProcessor getInstance() {
        return map.computeIfAbsent(Settings.lang, k -> newInstance());
    }

    private static NLPProcessor newInstance() {
        if (Settings.lang.equals("it")) {
            return new ItalianNLPProcessor();
        }
        return new EnglishNLPProcessor();
    }

    public List<TaggedWord> tag(String text) {
        List<String> sentences = sentenceDetector.detectSentences(text);
        List<List<String>> tokens = tokenizer.tokenizeAll(sentences);

        List<TaggedWord> taggedWords = posTagger.tagAll(tokens).stream()
                .flatMap(Collection::stream).collect(Collectors.toList());

        List<TaggedWord> lowerCase = taggedWords.stream()
                .map(w -> new TaggedWord(w.getWord().toLowerCase(), w.getTag()))
                .collect(Collectors.toList());

        List<TaggedWord> taggedLemmas = lemmatizer.lemmatize(lowerCase);

        List<TaggedWord> filteredLemmas = stopWordsFilter.filterTaggedSentence(taggedLemmas);

        return filteredLemmas;
    }

    public String[] process(String text) {
        List<TaggedWord> tags = tag(text);
        return tags.stream().map(TaggedWord::getWord).toArray(String[]::new);
    }

    public List<List<String>> processAll(List<String> data) {
        return data.stream()
                .map(s -> Arrays.asList(process(s)))
                .collect(Collectors.toList());
    }
}