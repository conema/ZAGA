package com.bupsolutions.polaritydetection.features;

import org.deeplearning4j.berkeley.Pair;
import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.wordstore.VocabCache;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class WordVectors {
    private static final int MAX_LENGTH = 256;
    private org.deeplearning4j.models.embeddings.wordvectors.WordVectors wordVectors;
    private int size;

    public WordVectors(String path) {
        load(path);
    }

    public void load(String path) {
        if (path.endsWith(".txt")) {
            loadTxt(path);
        } else {
            loadBin(path);
        }
    }

    public void loadBin(String path) {
        wordVectors = WordVectorSerializer.readWord2VecModel(path);
        size = wordVectors.getWordVector(wordVectors.vocab().wordAtIndex(0)).length;
    }

    public void loadTxt(String path) {
        try {
            Pair<InMemoryLookupTable, VocabCache> pair = WordVectorSerializer.loadTxt(new File(path));
            wordVectors = WordVectorSerializer.fromPair(pair);
            size = wordVectors.getWordVector(wordVectors.vocab().wordAtIndex(0)).length;
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            throw new WordVectorsLoadingException("Could not load word vectors at " + path);
        }
    }

    public INDArray extractFeatures(String[] context) {
        List<String> tokensFiltered = new ArrayList<>();
        for(String t : context) {
            if (wordVectors.hasWord(t)) {
                tokensFiltered.add(t);
            }
        }
        int outputLength = Math.max(MAX_LENGTH, tokensFiltered.size());

        INDArray features = Nd4j.create(1, size, outputLength);

        for (int j = 0; j < context.length && j < MAX_LENGTH; j++) {
            String token = context[j];
            INDArray vector = wordVectors.getWordVectorMatrix(token);
            features.put(new INDArrayIndex[]{NDArrayIndex.point(0), NDArrayIndex.all(), NDArrayIndex.point(j)}, vector);
        }

        return features;
    }

    public INDArray getWordVectorMatrix(String word) {
        return wordVectors.getWordVectorMatrix(word);
    }

    public int size() {
        return size;
    }
}
