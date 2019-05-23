package com.bupsolutions.polaritydetection.features;

public class WordVectorsLoadingException extends RuntimeException {

    public WordVectorsLoadingException() {
    }

    public WordVectorsLoadingException(String message) {
        super(message);
    }

    public WordVectorsLoadingException(String message, Throwable cause) {
        super(message, cause);
    }

    public WordVectorsLoadingException(Throwable cause) {
        super(cause);
    }

    public WordVectorsLoadingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
