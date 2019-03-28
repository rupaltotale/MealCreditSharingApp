package com.example.beng.mealcreditapp;

public class User {

    private static String jwt = "";
    private static String userId = "";
    private static String username = "";
    private static String firstname = "";
    private static String lastname = "";
    private static String email = "";

    public static boolean isNull(String str) {
        return str == null || str.toLowerCase().equals("null");
    }

    public static void setUser(String jwt, String userId, String username, String firstname, String lastname) {
        if(isNull(jwt)) {
            User.jwt = "";
        }
        else {
            User.jwt = jwt;
        }
        if(isNull(username)) {
            User.username = "";
        }
        else {
            User.username = username;
        }
        if(isNull(userId)) {
            User.userId = "";
        }
        else {
            User.userId = userId;
        }
        if(isNull(firstname)) {
            User.firstname = "";
        }
        else {
            User.firstname = firstname;
        }
        if(isNull(lastname)) {
            User.lastname = "";
        }
        else {
            User.lastname = lastname;
        }
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

    public static String getEmail() {
        return User.email;
    }

    public static void setJwt(String jwt) {
        if(jwt.toLowerCase().equals("null")) {
            User.jwt = "";
        }
        else {
            User.jwt = jwt;
        }
    }

    public static void setEmail(String email) {
        if(isNull(email)) {
            User.email = "";
        }
        else {
            User.email = email;
        }
    }

    public static void setUserId(String userId) {
        if(isNull(userId)) {
            User.userId = "";
        }
        else {
            User.userId = userId;
        }
    }

    public static void setFirstname(String firstname) {
        if(isNull(firstname)) {
            User.firstname = "";
        }
        else {
            User.firstname = firstname;
        }
    }

    public static void setLastname(String lastname) {
        if(isNull(lastname)) {
            User.lastname = "";
        }
        else {
            User.lastname = lastname;
        }
    }

    public static void setUsername(String username) {
        if(isNull(username)) {
            User.username = "";
        }
        else {
            User.username = username;
        }
    }

    public static String getAllInfoAsJSONString() {
        return JsonMethods.convertJSONToString(JsonMethods.makeJsonObjectFromStrings(new String[]{"username", "firstname", "lastname", "jwt", "userId", "email"},
                new String[]{getUsername(), User.firstname, User.lastname, User.jwt, User.userId, User.email}));
    }

    public static void updateSingleUserField(String fieldName, String value) {
        switch (fieldName) {
            case "jwt":
                User.jwt = value;
                break;
            case "userId":
                User.userId = value;
                break;
            case "username":
                User.username = value;
                break;
            case "firstname":
                User.firstname = value;
                break;
            case "lastname":
                User.lastname = value;
                break;
            case "email":
                User.email = value;
                break;
            default:
                break;
        }
    }
}
