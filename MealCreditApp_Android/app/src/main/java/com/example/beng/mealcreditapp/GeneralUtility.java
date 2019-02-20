package com.example.beng.mealcreditapp;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;

import java.util.HashMap;

public class GeneralUtility {

    public static final int version = Build.VERSION.SDK_INT;
    static private HashMap h = new HashMap();

    public static String getAssociatedStringForSort(String s) {
        return h.get(s).toString();
    }

    public static void setHashIfDoesntExist() {
        if(h.isEmpty()) {
            h.put("Price", "asking_price");
            h.put("Start Time", "start_time");
            h.put("End Time", "end_time");
            h.put("Location", "location");
            h.put("Username", "username");
        }
    }

    public static void printArray(int[] arr) {
        System.out.println();
        System.out.print("{ ");
        for(int i = 0; i < arr.length; i++) {
            System.out.print(arr[i] + " ");
        }
        System.out.print("}");
        System.out.println();
    }

    public static void printArray(String[] arr) {
        System.out.println("{ ");
        for(int i = 0; i < arr.length; i++) {
            System.out.print(arr[i] + " ");
        }
        System.out.print("}");
        System.out.println();
    }

    public static Drawable getDrawable(Context context, int id) {
        if (version >= 21) {
            return ContextCompat.getDrawable(context, id);
        } else {
            return context.getResources().getDrawable(id);
        }
    }

    public static boolean checkPrice(String price) {
        try {
            Double.parseDouble(price);
        } catch (Exception e) { return false; }

        return true;
    }
}
