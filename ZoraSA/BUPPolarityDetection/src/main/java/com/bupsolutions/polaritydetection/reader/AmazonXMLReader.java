package com.bupsolutions.polaritydetection.reader;

import com.bupsolutions.polaritydetection.model.LabeledText;
import com.bupsolutions.polaritydetection.model.LabeledTextSet;
import com.bupsolutions.polaritydetection.model.Polarity;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;


public class AmazonXMLReader implements DataSetReader<Polarity> {

    private final String textTag;
    private final String polarityTag;
    private final Document doc;

    public AmazonXMLReader(String path, String textTag, String polarityTag) throws IOException, SAXException, ParserConfigurationException {
        this.textTag = textTag;
        this.polarityTag = polarityTag;

        File fXmlFile = new File(path);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        doc = dBuilder.parse(fXmlFile);
        doc.getDocumentElement().normalize();
    }

    @Override
    public LabeledTextSet<Polarity> read() throws IOException {
        NodeList texts = doc.getElementsByTagName(textTag);
        NodeList ratings = doc.getElementsByTagName(polarityTag);

        int length = texts.getLength();
        LabeledTextSet<Polarity> dataset = new LabeledTextSet<>(length);

        for (int i = 0; i < length; i++) {
            String text = texts.item(i).getTextContent();
            String rating = ratings.item(i).getTextContent();
            //double score = Double.parseDouble(rating);

            Polarity polarity = Polarity.NEGATIVE;

            if (rating.equals("positive")) {
                polarity = Polarity.POSITIVE;
            }

            LabeledText<Polarity> instance = new LabeledText<>(text, polarity);
            dataset.add(instance);
        }

        return dataset;
    }
}
