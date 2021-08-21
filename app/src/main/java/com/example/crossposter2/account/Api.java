package com.example.crossposter2.account;

import android.util.Log;

import com.example.crossposter2.exceptions.KException;
import com.example.crossposter2.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.GZIPInputStream;


public class Api {

    public static final String BASE_URL="https://api.vk.com/method/";
    public static final String API_VERSION="5.5";
    private static final String TAG = "TAG";
    static boolean enable_compression=true;
    private final static int MAX_TRIES=3;

    private String access_token;
    private String api_id;

    public Api(String access_token, String api_id){
        this.access_token=access_token;
        this.api_id=api_id;
    }


}
