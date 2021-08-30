package com.example.crossposter2.fb;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.example.crossposter2.MainActivity;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareContent;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.ShareMedia;
import com.facebook.share.model.ShareMediaContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.widget.ShareDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FacebookActivity extends FragmentActivity {
    CallbackManager callbackManager;
    ShareDialog shareDialog;
    ArrayList<Uri> uris;
    public FacebookActivity(ArrayList<Uri> uris) {
        this.uris = uris;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        callbackManager = CallbackManager.Factory.create();
        System.out.println("12312312341234123412341234");
        shareDialog = new ShareDialog(this);
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                Log.i("success", result.toString());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
        if (ShareDialog.canShow(ShareMediaContent.class) | ShareDialog.canShow(ShareContent.class)) {
            ShareContent content = setShareContent(uris);
            shareDialog.show(this, content);
        }
    }
    public ShareContent setShareContent(ArrayList<Uri> uris)  {
        Log.i("startSettingUris", uris.toString());
        ArrayList<ShareMedia> photos = new ArrayList<>();
        ShareContent content;
        for (int i = 0; i < uris.size(); i++) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uris.get(i));
                SharePhoto photo = new SharePhoto.Builder()
                        .setBitmap(bitmap)
                        .build();
                photos.add(photo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        content = new ShareMediaContent.Builder()
                .addMedia(photos)
                .build();
        return content;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
