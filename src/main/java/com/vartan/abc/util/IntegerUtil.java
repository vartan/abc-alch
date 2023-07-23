package com.vartan.abc.util;

public class IntegerUtil {
    private static final String[] UNITS = {"", "k", "m", "b", "t"};

    public static String toShorthand(int number) {
        return toShorthand(number, 1);
    }

    public static String toShorthand(int number, int decimals) {
        int unitIndex = 0;
        double output = number;
        while (output >= 1000 && unitIndex < UNITS.length - 1) {
            output /= 1000;
            unitIndex++;
        }
        double roundingMultiplier = (int) Math.pow(10, decimals);
        double scalar = (Math.round(output * roundingMultiplier) / roundingMultiplier);
        String scalarString;
        if (scalar == (int) scalar) {
            scalarString = String.format("%d", (int) scalar);
        } else {
            scalarString = String.format("%s", scalar);
        }
        return scalarString + UNITS[unitIndex];
    }
}
