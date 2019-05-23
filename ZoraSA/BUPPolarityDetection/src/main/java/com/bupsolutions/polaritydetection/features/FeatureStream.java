package com.bupsolutions.polaritydetection.features;

import com.bupsolutions.polaritydetection.model.LabeledTextSet;
import com.bupsolutions.polaritydetection.nlp.NLPProcessor;
import opennlp.model.Event;
import opennlp.model.ListEventStream;

import java.util.stream.Collectors;

public class FeatureStream extends ListEventStream {

    public FeatureStream(LabeledTextSet<?> data) {
        super(data.stream().map(c -> {
            String value = Integer.toString(c.getLabel().asInt());
            String[] features = NLPProcessor.getInstance().process(c.getText());
            return new Event(value, features);
        })
        .collect(Collectors.toList()));
    }
}
