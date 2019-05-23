package com.bupsolutions.polaritydetection.nlp;

import com.bupsolutions.polaritydetection.model.TaggedWord;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

public class POSTagger {

    private POSTaggerME tagger;

    public POSTagger(String path) throws NLPModelLoadingException {
        try {
            InputStream inputStream = new FileInputStream(path);
            POSModel posModel = new POSModel(inputStream);

            tagger = new POSTaggerME(posModel);
        } catch (IOException e) {
            throw new NLPModelLoadingException(path, e);
        }
    }

    public List<TaggedWord> tag(List<String> tokens) {
        String[] words = tokens.toArray(new String[]{});
        String[] tags = tagger.tag(words);
        return TaggedWord.combine(words, tags);
    }

    public List<List<TaggedWord>> tagAll(List<List<String>> sentences) {
        return sentences.stream()
                .map(this::tag)
                .collect(Collectors.toList());
    }



}
