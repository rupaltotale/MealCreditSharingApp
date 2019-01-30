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

    public static String reverseParseServerDateTime(String dateTime) {
        int tIndex = dateTime.indexOf("T") + 1;
        if(tIndex > 0) {
            String date = dateTime.substring(0, tIndex - 1);
            String formattedDate = reverseDateServer(date);

            String time = dateTime.substring(tIndex);
            String formattedTime = reverseTimeServer(time);

            if(!formattedDate.equals("") && !formattedTime.equals("")) {
                return formattedDate + "|" + formattedTime;
            }
        }

        return null;
    }

    public static String reverseDateServer(String date) {
        int firstHyphenIndex = date.indexOf("-");
        int secondHyphenIndex = date.lastIndexOf("-");
        if(firstHyphenIndex != secondHyphenIndex) {
            String year = date.substring(0, firstHyphenIndex);
            String month = date.substring(firstHyphenIndex + 1, secondHyphenIndex);
            String day = date.substring(secondHyphenIndex + 1);

            return month + "/" + day + "/" + year;
        }
        else { // IN CASE A DATE INPUT ISNT A DATE
            return "";
        }
    }

    public static String reverseTimeServer(String time) {
        int firstColonIndex = time.indexOf(":");
        System.out.println(time);
        int secondColonIndex = time.substring(firstColonIndex + 1).indexOf(":") + firstColonIndex + 1;
        if(firstColonIndex > -1 && secondColonIndex > -1) {
            String hour = time.substring(0, firstColonIndex);
            String minute = time.substring(firstColonIndex + 1, secondColonIndex);
            int parsedHour = Integer.parseInt(hour);
            String relation = getHourTimeRelation(hour);
            String correctHour = getAppropriateHourFrom24(relation, parsedHour);

            return correctHour + ":" + minute + " " + relation;
        }
        return "";
    }

    public static String getHourTimeRelation(String hour) {
        return Integer.parseInt(hour) >= 12 ? "PM" : "AM";
    }

    protected static String getAppropriateHourFrom24(String relation, int hourOfDay) {
        if(hourOfDay == 0 || hourOfDay == 12) {
            return "12";
        }
        else if(relation.equals("PM")) {
            return Integer.toString(hourOfDay % 12);
        }

        return Integer.toString(hourOfDay);
    }

    /*private String getCorrectDateTimeFromServer(String dateTime, int tIndexPlus1, ) {

    }*/
}