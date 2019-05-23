package com.bupsolutions.polaritydetection.model;

import java.util.ArrayList;
import java.util.List;

public class TaggedWord {
    private String word;
    private String tag;

    public TaggedWord(String word, String tag) {
        this.word = word;
        this.tag = tag;
    }

    public String getWord() {
        return word;
    }

    public String getTag() {
        return tag;
    }

    @Override
    public String toString() {
        return tag + "=" + word;
    }

    public static List<TaggedWord> combine(String[] words, String[] tags) {
        int length = tags.length;
        List<TaggedWord> taggedWords = new ArrayList<>(length);

        for (int i = 0; i < length; i++) {
            TaggedWord current = new TaggedWord(words[i], tags[i]);
            taggedWords.add(current);
        }

        return taggedWords;
    }
}
