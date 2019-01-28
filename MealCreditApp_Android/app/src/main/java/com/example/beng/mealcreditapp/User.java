package com.example.beng.mealcreditapp;

public class User {

    private static String jwt = "";
    private static String userId = "";
    private static String username = "";
    private static String firstname = "";
    private static String lastname = "";

    public static void setUser(String jwt, String userId, String username, String firstname, String lastname) {
        User.jwt = jwt;
        User.username = username;
        User.userId = userId;
        User.firstname = firstname;
        User.lastname = lastname;
    }

    public static String getJwt() {
        return User.jwt;
    }

    public static String getUserId() {
        return User.userId;
    }

    public static String getUsername() {
        return User.username;
    }

    public static String getFullName() {
        return User.firstname + " " + User.lastname;
    }

    public static void setJwt(String jwt) {
        User.jwt = jwt;
    }

    public static void setUserId(String userId) {
        User.userId = userId;
    }

    public static void setFirstname(String firstname) {
        User.firstname = firstname;
    }

    public static void setLastname(String lastname) {
        User.lastname = lastname;
    }

    public static void setUsername(String username) {
        User.username = username;
    }

    public static String getAllInfoAsJSONString() {
        return JsonMethods.convertJSONToString(JsonMethods.makeJsonObjectFromStrings(new String[]{"username", "firstname", "lastname", "jwt", "userId"},
                new String[]{getUsername(), User.firstname, User.lastname, User.jwt, User.userId}));
    }
}
