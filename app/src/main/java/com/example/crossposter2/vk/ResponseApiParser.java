package com.example.crossposter2.vk;

import com.vk.api.sdk.VKApiResponseParser;

import org.json.JSONObject;

 class ResponseApiParser implements VKApiResponseParser {

    public Integer parse(String response) {
        try {
            return (new JSONObject(response)).getJSONObject("response").getInt("post_id");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}