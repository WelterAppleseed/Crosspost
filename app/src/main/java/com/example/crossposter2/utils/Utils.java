package com.example.crossposter2.utils;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.widget.Button;
import android.widget.Switch;

import com.example.crossposter2.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static String extractPattern(String string, String pattern) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(string);
        if (!m.find())
            return null;
        return m.toMatchResult().group(1);
    }

    public static String convertStreamToString(InputStream is) throws IOException {
        InputStreamReader r = new InputStreamReader(is);
        StringWriter sw = new StringWriter();
        char[] buffer = new char[1024];
        try {
            for (int n; (n = r.read(buffer)) != -1; )
                sw.write(buffer, 0, n);
        } finally {
            try {
                is.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return sw.toString();
    }

    public static void closeStream(Object oin) {
        if (oin != null)
            try {
                if (oin instanceof InputStream)
                    ((InputStream) oin).close();
                if (oin instanceof OutputStream)
                    ((OutputStream) oin).close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private static String pattern_string_profile_id = "^(id)?(\\d{1,10})$";
    private static Pattern pattern_profile_id = Pattern.compile(pattern_string_profile_id);

    public static String parseProfileId(String text) {
        Matcher m = pattern_profile_id.matcher(text);
        if (!m.find())
            return null;
        return m.group(2);
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static void setDisable(Context context, Switch swtch, Button button) {
        swtch.setClickable(false);
        swtch.setEnabled(false);
        swtch.setAlpha((float) 0.3);
        button.setEnabled(false);
        button.setClickable(false);
        button.setBackgroundColor(context.getResources().getColor(R.color.disabledColorBack));
    }

    public static void setEnable(Context context, Switch swtch, Button button, boolean state) {
        swtch.setChecked(state);
        swtch.setClickable(true);
        swtch.setEnabled(true);
        swtch.setAlpha((float) 1);
        button.setEnabled(false);
        button.setClickable(false);
        button.setBackgroundColor(context.getResources().getColor(R.color.disabledColorBack));
    }

    public static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static void onLogout(Context context, Switch swtch, Button logoutButton, Button
            connectButton) {
        swtch.setClickable(false);
        swtch.setFocusable(false);
        swtch.setEnabled(false);
        swtch.setAlpha((float) 0.3);
        logoutButton.setEnabled(false);
        logoutButton.setClickable(false);
        logoutButton.setBackgroundColor(context.getResources().getColor(R.color.disabledColorBack));
        connectButton.setEnabled(true);
        connectButton.setClickable(true);
        connectButton.setBackgroundColor(context.getResources().getColor(R.color.colorInvisible));
    }

    public static void onLogin(Context context, Switch swtch, Button connectButton, Button
            logoutButton) {
        swtch.setEnabled(true);
        swtch.setClickable(true);
        swtch.setFocusable(true);
        swtch.setAlpha(1);
        connectButton.setEnabled(false);
        connectButton.setClickable(false);
        connectButton.setBackgroundColor(context.getResources().getColor(R.color.disabledColorBack));
        logoutButton.setEnabled(true);
        logoutButton.setClickable(true);
        logoutButton.setBackgroundColor(context.getResources().getColor(R.color.colorInvisible));
    }
}