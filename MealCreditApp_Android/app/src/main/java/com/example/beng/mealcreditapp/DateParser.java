package com.example.beng.mealcreditapp;

import java.util.regex.Pattern;

public class DateParser {

    public String convertSlashDateTime(String date, String time) {
        String dateReplace = date.replaceAll("/", "-");
        String rightDate = reverseDate(dateReplace);
        int timeSpaceIndex = time.indexOf(" ");
        if(time.contains("PM")) {
            int colonIndex = time.indexOf(":");
            int hour = Integer.parseInt(time.substring(0, colonIndex)) + 12;
            int minute = Integer.parseInt(time.substring(colonIndex + 1, timeSpaceIndex));
            return rightDate + " " + hour + ":" + minute + ":" + "00";
        }
        if(time.substring(0, 2).equals("12")) {
            time = "00" + time.substring(2, time.length());
        }
        return rightDate + " " + time.substring(0, timeSpaceIndex) + ":" + "00";
    }

    public String reverseDate(String date) {
        int firstIndex = date.indexOf("-");
        int lastIndex = date.lastIndexOf("-");
        String year = date.substring(lastIndex + 1, date.length());
        return year + "-" + date.substring(0, lastIndex);
    }
}
