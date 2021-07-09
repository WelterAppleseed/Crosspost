package com.example.crossposter2.tg;

import android.content.Context;
import android.content.pm.PackageManager;

import java.util.jar.Attributes;

public class IsAppAvailable {

    public IsAppAvailable() {
    }

    public static boolean isAppAvailable(Context context, String appName) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(appName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }
}
