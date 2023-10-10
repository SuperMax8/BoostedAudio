package fr.supermax_8.boostedaudio.utils;

import java.text.NumberFormat;
import java.util.Random;

public class MathUtils {

    private static final Random r = new Random();
    private static final NumberFormat numberFormat = NumberFormat.getInstance();

    public static double randomSignChange(double n) {
        return r.nextBoolean() ? -n : n;
    }

    public static double randomBetween(double min, double max) {
        return min + (max - min) * r.nextDouble();
    }

    public static double randomBetweenWithSignChange(double min, double max) {
        return randomSignChange(randomBetween(min, max));
    }

    public static String numberFormat(double d) {
        return numberFormat.format(d);
    }

    public static double getDecimals(double d) {
        return d - Math.floor(d);
    }

    public static int getInventoryCollumn(int slot) {
        return (int) Math.round(MathUtils.getDecimals(slot / 9f) * 9);
    }

    public static int getInventoryRow(int slot) {
        return (int) Math.ceil((slot + 1) / 9f);
    }

    public static double getUpperNearestMultiple(double number, double multiple) {
        return Math.ceil((number / multiple)) * multiple;
    }

    public static double getLowerNearestMultiple(double number, double multiple) {
        return Math.floor((number / multiple)) * multiple;
    }

    public static double getNearestMultiple(double number, double multiple) {
        return Math.round((number / multiple)) * multiple;
    }

    public static boolean isEven(double d) {
        return d % 2 == 0;
    }


}