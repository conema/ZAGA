package com.bupsolutions.polaritydetection.utils;


import org.joda.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class Times {

    public static List<Duration> preprocessing = new ArrayList<>();
    public static List<Duration> wordvectors = new ArrayList<>();
    public static List<Duration> classification = new ArrayList<>();
    public static List<Duration> sentiwordnet = new ArrayList<>();
    public static List<Integer> lengths = new ArrayList<>();
}
