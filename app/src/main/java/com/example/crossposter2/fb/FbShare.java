package com.example.crossposter2.fb;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import com.example.crossposter2.R;
import com.facebook.share.ShareApi;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;

public class FbShare extends Activity {

    private void sharePhotoToFacebook(Bitmap image) {
        SharePhoto photo = new SharePhoto.Builder()
                .setBitmap(image)
                .setCaption("Nice Place.....")
                .build();

        SharePhotoContent content = new SharePhotoContent.Builder()
                .addPhoto(photo)
                .build();

        ShareApi.share(content, null);
        Toast.makeText(this, "Facebook Photo Upload Complated", Toast.LENGTH_LONG).show();

    }
}
