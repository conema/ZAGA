package com.bupsolutions.polaritydetection.reader;

import com.bupsolutions.polaritydetection.model.LabeledText;
import com.bupsolutions.polaritydetection.model.LabeledTextSet;
import com.bupsolutions.polaritydetection.model.Polarity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Sentipolc2016Reader extends CSVReader implements DataSetReader<Polarity> {

    public Sentipolc2016Reader(String path) throws FileNotFoundException {
        super(path);
    }

    @Override
    public LabeledTextSet<Polarity> read() throws IOException {
        List<Map<String, String>> data = read("opos", "oneg", "text");
        LabeledTextSet<Polarity> dataset = new LabeledTextSet<>(data.size());

        for (Map<String, String> record : data) {
            int opos = Integer.parseInt(record.get("opos"));
            int oneg = Integer.parseInt(record.get("oneg"));
            String text = record.get("text");

            Polarity polarity = null;

            if (opos == 1) {
                polarity = Polarity.POSITIVE;
            } else if (oneg == 1) {
                polarity = Polarity.NEGATIVE;
            }

            if (polarity != null) {
                dataset.add(new LabeledText<>(text, polarity));
            }
        }
        return dataset;
    }
}
