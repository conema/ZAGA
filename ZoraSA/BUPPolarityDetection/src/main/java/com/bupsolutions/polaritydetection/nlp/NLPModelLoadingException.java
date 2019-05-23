package com.bupsolutions.polaritydetection.nlp;

import java.io.IOException;

public class NLPModelLoadingException extends IOException {

    private static final String MESSAGE = "Could not load polarityModel ";

    public NLPModelLoadingException() {

    }

    public NLPModelLoadingException(String model) {
        super(MESSAGE + model);
    }


    public NLPModelLoadingException(String model, Throwable cause) {
        super(MESSAGE + model, cause);
    }

    public NLPModelLoadingException(Throwable cause) {
        super(cause);
    }
}
