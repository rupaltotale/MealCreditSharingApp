package com.example.beng.mealcreditapp;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

public class DateParser {

    public static String convertSlashDateTime(String date, String time) {
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

    public static String reverseDate(String date) {
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

    public static String getHourTimeRelation(int hour) {
        return hour >= 12 ? "PM" : "AM";
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

    public static String getCurrentDateTimeServer() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH) + 1;
        int minute = cal.get(Calendar.MINUTE);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        String relation = getHourTimeRelation(hour);
        String newHour = getAppropriateHourFrom24(relation, hour);

        return convertSlashDateTime(month + "/" + day + "/" + year, newHour + ":" + minute + " " + relation);
    }

    public static String getCurrentDateTime() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH) + 1;
        int minute = cal.get(Calendar.MINUTE);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        String relation = getHourTimeRelation(hour);
        String newHour = getAppropriateHourFrom24(relation, hour);

        return month + "/" + day + "/" + year + " " + newHour + ":" + minute + " " + relation;
    }

    public static int[] getValuesFromServerDateTimeFormat(String dateTime) {
        int periodIndex = dateTime.indexOf(".");
        if(periodIndex == -1) {
            periodIndex = dateTime.length();
        }
        int firstHy = dateTime.indexOf("-");
        int secondHy = dateTime.substring(firstHy + 1).indexOf("-") + firstHy + 1;
        int spaceIndex = dateTime.indexOf("T");
        if(spaceIndex == -1) {
            spaceIndex = dateTime.indexOf(" ");
        }
        int firstCo = dateTime.indexOf(":");
        int secondCo = dateTime.substring(firstCo + 1).indexOf(":") + firstCo + 1;

        String year = dateTime.substring(0, firstHy);
        String month = dateTime.substring(firstHy + 1, secondHy);
        String day = dateTime.substring(secondHy + 1, spaceIndex);
        String hour = dateTime.substring(spaceIndex + 1, firstCo);
        String minute = dateTime.substring(firstCo + 1, secondCo);
        String second = dateTime.substring(secondCo + 1, periodIndex);
        String[] values = new String[]{year, month, day, hour, minute, second};
        int[] intValues = new int[6];
        for(int i = 0; i < values.length; i++) {
            intValues[i] = Integer.parseInt(values[i]);
        }
        return intValues;
    }

    public static long getMillisDifference(String dt1, String dt2) {
        /*System.out.println(dt1);
        System.out.println(dt2);*/
        int[] dt1Vals = getValuesFromServerDateTimeFormat(dt1);
        int[] dt2Vals = getValuesFromServerDateTimeFormat(dt2);
        GeneralUtility.printArray(dt2Vals);

        Calendar c1 = (Calendar) Calendar.getInstance().clone();
        c1.clear();
        Calendar c2 = (Calendar) Calendar.getInstance().clone();
        c2.clear();
        c1.set(dt1Vals[0], dt1Vals[1] - 1, dt1Vals[2], dt1Vals[3], dt1Vals[4], 0);
        /*System.out.println(c1);
        System.out.println(dt2Vals[1]);
        System.out.println(dt2Vals[2]);*/
        c2.set(dt2Vals[0], dt2Vals[1] - 1, dt2Vals[2], dt2Vals[3], dt2Vals[4], 0);
        //System.out.println(c1);
        //System.out.println(c2);

        return c2.getTime().getTime() - c1.getTime().getTime();
    }

    public static String getHumanTimeDifference(String dt1, String dt2) {
        long timeDifference = DateParser.getMillisDifference(dt1, dt2);
        System.out.println(timeDifference);
        int minuteDifference = (int)(timeDifference / 60000);
        System.out.println(minuteDifference);
        if(minuteDifference < 3) {
            return "0m";
        }

        int numOfDays = minuteDifference / (60 * 24);
        int numOfHours = minuteDifference / 60;
        int minutes = minuteDifference % 60;
        String daysToDisplay = numOfDays > 0 ? Integer.toString(numOfDays) + "d" : "";
        String hoursToDisplay = numOfHours > 0 ? Integer.toString(numOfHours) + "h" : "";
        String minutesToDisplay = minutes > 0 ? Integer.toString(minutes) + "m" : "";

        return daysToDisplay + hoursToDisplay + minutesToDisplay;
    }

    /*private String getCorrectDateTimeFromServer(String dateTime, int tIndexPlus1, ) {

    }*/
}