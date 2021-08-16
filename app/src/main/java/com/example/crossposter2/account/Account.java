package com.example.crossposter2.account;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class Account {
    public String access_token_vk, access_token_fb;
    public long user_id_vk, user_id_fb;

    public void saveVk(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = prefs.edit();
        editor.putString("access_token_vk", access_token_vk);
        editor.putLong("user_id_vk", user_id_vk);
        editor.apply();
    }
    public void restoreVk(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        access_token_vk = prefs.getString("access_token_vk", null);
        user_id_vk = prefs.getLong("user_id_vk", 0);
    }
    public void saveFb(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = prefs.edit();
        editor.putString("access_token_fb", access_token_fb);
        editor.putLong("user_id_fb", user_id_fb);
        editor.apply();
    }
    public void restoreFb(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        access_token_fb = prefs.getString("access_token_fb", null);
        user_id_fb = prefs.getLong("user_id_fb", 0);
    }
}