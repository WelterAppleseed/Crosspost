package com.example.crossposter2.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public final class ImageManager {
    private Context _ctx;
    private int _boxWidth;
    private int _boxHeight;
    private ResizeMode _resizeMode;
    private ScaleMode _scaleMode;
    private Bitmap.Config _rgbMode;
    private boolean _isScale;
    private boolean _isResize;
    private boolean _isCrop;
    private boolean _isRecycleSrcBitmap;
    private boolean _useOrientation;

    public ImageManager(Context ctx, int boxWidth, int boxHeight) {
        this(ctx);
        _boxWidth = boxWidth;
        _boxHeight = boxHeight;
    }

    public ImageManager(Context ctx) {
        _ctx = ctx;
        _isScale = false;
        _isResize = false;
        _isCrop = false;
        _isRecycleSrcBitmap = true;
        _useOrientation = false;
    }

    public ImageManager setResizeMode(ResizeMode mode) {
        _resizeMode = mode;
        return this;
    }

    public ImageManager setScaleMode(ScaleMode mode) {
        _scaleMode = mode;
        return this;
    }

    public ImageManager setRgbMode(Bitmap.Config mode) {
        _rgbMode = mode;
        return this;
    }

    public ImageManager setIsScale(boolean isScale) {
        _isScale = isScale;
        return this;
    }

    public ImageManager setIsResize(boolean isResize) {
        _isResize = isResize;
        return this;
    }

    public ImageManager setIsCrop(boolean isCrop) {
        _isCrop = isCrop;
        return this;
    }

    public ImageManager setUseOrientation(boolean value) {
        _useOrientation = value;
        return this;
    }

    public ImageManager setIsRecycleSrcBitmap(boolean value) {
        _isRecycleSrcBitmap = value;
        return this;
    }

    public Bitmap getFromFile(String path, int orientation) {
        Uri uri = Uri.parse(path);
        Bitmap bitmap = scale(new StreamFromFile(_ctx, path), orientation);
        return getFromBitmap(bitmap);
    }

    public Bitmap getFromBitmap(Bitmap bitmap) {
        if (bitmap == null) return null;
        if (_isResize) bitmap = resize(bitmap);
        if (_isCrop) bitmap = crop(bitmap);
        return bitmap;
    }

    public byte[] getRawFromFile(String path) {
        return getRawFromFile(path, 75);
    }

    public byte[] getRawFromFile(String path, int compressRate) {
        Bitmap scaledBitmap = getFromFile(path,0);
        if (scaledBitmap == null) return null;

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, compressRate, output);
        recycleBitmap(scaledBitmap);

        byte[] rawImage = output.toByteArray();
        if (rawImage == null) {

            return null;
        }

        return rawImage;
    }

    public Bitmap getFromByteArray(byte[] rawImage) {
        Bitmap bitmap = scale(new StreamFromByteArray(rawImage), -1);
        return getFromBitmap(bitmap);
    }

    @SuppressLint("NewApi")
    private Bitmap scale(IStreamGetter streamGetter, int orientation) {
        try {
            InputStream in = streamGetter.Get();
            if (in == null) return null;

            Bitmap bitmap = null;
            Bitmap.Config rgbMode = _rgbMode != null ? _rgbMode : Bitmap.Config.RGB_565;

            if (!_isScale) {
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inPreferredConfig = rgbMode;
                if (android.os.Build.VERSION.SDK_INT >= 11) {
                    o.inMutable = true;
                }
                bitmap = BitmapFactory.decodeStream(in, null, o);
                in.close();
                return bitmap;
            }

            if (_boxWidth == 0 || _boxHeight == 0) {

                if (in != null) in.close();
                return null;
            }

            ScaleMode scaleMode = _scaleMode != null ? _scaleMode : ScaleMode.EQUAL_OR_GREATER;
            int bytesPerPixel = rgbMode == Bitmap.Config.ARGB_8888 ? 4 : 2;
            int maxSize = 480 * 800 * bytesPerPixel;
            int desiredSize = _boxWidth * _boxHeight * bytesPerPixel;
            if (desiredSize < maxSize) maxSize = desiredSize;

            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, o);
            in.close();
            int scale = 1;

            int origWidth;
            int origHeight;
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
            if (scaleMode == ScaleMode.EQUAL_OR_LOWER) {
                scale++;
            }

            o = new BitmapFactory.Options();
            o.inSampleSize = scale;
            o.inPreferredConfig = rgbMode;

            in = streamGetter.Get();
            if (in == null) return null;
            bitmap = BitmapFactory.decodeStream(in, null, o);
            in.close();

            if (orientation > 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(orientation);
                Bitmap decodedBitmap = bitmap;
                bitmap = Bitmap.createBitmap(decodedBitmap, 0, 0, bitmap.getWidth(),
                        bitmap.getHeight(), matrix, true);
                if (decodedBitmap != null && !decodedBitmap.equals(bitmap)) {
                    recycleBitmap(decodedBitmap);
                }
            }

            return bitmap;
        }
        catch (IOException e) {

            return null;
        }
    }

    private Bitmap resize(Bitmap sourceBitmap) {
        if (sourceBitmap == null) return null;
        if (_resizeMode == null) _resizeMode = ResizeMode.EQUAL_OR_GREATER;
        float srcRatio;
        float boxRatio;
        int srcWidth = 0;
        int srcHeight = 0;
        int resizedWidth = 0;
        int resizedHeight = 0;

        srcWidth = sourceBitmap.getWidth();
        srcHeight = sourceBitmap.getHeight();

        if (_resizeMode == ResizeMode.EQUAL_OR_GREATER && (srcWidth <= _boxWidth || srcHeight <= _boxHeight) ||
                _resizeMode == ResizeMode.EQUAL_OR_LOWER && srcWidth <= _boxWidth && srcHeight <= _boxHeight) {

            return sourceBitmap;
        }

        srcRatio = (float)srcWidth / (float)srcHeight;
        boxRatio = (float)_boxWidth / (float)_boxHeight;

        if (srcRatio > boxRatio && _resizeMode == ResizeMode.EQUAL_OR_GREATER ||
                srcRatio < boxRatio && _resizeMode == ResizeMode.EQUAL_OR_LOWER) {
            resizedHeight = _boxHeight;
            resizedWidth = (int)((float)resizedHeight * srcRatio);
        }
        else {
            resizedWidth = _boxWidth;
            resizedHeight = (int)((float)resizedWidth / srcRatio);
        }

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(sourceBitmap, resizedWidth, resizedHeight, true);

        if (_isRecycleSrcBitmap && !sourceBitmap.equals(resizedBitmap)) {
            recycleBitmap(sourceBitmap);
        }

        return resizedBitmap;
    }

    private Bitmap crop(Bitmap sourceBitmap) {
        if (sourceBitmap == null) return null;
        int srcWidth = sourceBitmap.getWidth();
        int srcHeight = sourceBitmap.getHeight();
        int croppedX = 0;
        int croppedY = 0;

        croppedX = (srcWidth > _boxWidth) ? (int)((srcWidth - _boxWidth) / 2) : 0;
        croppedY = (srcHeight > _boxHeight) ? (int)((srcHeight - _boxHeight) / 2) : 0;
        _boxWidth = (srcWidth > _boxWidth) ? 200 : srcWidth;
        _boxHeight = (srcHeight > _boxHeight) ? 200: srcHeight;

        if (_boxWidth < _boxHeight) {
            _boxHeight = _boxWidth;
        } else {
            _boxWidth = _boxHeight;
        }
        Log.i("X", Integer.toString(croppedX));
        Log.i("Y", Integer.toString(croppedY));
        System.out.println(sourceBitmap.getWidth() + " " + _boxWidth + " " + croppedX);
        if (croppedX == 0 && croppedY == 0)
            return sourceBitmap;

        Bitmap croppedBitmap = null;
        try {
            croppedBitmap = Bitmap.createBitmap(sourceBitmap, croppedX, croppedY, _boxWidth, _boxHeight);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        if (_isRecycleSrcBitmap && !sourceBitmap.equals(croppedBitmap)) {
            recycleBitmap(sourceBitmap);
        }

        return croppedBitmap;
    }

    public static void recycleBitmap(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) return;
        bitmap.recycle();
        System.gc();
    }

    private static interface IStreamGetter {
        public InputStream Get();
    }

    private static class StreamFromFile implements IStreamGetter {
        private String _path;
        private Context _ctx;
        public StreamFromFile(Context ctx, String path) {
            _path = path;
            _ctx = ctx;
        }
        @SuppressWarnings("resource")
        public InputStream Get() {
            try {
                Uri uri = Uri.parse(_path);
                return "content".equals(uri.getScheme())
                        ? _ctx.getContentResolver().openInputStream(uri)
                        : new FileInputStream(_path);
            }
            catch (FileNotFoundException e) {

                return null;
            }
        }
    }

    private static class StreamFromByteArray implements IStreamGetter {
        private byte[] _rawImage;
        public StreamFromByteArray(byte[] rawImage) {
            _rawImage = rawImage;
        }
        public InputStream Get() {
            if (_rawImage == null) return null;
            return new ByteArrayInputStream(_rawImage);
        }
    }
    }
    enum ScaleMode {
        EQUAL_OR_GREATER,
        EQUAL_OR_LOWER
    }
    enum ResizeMode {
        EQUAL_OR_GREATER,
        EQUAL_OR_LOWER
    }