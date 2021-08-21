package com.example.crossposter2.fb;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.crossposter2.MainActivity;
import com.example.crossposter2.R;
import com.facebook.CallbackManager;
import com.facebook.share.ShareApi;
import com.facebook.share.model.ShareContent;
import com.facebook.share.model.ShareMedia;
import com.facebook.share.model.ShareMediaContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class FbShare extends AppCompatActivity {

    public SharePhotoContent setMedia(Bitmap[] img, String text) {
                ArrayList<SharePhoto> media = new ArrayList<>();
                SharePhoto[] photos = new SharePhoto[img.length];
                for (int i = 0; i < img.length; i++) {
                    photos[i] = new SharePhoto.Builder()
                            .setBitmap(img[i])
                            .setCaption(text)
                            .build();
                    media.add(photos[i]);
                }
        return new SharePhotoContent.Builder()
                .addPhotos(media)
                .build();
        }

}
