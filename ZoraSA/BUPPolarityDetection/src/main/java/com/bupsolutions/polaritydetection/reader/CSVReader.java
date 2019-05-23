package com.bupsolutions.polaritydetection.reader;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CSVReader {

    private com.opencsv.CSVReader reader;

    public CSVReader(String path) throws FileNotFoundException {
        FileInputStream stream = new FileInputStream(path);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
        reader = new com.opencsv.CSVReader(bufferedReader);
    }

    public List<Map<String, String>> read(String... fields) throws IOException {
        List<Map<String, String>> dataset = new ArrayList<>();

        String[] header = reader.readNext();
        int[] indexes = getIndexes(header, fields);

        String[] line = reader.readNext();

        while (line != null) {
            Map<String, String> record = parseInstance(line, header, indexes);
            dataset.add(record);
            line = reader.readNext();
        }

        return dataset;
    }

    private int[] getIndexes(String[] header, String[] fields) {
        if (header == null) {
            return new int[]{};
        }

        int[] indexes = new int[fields.length];

        for (int i = 0; i < fields.length; i++) {
            for (int j = 0; j < header.length; j++) {
                if (header[j].equals(fields[i])) {
                    indexes[i] = j;
                    break;
                }
            }
        }

        return indexes;
    }

    private Map<String, String> parseInstance(String[] line, String[] header, int[] indexes) {
        Map<String, String> map = new HashMap<>();
        for (int i : indexes) {
            String value = line[i];
            String key = header[i];
            map.put(key, value);
        }

        return map;
    }
}
