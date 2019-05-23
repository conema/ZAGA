package com.bupsolutions.polaritydetection.reader;

import com.bupsolutions.polaritydetection.model.Label;
import com.bupsolutions.polaritydetection.model.LabeledTextSet;

import java.io.IOException;

public interface DataSetReader<T extends Label> {
    LabeledTextSet<T> read() throws IOException;
}
