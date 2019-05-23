package com.bupsolutions.polaritydetection.ml.rnn;


import com.bupsolutions.polaritydetection.features.WordVectorsManager;
import com.bupsolutions.polaritydetection.ml.ClassificationModel;
import com.bupsolutions.polaritydetection.model.Label;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.IOException;

public abstract class RNNModel<T extends Label> implements ClassificationModel<T> {
    private MultiLayerNetwork rnn;
    private int maxLength = 256;

    public RNNModel() {
        initNeuralNetwork();
    }

    public RNNModel(MultiLayerNetwork rnn) {
        if (rnn == null) {
            throw new IllegalArgumentException("RNN cannot be null");
        }
        this.rnn = rnn;
    }

    public RNNModel(File model) throws IOException {
        rnn = ModelSerializer.restoreMultiLayerNetwork(model);
    }

    public RNNModel(String path) throws IOException {
        this(new File(path));
    }

    private void initNeuralNetwork() {
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .updater(Updater.ADAM).adamMeanDecay(0.9).adamVarDecay(0.999)
                .regularization(true).l2(1e-5)
                .weightInit(WeightInit.XAVIER)
                .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue).gradientNormalizationThreshold(1.0)
                .learningRate(2e-2)
                .list()
                .layer(0, new GravesLSTM.Builder().nIn(WordVectorsManager.getVectors().size()).nOut(maxLength)
                        .activation(Activation.SOFTSIGN).build())
                .layer(1, new RnnOutputLayer.Builder().activation(Activation.SOFTMAX)
                        .lossFunction(LossFunctions.LossFunction.MCXENT).nIn(maxLength).nOut(2).build())
                .pretrain(false).backprop(true)
                .build();

        rnn = new MultiLayerNetwork(conf);
        rnn.init();
        rnn.setListeners(new ScoreIterationListener(1));
    }

    protected INDArray probabilitiesAtLastInstance(INDArray instance) {
        INDArray networkOutput = rnn.output(instance);
        int timeSeriesLength = networkOutput.size(2);
        return networkOutput.get(NDArrayIndex.point(0), NDArrayIndex.all(), NDArrayIndex.point(timeSeriesLength - 1));
    }

    @Override
    public void save(String path) throws IOException {
        ModelSerializer.writeModel(rnn, new File(path), true);
    }

    public int getBatchSize() {
        return 64;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public MultiLayerNetwork getNeuralNetwork() {
        return rnn;
    }
}
