package com.bupsolutions.polaritydetection.nlp;

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Tokenizer {

    private TokenizerME tokenizer;

    public Tokenizer(String path) throws NLPModelLoadingException {
        try {
            InputStream inputStream = new FileInputStream(path);
            TokenizerModel tokenModel = new TokenizerModel(inputStream);
            tokenizer = new TokenizerME(tokenModel);
        } catch (IOException e) {
            throw new NLPModelLoadingException(path);
        }
    }

    public List<String> tokenize(String text) {
        String[] tokens = tokenizer.tokenize(text);
        return Arrays.asList(tokens);
    }

    public List<List<String>> tokenizeAll(List<String> tokens) {
        return tokens.stream().map(this::tokenize).collect(Collectors.toList());
    }

}
