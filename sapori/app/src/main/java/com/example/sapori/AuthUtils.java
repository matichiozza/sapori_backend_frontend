package com.example.sapori;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthUtils {

    public static boolean estaLogueado(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("sapori_prefs", Context.MODE_PRIVATE);
        return prefs.getBoolean("logueado", false);
    }
}
