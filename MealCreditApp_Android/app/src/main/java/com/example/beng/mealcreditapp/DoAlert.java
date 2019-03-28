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

    public static void doBasicConfirm(String message, Context context, final Alertable cf1, final Alertable cf2) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(cf1 == null) {
                            return;
                        }
                        cf1.doAction();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(cf2 == null) {
                            return;
                        }
                        cf2.doAction();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
