package com.treycorp.ezpassva;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;

public class LocalPlugin {
    Context context;
    SharedPreferences prefs;
    DisplayMetrics displayMetrics;

    public void init(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences("LocalPlugin", MODE_PRIVATE);
        this.displayMetrics = context.getResources().getDisplayMetrics();
    }

    public void saveLogin(String username, String password) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.putBoolean("remember", true);
        editor.commit();
    }

    public boolean loginAvailable() {
        return prefs.getBoolean("remember", false);
    }

    public void revokeLogin() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("username");
        editor.remove("password");
        editor.remove("remember");
        editor.commit();
    }

    public String getUsername() {
        return prefs.getString("username", "");
    }

    public String getPassword() {
        return prefs.getString("password", "");
    }

    public int getScreenWidth() {
        return (int) (displayMetrics.widthPixels / displayMetrics.density);
    }

    public int getScreenHeight() {
        return (int) (displayMetrics.heightPixels / displayMetrics.density);
    }

    public Boolean getBoolean(String key) {
        return prefs.getBoolean(key, false);
    }

    public void setBoolean(String key, Boolean value) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

}
