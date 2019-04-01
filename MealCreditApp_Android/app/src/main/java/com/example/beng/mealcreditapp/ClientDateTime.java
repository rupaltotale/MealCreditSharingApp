package com.example.beng.mealcreditapp;

public class ClientDateTime {

    public int year;
    public int day;
    public int month;
    public int hour;
    public int minute;

    public ClientDateTime(String date, String time) {
        int lastSlashIndex = date.lastIndexOf("/");
        int firstSlashIndex = date.indexOf("/");
        int colonIndex = time.indexOf(":");

        this.year = Integer.parseInt(date.substring(lastSlashIndex + 1));
        this.month = Integer.parseInt(date.substring(0, firstSlashIndex)) - 1;
        this.day = Integer.parseInt(date.substring(firstSlashIndex + 1, lastSlashIndex));

        this.hour = Integer.parseInt(time.substring(0, colonIndex));
        if(time.contains("PM")) {
            this.hour += 12;
        }
        this.minute = Integer.parseInt(time.substring(colonIndex + 1, time.indexOf(" ")));
    }

    public ClientDateTime(String dateTime) {
        int firstSpace = dateTime.indexOf(" ");
        String date = dateTime.substring(0, firstSpace);
        String time = dateTime.substring(firstSpace + 1);

        int lastSlashIndex = date.lastIndexOf("/");
        int firstSlashIndex = date.indexOf("/");
        int colonIndex = time.indexOf(":");

        this.year = Integer.parseInt(date.substring(lastSlashIndex + 1));
        this.month = Integer.parseInt(date.substring(0, firstSlashIndex));
        this.day = Integer.parseInt(date.substring(firstSlashIndex + 1, lastSlashIndex));

        this.hour = Integer.parseInt(time.substring(0, colonIndex));
        if(time.contains("PM")) {
            this.hour += 12;
        }
        if(time.contains("AM") && hour == 12) {
            hour = 0;
        }
        this.minute = Integer.parseInt(time.substring(colonIndex + 1, time.indexOf(" ")));
    }
}
