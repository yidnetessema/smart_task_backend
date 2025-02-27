package com.inkomoko.smarttask.helpers;

import java.util.Random;

public class StringHelper {
    public static String getCleanPhone(String junkPhone) {
        junkPhone = junkPhone.replace(")", "").replace("(", "").replace("+", "");
        char[] explodedPhone = junkPhone.toCharArray();
        String swap = "";
        String cleanPhone = "0";
        for (int x = explodedPhone.length - 1; x >= 0; x--) {
            swap += explodedPhone[x];

            if (swap.length() == 9) {
                break;
            }
        }

        explodedPhone = swap.toCharArray();
        for (int x = explodedPhone.length - 1; x >= 0; x--) {
            cleanPhone += explodedPhone[x];
        }
        return cleanPhone;
    }

    public static String getRandomNumberString() {
        Random rnd = new Random();
        int number = rnd.nextInt(999999);
        if (number <= 10) {
            getRandomNumberString();
        }
        return String.format("%06d", number);
    }
}
