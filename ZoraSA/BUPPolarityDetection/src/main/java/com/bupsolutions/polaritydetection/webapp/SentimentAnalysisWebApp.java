package com.bupsolutions.polaritydetection.webapp;


import com.bupsolutions.polaritydetection.ml.PolarityModel;
import com.bupsolutions.polaritydetection.ml.SubjectivityModel;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;
import java.io.IOException;

@ApplicationPath("sa")
public class SentimentAnalysisWebApp extends ResourceConfig {

    private static PolarityModel polarityModel;
    private static SubjectivityModel subjectivityModel;

    public SentimentAnalysisWebApp() throws IOException {
        register(ContainerListener.class);
        packages("com.bupsolutions.polaritydetection.webapp.api");
    }

    public static PolarityModel getPolarityModel() {
        return polarityModel;
    }

    public static void setPolarityModel(PolarityModel polarityModel) {
        SentimentAnalysisWebApp.polarityModel = polarityModel;
    }

    public static SubjectivityModel getSubjectivityModel() {
        return subjectivityModel;
    }

    public static void setSubjectivityModel(SubjectivityModel subjectivityModel) {
        SentimentAnalysisWebApp.subjectivityModel = subjectivityModel;
    }
}
