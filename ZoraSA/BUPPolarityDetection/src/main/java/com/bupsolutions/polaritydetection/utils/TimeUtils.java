package com.bupsolutions.polaritydetection.utils;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

public class TimeUtils {

    private static PeriodFormatter formatter = new PeriodFormatterBuilder()
            .appendHours()
            .appendSuffix(" h ")
            .appendMinutes()
            .appendSuffix(" min ")
            .appendSeconds()
            .appendSuffix(" s ")
            .appendMillis()
            .appendSuffix(" ms")
            .toFormatter();

    public static String formatPeriod(long start, long stop) {
        Period period = new Period(start, stop);
        return formatPeriod(period);
    }

    private static String formatPeriod(Period period) {
        return formatter.print(period);
    }
}
