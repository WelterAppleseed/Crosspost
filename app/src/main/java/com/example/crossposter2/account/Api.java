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
import java.util.Collection;
import java.util.zip.GZIPInputStream;

import static androidx.constraintlayout.motion.widget.MotionScene.TAG;

public class Api {

    public static final String BASE_URL="https://api.vk.com/method/";
    public static final String API_VERSION="5.5";
    static boolean enable_compression=true;
    private final static int MAX_TRIES=3;


    public Api(String access_token, String api_id){
        this.access_token=access_token;
        this.api_id=api_id;
    }

    public void setAccessToken(String access_token){
        this.access_token=access_token;
    }

    String access_token;
    String api_id;

    <T> String arrayToString(Collection<T> items) {
        if(items==null)
            return null;
        String str_cids = "";
        for (Object item:items){
            if(str_cids.length()!=0)
                str_cids+=',';
            str_cids+=item;
        }
        return str_cids;
    }
    private void addCaptchaParams(String captcha_key, String captcha_sid, Params params) {
        params.put("captcha_sid",captcha_sid);
        params.put("captcha_key",captcha_key);
    }


    public long createWallPost(long owner_id, String text, Collection<String> attachments, String export, boolean only_friends, boolean from_group, boolean signed, String lat, String lon, Long publish_date, Long post_id, String captcha_key, String captcha_sid, double version) throws IOException, JSONException, KException {
        Params params = new Params("wall.post");
        params.put("owner_id", owner_id);
        params.put("message", text);
        params.put("attachments", arrayToString(attachments));
        params.put("lat", lat);
        params.put("long", lon);
        params.put("v", version);
        if (export != null && export.length() != 0)
            params.put("services", export);
        if (from_group)
            params.put("from_group", "1");
        if (only_friends)
            params.put("friends_only", "1");
        if (signed)
            params.put("signed", "1");
        params.put("publish_date", publish_date);
        if (post_id > 0)
            params.put("post_id", post_id);
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params, true);
        JSONObject response = root.getJSONObject("response");
        long res_post_id = response.optLong("post_id");
        return res_post_id;
    }
    private JSONObject sendRequest(Params params, boolean is_post) throws IOException, JSONException, KException {
        String url = getSignedUrl(params, is_post);
        String body="";
        if(is_post)
            body=params.getParamsString();
        Log.i(TAG, "url="+url);
        if(body.length()!=0)
            Log.i(TAG, "body="+body);
        String response="";
        for(int i=1;i<=MAX_TRIES;++i){
            try{
                if(i!=1)
                    Log.i(TAG, "try "+i);
                response = sendRequestInternal(url, body, is_post);
                break;
            }catch(javax.net.ssl.SSLException ex){
                processNetworkException(i, ex);
            }catch(java.net.SocketException ex){
                processNetworkException(i, ex);
            }
        }
        Log.i(TAG, "response="+response);
        JSONObject root=new JSONObject(response);
        checkError(root, url);
        return root;
    }
    private String getSignedUrl(Params params, boolean is_post) {
        params.put("access_token", access_token);
        if(!params.contains("v"))
            params.put("v", API_VERSION);

        String args = "";
        if(!is_post)
            args=params.getParamsString();

        return BASE_URL+params.method_name+"?"+args;
    }
    private String sendRequestInternal(String url, String body, boolean is_post) throws IOException {
        HttpURLConnection connection=null;
        try{
            connection = (HttpURLConnection)new URL(url).openConnection();
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            connection.setUseCaches(false);
            connection.setDoOutput(is_post);
            connection.setDoInput(true);
            connection.setRequestMethod(is_post?"POST":"GET");
            if(enable_compression)
                connection.setRequestProperty("Accept-Encoding", "gzip");
            if(is_post)
                connection.getOutputStream().write(body.getBytes("UTF-8"));
            int code=connection.getResponseCode();
            Log.i(TAG, "code="+code);
            InputStream is = new BufferedInputStream(connection.getInputStream(), 8192);
            String enc=connection.getHeaderField("Content-Encoding");
            if(enc!=null && enc.equalsIgnoreCase("gzip"))
                is = new GZIPInputStream(is);
            String response= Utils.convertStreamToString(is);
            return response;
        }
        finally{
            if(connection!=null)
                connection.disconnect();
        }
    }
    private void processNetworkException(int i, IOException ex) throws IOException {
        ex.printStackTrace();
        if(i==MAX_TRIES)
            throw ex;
    }
    private void checkError(JSONObject root, String url) throws JSONException,KException {
        if(!root.isNull("error")){
            JSONObject error=root.getJSONObject("error");
            int code=error.getInt("error_code");
            String message=error.getString("error_msg");
            KException e = new KException(code, message, url);
            if (code==14) {
                e.captcha_img = error.optString("captcha_img");
                e.captcha_sid = error.optString("captcha_sid");
            }
            if (code==17)
                e.redirect_uri = error.optString("redirect_uri");
            throw e;
        }
        if(!root.isNull("execute_errors")){
            JSONArray errors=root.getJSONArray("execute_errors");
            if(errors.length()==0)
                return;
            //only first error is processed if there are multiple
            JSONObject error=errors.getJSONObject(0);
            int code=error.getInt("error_code");
            String message=error.getString("error_msg");
            KException e = new KException(code, message, url);
            if (code==14) {
                e.captcha_img = error.optString("captcha_img");
                e.captcha_sid = error.optString("captcha_sid");
            }
            if (code==17)
                e.redirect_uri = error.optString("redirect_uri");
            throw e;
        }
    }
}
