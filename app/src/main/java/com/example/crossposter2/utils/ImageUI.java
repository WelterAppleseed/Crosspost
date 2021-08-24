package com.example.crossposter2.utils;

import android.Manifest;
import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.FileUriExposedException;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.crossposter2.MainActivity;

import org.apache.commons.io.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Permission;

public class ImageUI extends Activity{
    private boolean touched;
    private final double dif_const = 0.00304615384;
    private float yCoOrdinate;
    private ViewPropertyAnimator viewPropertyAnimator, secViewPropertyAnimator;
    private ResizeMode _resizeMode;
    private ScaleMode _scaleMode;
    private int _boxWidth = 250;
    private int _boxHeight = 250;
    private boolean _isRecycleSrcBitmap;

    public ImageUI() {
    }

    public Bitmap getScaleImage(Context context, Uri uri, int orientation) throws IOException {
        Bitmap bm = null;
        try {
            InputStream in = context.getContentResolver().openInputStream(uri);
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, o);
            in.close();

            int origWidth = o.outWidth;
            int origHeight = o.outHeight;
            int bytesPerPixel = 2;
            int maxSize = 480 * 800 * bytesPerPixel;

            int desiredWidth = 250;
            int desiredHeight = 250;

            int desiredSize = desiredWidth * desiredHeight * bytesPerPixel;
            if (desiredSize < maxSize) maxSize = desiredSize;
            int scale = 1;
            int origSize = origWidth * origHeight * bytesPerPixel;

            if (origWidth > origHeight) {
                scale = Math.round((float) origHeight / (float) desiredHeight);
            } else {
                scale = Math.round((float) origWidth / (float) desiredWidth);
            }
            if (orientation == 90 || orientation == 270) {
                origWidth = o.outHeight;
                origHeight = o.outWidth;
            } else {
                origWidth = o.outWidth;
                origHeight = o.outHeight;
            }
            while ((origWidth * origHeight * bytesPerPixel) * (1 / Math.pow(scale, 2)) > maxSize) {
                scale++;
            }
            o = new BitmapFactory.Options();
            o.inSampleSize = scale;
            o.inPreferredConfig = Bitmap.Config.RGB_565;

            in = context.getContentResolver().openInputStream(uri);
            bm = BitmapFactory.decodeStream(in, null, o);
            in.close();
            if (orientation > 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(orientation);
                Bitmap decodedBitmap = bm;
                bm = Bitmap.createBitmap(decodedBitmap, 0, 0, bm.getWidth(),
                        bm.getHeight(), matrix, true);
                if (decodedBitmap != null && !decodedBitmap.equals(bm)) {
                    recycleBitmap(decodedBitmap);
                }
            }
            return bm;
            } catch (Exception e) {
            e.printStackTrace();
        }
        return bm;
    }

    public static void recycleBitmap(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) return;
        bitmap.recycle();
        System.gc();
    }


}

