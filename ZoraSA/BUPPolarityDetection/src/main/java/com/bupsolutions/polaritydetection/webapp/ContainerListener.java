package com.bupsolutions.polaritydetection.webapp;

import com.bupsolutions.polaritydetection.Settings;
import com.bupsolutions.polaritydetection.features.WordVectorsManager;
import com.bupsolutions.polaritydetection.ml.PolarityModel;
import com.bupsolutions.polaritydetection.ml.SubjectivityModel;
import com.bupsolutions.polaritydetection.ml.rnn.RNNPolarityModel;
import com.bupsolutions.polaritydetection.ml.rnn.RNNSubjectivityModel;
import com.bupsolutions.polaritydetection.ml.sentiwordnet.SentiWordNetPolarityModel;
import com.bupsolutions.polaritydetection.ml.sentiwordnet.SentiWordNetSubjectivityModel;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;

import java.io.IOException;

public class ContainerListener implements ContainerLifecycleListener {

    private void loadModels() {
        WordVectorsManager.load("en", Settings.GLOVE_6B_300);
        //WordVectorsManager.load("it", Settings.GLOVE_ITA);
        try {
            DetectorFactory.loadProfile(Settings.LANGUAGE_DETECTION_PROFILES);

            PolarityModel polarityModel = new RNNPolarityModel("en-rnn.zip");
            SentimentAnalysisWebApp.setPolarityModel(new SentiWordNetPolarityModel(polarityModel, Settings.SENTI_WORD_NET_PATH));

            SubjectivityModel subjectivityModel = new RNNSubjectivityModel("subj-rnn.zip");
            SentimentAnalysisWebApp.setSubjectivityModel(new SentiWordNetSubjectivityModel(subjectivityModel, Settings.SENTI_WORD_NET_PATH));

        } catch (IOException | LangDetectException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStartup(Container container) {
        loadModels();
    }

    @Override
    public void onReload(Container container) {
        loadModels();
    }

    @Override
    public void onShutdown(Container container) {

    }
}
