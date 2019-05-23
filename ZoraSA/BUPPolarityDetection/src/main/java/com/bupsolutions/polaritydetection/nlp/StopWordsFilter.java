package com.bupsolutions.polaritydetection.nlp;

import com.bupsolutions.polaritydetection.model.TaggedWord;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

public class StopWordsFilter {

    private Set<String> stopwords = new HashSet<>();

    public StopWordsFilter(String path) throws FileNotFoundException {
        Scanner s = new Scanner(new File(path));
        while (s.hasNextLine()){
            stopwords.add(s.nextLine());
        }
        s.close();
    }

    public List<TaggedWord> filterTaggedSentence(List<TaggedWord> sentence) {
        return sentence.stream()
                .filter(taggedWord -> !isStopWord(taggedWord.getWord()))
                .collect(Collectors.toList());
    }

    public List<List<TaggedWord>> filterTaggedSentences(List<List<TaggedWord>> sentences) {
        return sentences.stream()
                .map(this::filterTaggedSentence)
                .collect(Collectors.toList());
    }

    public boolean isStopWord(String word) {
        return word.matches("(\\W)+") || stopwords.contains(word);
    }

}
