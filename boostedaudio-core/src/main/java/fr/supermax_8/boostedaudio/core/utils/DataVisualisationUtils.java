package fr.supermax_8.boostedaudio.core.utils;

public class DataVisualisationUtils {


    public static String intMetricToEzReadString(int metrics) {
        if (metrics >= 1000) return "> 1000 (WOW)";
        if (metrics >= 200) return "> 200";
        if (metrics >= 100) return "> 100";
        if (metrics >= 30) return "> 30";
        if (metrics >= 10) return "> 10";
        if (metrics >= 5) return "> 5";
        return "0";
    }


}