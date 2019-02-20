package com.example.beng.mealcreditapp;

import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ServerCommunicationGet {

    private OkHttpClient client;
    private Request request;

    public ServerCommunicationGet(String url) {
        client = new OkHttpClient();
        String fullUrl = "http://10.0.2.2:8000/" + url;
        request = new Request.Builder()
                .url(fullUrl)
                .build();
    }

    public Response sendGetRequest() {
        /*client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                else {
                    String res = response.body().string();
                    System.out.println("RESULT OF GET: " + res);
                }
            }
        });*/
        try {
            //System.out.println("HERE...");
            Response response = client.newCall(request).execute();
            //System.out.println(response);
            return response;
        }
        catch(IOException e) {
            return null;
        }
    }
}