package com.bupsolutions.polaritydetection.features;

import com.bupsolutions.polaritydetection.Settings;

import java.util.HashMap;
import java.util.Map;

public class WordVectorsManager {
    private static Map<String, WordVectors> map = new HashMap<>();

    private WordVectorsManager() {

    }

    public static WordVectors getVectors(String lang) {
        WordVectors vectors =  map.get(lang);
        if (vectors == null) {
            throw new WordVectorsNotLoadedException("Word vectors for language " + lang + " have not been loaded");
        }
        return vectors;
    }

    public static WordVectors getVectors() {
        return map.get(Settings.lang);
    }

    public static void load(String lang, String path) {
        WordVectors vectors = new WordVectors(path);
        map.put(lang, vectors);
    }


}
