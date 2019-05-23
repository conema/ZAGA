package com.bupsolutions.polaritydetection.webapp.api;


import com.bupsolutions.polaritydetection.Settings;
import com.bupsolutions.polaritydetection.ml.PolarityModel;
import com.bupsolutions.polaritydetection.ml.SubjectivityModel;
import com.bupsolutions.polaritydetection.model.Polarity;
import com.bupsolutions.polaritydetection.model.Subjectivity;
import com.bupsolutions.polaritydetection.webapp.SentimentAnalysisWebApp;
import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.Map;

@Path("service")
public class MainClassification {

    private PolarityModel polarityModel;
    private SubjectivityModel subjectivityModel;

    @POST
    public String classify(String text) {
        try {

//            setLanguage(text);
//            String result = "Language: " + Settings.lang;
//
//            Map<Polarity, Double> polarityProbabilities = polarityModel.getProbabilities(text);
//            result += "\n\n" + getPolarityStats(polarityProbabilities);
//
//            if (Settings.lang.equals("en")) {
//                Map<Subjectivity, Double> subjProbabilities = subjectivityModel.getProbabilities(text);
//                result += "\n\n" + getSubjectivityStats(subjProbabilities);
//            }
//
//            return result;

            setLanguage(text);
            if (subjectivityModel.eval(text).equals(Subjectivity.OBJECTIVE)) {
                return "Neutral";
            }
            return polarityModel.eval(text).toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getSubjectivityStats(Map<Subjectivity, Double> probabilities) {
        Double subProbability = probabilities.get(Subjectivity.SUBJECTIVE);
        Double objProbability = probabilities.get(Subjectivity.OBJECTIVE);
        Subjectivity subjectivity = subProbability > 0.5 ? Subjectivity.SUBJECTIVE : Subjectivity.OBJECTIVE;
        return "Subjectivity: " + subjectivity + "\n" +
                "P(Subjective) = " + subProbability + "\n" +
                "P(Objective) = " + objProbability + "\n";
    }

    private String getPolarityStats(Map<Polarity, Double> probabilities) {
        Double posProbability = probabilities.get(Polarity.POSITIVE);
        Double negProbability = probabilities.get(Polarity.NEGATIVE);
        Polarity polarity = posProbability > 0.5 ? Polarity.POSITIVE : Polarity.NEGATIVE;
        return "Polarity: " + polarity + "\n" +
                "P(Positive) = " + posProbability + "\n" +
                "P(Negative) = " + negProbability + "\n";
    }

    private void setLanguage(String message) throws LangDetectException {
        Detector detector = DetectorFactory.create();
        detector.append(message);
        String lang = detector.detect();
        polarityModel = SentimentAnalysisWebApp.getPolarityModel();
        subjectivityModel = SentimentAnalysisWebApp.getSubjectivityModel();

        if (lang.equals("it")) {
            Settings.lang = "it";
            return;
        }

        Settings.lang = "en";
    }
}