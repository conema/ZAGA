package com.bupsolutions.polaritydetection.model;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LabeledTextSet<T extends Label> {

    private List<LabeledText<T>> data;

    public LabeledTextSet(int initialCapacity) {
       data = new ArrayList<>(initialCapacity);
    }

    public LabeledTextSet() {
        data = new ArrayList<>();
    }

    public LabeledTextSet(Collection<? extends LabeledText<T>> c) {
        data = new ArrayList<>(c);
    }

    public Stream<LabeledText<T>> stream() {
        return data.stream();
    }

    public LabeledText<T> get(int index) {
        return data.get(index);
    }

    public boolean add(LabeledText<T> labeledText) {
        return data.add(labeledText);
    }

    public boolean addAll(LabeledTextSet<T> c) {
        return data.addAll(c.data);
    }

    public int size() {
        return data.size();
    }

    public List<LabeledText<T>> getList() {
        return data;
    }

    public void shuffle(Random prg) {
        Collections.shuffle(data, prg);
    }

    public List<String> texts() {
        return data.stream().map(LabeledText::getText).collect(Collectors.toList());
    }

    public LabeledTextSet<T> sample(int fromIndex, int toIndex) {
        List<LabeledText<T>> sample = data.subList(fromIndex, toIndex);
        return new LabeledTextSet<>(sample);
    }

    public List<LabeledTextSet<T>> split(List<Double> values) {
        if (values.stream().reduce((a, b) -> a + b).orElse(0.0) > 1.0) {
            throw new IllegalArgumentException("The sum of the labels must not be higher than 1.0");
        }

        List<LabeledTextSet<T>> result = new ArrayList<>(values.size());
        int i = 0;

        for (Double value : values) {
            int j = i + (int) (value * size());
            result.add(sample(i, j));
            i = j;
        }

        return result;
    }

    public void forEach(Consumer<? super LabeledText> action) {
        data.forEach(action);
    }

    public static <T extends Label> Collector<LabeledText<T>, ?, LabeledTextSet<T>> collector() {
        return Collector.of(
                LabeledTextSet::new,
                LabeledTextSet::add,
                (set1, set2) -> {
                    set1.addAll(set2);
                    return set1;
                },
                Collector.Characteristics.IDENTITY_FINISH
        );
    }
}
