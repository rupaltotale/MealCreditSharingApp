package com.example.beng.mealcreditapp;

import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class JsonMethods {

    private static String getJson(String strEncoded) throws UnsupportedEncodingException {
        byte[] decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE);
        return new String(decodedBytes, "UTF-8");
    }

    public static String decoded(String JWTEncoded) throws Exception {
        try {
            String[] split = JWTEncoded.split("\\.");
            return getJson(split[1]);
        } catch (UnsupportedEncodingException e) {
            //Error
            System.out.println("Invalid JWT");
            return null;
        }
    }

    public static JSONObject makeJsonObjectFromStrings(String[] keys, String[] values) {
        JSONObject json = new JSONObject();
        try {
            for (int i = 0; i < keys.length; i++) {
                json.put(keys[i], values[i]);
            }
        } catch (JSONException e) { return null; }

        return json;
    }

    public static JSONObject convertToJSONFromString(String jsonStr) {
        try {
            return new JSONObject(jsonStr);
        }
        catch (JSONException e) { return null; }
    }

    protected static String getString(JSONObject json, String str) {
        try {
            return json.getString(str);
        }
        catch (JSONException e) { return ""; }
    }

    protected static String get(JSONObject json, String str) {
        try {
            return json.get(str).toString();
        }
        catch (JSONException e) { return null; }
    }

    protected static String convertJSONToString(JSONObject json) {
        return json.toString();
    }

    protected static void put(JSONObject json, String s, String s1) {
        try {
            json.put(s, s1);
        } catch (JSONException e) { System.out.println("JSON Exception"); }
    }
}