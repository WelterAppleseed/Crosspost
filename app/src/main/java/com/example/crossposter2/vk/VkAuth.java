package com.example.crossposter2.vk;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;


import com.example.crossposter2.R;
import com.example.crossposter2.utils.Utils;

import java.net.URLEncoder;

import static android.content.ContentValues.TAG;


public class VkAuth extends Activity {

    WebView webview;
    public static String redirect_url="https://oauth.vk.com/blank.html";
    private static String API_VERSION="5.5";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        webview = (WebView) findViewById(R.id.vk_view);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.clearCache(true);
        webview.setWebViewClient(new VkontakteWebViewClient());

        CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();

        String url=VkAuth.getUrl(VkConstant.AppId, "wall");
        webview.loadUrl(url);
    }
    public static String getUrl(String api_id, String settings){
        String url="https://oauth.vk.com/authorize?client_id="+api_id+"&display=mobile&scope="+settings+"&redirect_uri="+ URLEncoder.encode(redirect_url)+"&response_type=token"
                +"&v="+ URLEncoder.encode(API_VERSION);
        return url;
    }

    public static String[] parseRedirectUrl(String url) throws Exception {
        String access_token= Utils.extractPattern(url, "access_token=(.*?)&");
        Log.i(TAG, "access_token=" + access_token);
        String user_id=Utils.extractPattern(url, "user_id=(\\d*)");
        Log.i(TAG, "user_id=" + user_id);
        if(user_id==null || user_id.length()==0 || access_token==null || access_token.length()==0)
            throw new Exception("Failed to parse redirect url "+url);
        return new String[]{access_token, user_id};
    }
    class VkontakteWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            parseUrl(url);
        }
    }

    private void parseUrl(String url) {
        try {
            if (url == null)
                return;
            Log.i(TAG, "url=" + url);
            if (url.startsWith(VkAuth.redirect_url)) {
                if (!url.contains("error=")) {
                    String[] auth = VkAuth.parseRedirectUrl(url);
                    Intent intent = new Intent();
                    intent.putExtra("token", auth[0]);
                    intent.putExtra("user_id", Long.parseLong(auth[1]));
                    setResult(Activity.RESULT_OK, intent);
                }
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
