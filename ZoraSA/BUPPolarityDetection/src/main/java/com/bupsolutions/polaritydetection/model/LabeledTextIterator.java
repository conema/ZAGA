package com.bupsolutions.polaritydetection.model;

import com.bupsolutions.polaritydetection.nlp.NLPProcessor;
import com.bupsolutions.polaritydetection.features.WordVectorsManager;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LabeledTextIterator<T extends Label> implements DataSetIterator {

    private final int batchSize;
    private final int truncateLength;
    private final LabeledTextSet<T> data;

    private int cursor = 0;


    public LabeledTextIterator(LabeledTextSet<T> data, int batchSize, int truncateLength) {
        this.data = data;
        this.batchSize = batchSize;
        this.truncateLength = truncateLength;
    }


    @Override
    public DataSet next(int size) {
        try{
            return nextDataSet(size);
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    private DataSet nextDataSet(int size) throws IOException {
        LabeledTextSet<?> sample = nextSample(size);
        List<List<String>> tokensMatrix = NLPProcessor.getInstance().processAll(sample.texts());

        int maxLength = tokensMatrix.stream().mapToInt(List::size).max().orElse(0);

        if (maxLength > truncateLength) {
            maxLength = truncateLength;
        }

        INDArray features = Nd4j.create(sample.size(), WordVectorsManager.getVectors().size(), maxLength);
        INDArray labels = Nd4j.create(sample.size(), 2, maxLength);
        INDArray featuresMask = Nd4j.zeros(sample.size(), maxLength);
        INDArray labelsMask = Nd4j.zeros(sample.size(), maxLength);

        int[] temp = new int[2];
        for (int i = 0; i < sample.size(); i++) {
            String[] tokens = tokensMatrix.get(i).toArray(new String[]{});
            temp[0] = i;

            for (int j = 0; j < tokens.length && j < maxLength; j++) {
                String token = tokens[j];
                INDArray vector = WordVectorsManager.getVectors().getWordVectorMatrix(token);
                features.put(new INDArrayIndex[]{NDArrayIndex.point(i), NDArrayIndex.all(), NDArrayIndex.point(j)}, vector);

                temp[1] = j;
                featuresMask.putScalar(temp, 1.0);
            }

            int idx = sample.get(i).getLabel().asInt();
            int lastIdx = Math.min(tokens.length, maxLength);
            labels.putScalar(new int[]{i, idx, lastIdx - 1}, 1.0);
            labelsMask.putScalar(new int[]{i, lastIdx - 1}, 1.0);
        }

        return new DataSet(features,labels,featuresMask,labelsMask);
    }

    public LabeledTextSet<?> nextSample(int size) {
        int index = Math.min(cursor + size, totalExamples());
        LabeledTextSet<?> sample = data.sample(cursor, index);
        cursor = index;
        return sample;
    }

    @Override
    public int totalExamples() {
        return data.size();
    }

    @Override
    public int inputColumns() {
        return WordVectorsManager.getVectors().size();
    }

    @Override
    public int totalOutcomes() {
        return 2;
    }

    @Override
    public void reset() {
        cursor = 0;
    }

    public boolean resetSupported() {
        return true;
    }

    @Override
    public boolean asyncSupported() {
        return true;
    }

    @Override
    public int batch() {
        return batchSize;
    }

    @Override
    public int cursor() {
        return cursor;
    }

    @Override
    public int numExamples() {
        return totalExamples();
    }

    @Override
    public void setPreProcessor(DataSetPreProcessor preProcessor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getLabels() {
        Label[] values = data.get(0).getLabel().labels();
        return Arrays.stream(values).map(Object::toString).collect(Collectors.toList());
    }

    @Override
    public boolean hasNext() {
        return cursor < numExamples();
    }

    @Override
    public DataSet next() {
        return next(batchSize);
    }

    @Override
    public void remove() {

    }
    @Override
    public  DataSetPreProcessor getPreProcessor() {
        throw new UnsupportedOperationException("Not implemented");
    }

}
