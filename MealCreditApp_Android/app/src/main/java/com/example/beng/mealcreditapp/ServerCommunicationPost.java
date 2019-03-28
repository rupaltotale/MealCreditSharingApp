package com.example.beng.mealcreditapp;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ServerCommunicationPost {

    private OkHttpClient client;
    private Request request;
    private RequestBody body;
    private final MediaType JSON_MEDIA = MediaType.parse("application/json; charset=utf-8");

    public ServerCommunicationPost(String url, String jsonBody) {
        client = new OkHttpClient();
        body = RequestBody.create(JSON_MEDIA, jsonBody);
        //String fullUrl = "http://10.0.2.2:8000/" + url;
        String fullUrl = GeneralUtility.SERVER_IP + url;
        request = new Request.Builder()
                .url(fullUrl)
                .post(body)
                .build();
    }

    public Response sendPostRequest() {
        try {
            //System.out.println("HERE...");
            Response response = client.newCall(request).execute();
            //System.out.println(response.body().string());
            return response;
        }
        catch(IOException e) {
            //System.out.println("Exception error: " + e.getLocalizedMessage());
            return null;
        }
    }
}