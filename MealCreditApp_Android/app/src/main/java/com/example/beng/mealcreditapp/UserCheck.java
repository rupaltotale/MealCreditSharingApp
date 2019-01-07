package com.example.beng.mealcreditapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserCheck {

    private static String jwt = "";
    private static String filename = "spUser";
    private static SharedPreferences sp;
    private static boolean hasInitialized = false;

    public static void initializeUserCheck(Context context) {
        sp = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
        jwt = sp.getString("storedjwt", "");
        /*if(setJwt) {
            jwt = sp.getString("storedjwt", "");
        }*/
        hasInitialized = true;
    }

    public static boolean hasInitialized() {
        return hasInitialized;
    }

    private static boolean userExists() {
        return !jwt.equals("");
    }

    private static String getJwt() {
        if(userExists() && hasInitialized) {
            return jwt;
        }
        return "";
    }

    public static boolean setUserInfoIfExists() {
        String gotJwt = getJwt();
        if(!gotJwt.equals("")) {
            String jwtBody;
            try {
                jwtBody = JsonMethods.decoded(jwt);
                JSONObject json = new JSONObject(jwtBody);
                String userIdFound;
                if(!json.isNull("user_id")) {
                    userIdFound = json.getString("userId");
                    String userId = sp.getString("userId", "");
                    if(!userId.equals(userIdFound)) {
                        return false;
                    }
                    else {
                        String firstname = sp.getString("firstname", "");
                        String lastname = sp.getString("lastname", "");
                        String username = sp.getString("username", "");
                        User.setUser(jwt, userIdFound, username, firstname, lastname);
                    }
                }
                else {
                    userIdFound = null;
                    return false;
                }

            } catch (Exception e) { return false; }
            return true;
        }
        return false;
    }

    public static void setUserSharedPreferences(JSONObject json) {
        if(hasInitialized()) {
            Iterator<String> keys = json.keys();
            SharedPreferences.Editor sPEditor = sp.edit();

            while(keys.hasNext()) {
                try {
                    String key = keys.next();
                    if(key.equals("token")) {
                        key = "jwt";
                    }
                    String newValue = json.getString(key);
                    sPEditor.putString(key, newValue);
                }
                catch (JSONException e) { continue; }
            }

            sPEditor.apply();
        }
    }


    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public static boolean validate(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(emailStr);
        return matcher.find();
    }
}