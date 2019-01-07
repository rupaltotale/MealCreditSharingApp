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
            String jwtBody = getJson(split[1]);
            return jwtBody;
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
}