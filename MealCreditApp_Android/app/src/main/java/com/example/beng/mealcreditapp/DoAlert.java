package com.example.beng.mealcreditapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class DoAlert {

    public static void doBasicAlert(String message, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        return;
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
