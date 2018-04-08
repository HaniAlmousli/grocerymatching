package com.hanialmousli.shoppingapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by hani on 18/11/17.
 */

public class AUXCLASS {

    public static String IMAGE_TYPE="ERROR";
    public static void MESSAGEBOXSHOW(String text,String title, Context mainContext){
        AlertDialog alertDialog = new AlertDialog.Builder(mainContext).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(text);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
}
