package com.example.crossposter2;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;


import com.example.crossposter2.account.Account;
import com.example.crossposter2.account.Api;
import com.example.crossposter2.fb.FbConstant;
import com.example.crossposter2.tg.IsAppAvailable;
import com.example.crossposter2.utils.ClickListeners;
import com.example.crossposter2.utils.CrossposterDialogFragment;
import com.example.crossposter2.utils.ImageManager;
import com.example.crossposter2.utils.ImageUI;
import com.example.crossposter2.utils.RealPathUtil;
import com.example.crossposter2.utils.Utils;
import com.example.crossposter2.vk.VKWallPostCommand;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.widget.ShareButton;
import com.facebook.share.widget.ShareDialog;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.vk.api.sdk.VK;
import com.vk.api.sdk.VKApiCallback;
import com.vk.api.sdk.VKTokenExpiredHandler;
import com.vk.api.sdk.auth.VKAccessToken;
import com.vk.api.sdk.auth.VKAuthCallback;
import com.vk.api.sdk.auth.VKScope;


import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private final int BOX_WIDTH = 185;
    private final int BOX_HEIGHT = 185;
    private final int SHOW_MESSENGERS_DIALOG = 2;
    private final int DIALOG_INPUT = 3;
    private final int LOGOUT_FORM = 4;
    private int imgCount = 5;
    private float xCoOrdinate, yCoOrdinate;
    private ArrayList<String> imagesEncodedList;
    ImageView addImageIcon;
    private HorizontalScrollView scrollView;
    ImageView[] imgGroup;
    private final String TAG = "Crossposter";
    private final int FACEBOOK_BUTTON = 4;
    final String SAVED_TG_CHANNEL = "saved_tg_channel";
    private FirebaseAuth mAuth;
    private String imageEncoded;
    SharedPreferences token_prf, switch_prf;
    SharedPreferences.Editor editor;
    String fb_token, vk_token;
    private long fb_user_id, vk_user_id;
    EditText input;
    private ArrayList<Bitmap> bitmaps;
    boolean facebook_switch_state, vk_switch_state, telegram_switch_state, unknown_switch_state;
    private CallbackManager callbackManager;
    private LoginButton loginButton;
    ImageButton logoutButton;
    ArrayList<Uri> uris = new ArrayList<>();
    LoginButton logoutButton1;
    ShareButton shareButton;
    public LinearLayout imgLayout;
    ShareDialog shareDialog;
    Switch fS, tS, vS, oS;
    ImageButton postButton;
    FragmentManager manager;
    Button addImageButton, post;
    ImageButton dialogButton;
    View.OnClickListener longClickListener;
    View.OnTouchListener imgListener;
    View.OnClickListener postListener = new ClickListeners().getPostClickListener(this);
    VKTokenExpiredHandler tokenHandler = new VKTokenExpiredHandler() {
        @Override
        public void onTokenExpired() {
            Toast.makeText(MainActivity.this, "Token has been expired.", Toast.LENGTH_SHORT).show();
        }
    };
    VK vk;
    Bitmap[] images = new Bitmap[5];
    EditText messageEditText;
    Account fbAccount = new Account();
    ActivityResultLauncher<Intent> addImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @SuppressLint("ResourceType")
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Bitmap bitmap = null;
                        InputStream in;
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(BOX_WIDTH, BOX_HEIGHT);
                        params.setMargins(Utils.dpToPx(10), Utils.dpToPx(6), 0, 0);
                        ImageManager imageManager = new ImageManager(getApplication(), BOX_WIDTH, BOX_HEIGHT);
                        imgListener = new ClickListeners().getImageListener(imgLayout);
                        longClickListener = new ClickListeners().getReadyToImageListener(imgListener);
                        try {
                            imagesEncodedList = new ArrayList<String>();
                            uris.clear();
                            if (data.getData() != null) {
                                Uri mImageUri = data.getData();
                                String path = RealPathUtil.getRealPathFromURI_API19(getApplicationContext(), mImageUri);
                                int orientation = getCameraPhotoOrientation(getApplicationContext(), mImageUri, path);
                                Bitmap bm = new ImageUI().getScaleImage(getApplicationContext(), mImageUri, orientation);
                                Bitmap bem = imageManager
                                        .setIsCrop(true)
                                        .setIsScale(true)
                                        .setIsResize(true)
                                        .getFromBitmap(bm);
                                uris.add(mImageUri);
                                ImageView imageView = new ImageView(getApplicationContext());
                                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                imageView.setVisibility(View.VISIBLE);
                                imageView.setImageBitmap(bem);
                                imageView.setOnClickListener(longClickListener);
                                imageView.setLayoutParams(params);
                                imageView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_corners));
                                imageView.setForeground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.images_border));
                                imageView.setClipToOutline(true);
                                imgLayout.addView(imageView);
                            } else {
                                if (data.getClipData() != null) {
                                    ClipData mClipData = data.getClipData();
                                    for (int i = 0; i < mClipData.getItemCount(); i++) {
                                        ClipData.Item item = mClipData.getItemAt(i);
                                        Uri uri = item.getUri();
                                        String path = RealPathUtil.getRealPathFromURI_API19(getApplicationContext(), uri);
                                        int orientation = getCameraPhotoOrientation(getApplicationContext(), uri, path);
                                        Bitmap bm = new ImageUI().getScaleImage(getApplicationContext(), uri, orientation);
                                        Bitmap bem = imageManager
                                                .setIsCrop(true)
                                                .setIsScale(true)
                                                .setIsResize(true)
                                                .getFromBitmap(bm);
                                        uris.add(uri);
                                        ImageView imageView = new ImageView(getApplicationContext());
                                        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                        imageView.setVisibility(View.VISIBLE);
                                        imageView.setImageBitmap(bem);
                                        imageView.setOnTouchListener(imgListener);
                                        imageView.setLayoutParams(params);
                                        imageView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_corners));
                                        imageView.setForeground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.images_border));
                                        imageView.setClipToOutline(true);
                                        imgLayout.addView(imageView);
                                    }
                                    imgLayout.setVisibility(View.VISIBLE);
                                    Log.v("LOG_TAG", "Selected Images " + uris.size());
                                }
                            }
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_LONG)
                                    .show();
                            e.printStackTrace();
                        }
                    } else {

                    }
                }
            });

    Api fbApi;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        manager = getSupportFragmentManager();
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_main);
        //FB
        shareDialog = new ShareDialog(this);
        fbRestore();
        //VK
        VK.addTokenExpiredHandler(tokenHandler);
        //
        checkAuth();
    }

    public int getCameraPhotoOrientation(Context context, Uri imageUri,
                                         String imagePath) {
        int rotate = 0;
        try {
            context.getContentResolver().notifyChange(imageUri, null);
            File imageFile = new File(imagePath);
            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }

            Log.i("RotateImage", "Exif orientation: " + orientation);
            Log.i("RotateImage", "Rotate value: " + rotate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    private void checkAuth() {
        if (!VK.isLoggedIn() && fbApi == null) {
            Intent intent = new Intent(MainActivity.this, StartPage.class);
            startActivity(intent);
        } else {
            createUI();
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == SHOW_MESSENGERS_DIALOG) {
            final String[] accounts = {"VK account", "Telegram account", "Facebook account"};
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.Base_Crossposter_AlertDialog));
            builder
                    .setTitle("Connect with...")
                    .setItems(accounts, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    assert accounts != null;
                                    switch (accounts[which]) {
                                        case "VK account": {
                                            onVkClick();
                                            break;
                                        }
                                        case "Telegram account": {
                                            onTgClick();
                                            break;
                                        }
                                        case "Facebook account": {
                                            onFbClick();
                                            break;
                                        }
                                    }
                                }
                            }
                    );
            return builder.create();
        }
        if (id == LOGOUT_FORM) {
            final String[] logoutFrom = {"VK", "Telegram", "Facebook"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                    .setTitle("Log out from...")
                    .setItems(logoutFrom, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            assert logoutFrom != null;
                            switch (logoutFrom[which]) {
                                case "VK": {
                                    if (VK.isLoggedIn()) {
                                        Toast.makeText(getApplicationContext(), "You have logged out of your VK account", Toast.LENGTH_LONG).show();
                                        VK.logout();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "You are not authorized with VK", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                case "Telegram": {
                                    //tgAction();
                                    break;
                                }
                                case "Facebook": {
                                    if (fbApi != null) {
                                        fbApi = null;
                                        fbAccount.access_token_fb = null;
                                        fbAccount.user_id_fb = 0;
                                        fbAccount.saveFb(MainActivity.this);
                                        LoginManager.getInstance().logOut();
                                        Toast.makeText(getApplicationContext(), "You have logged out of your Facebook account", Toast.LENGTH_SHORT).show();
                                        break;
                                    } else {
                                        Toast.makeText(getApplicationContext(), "You are not authorized with Facebook", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                            checkAuth();
                        }
                    });
            return builder.create();
        }
        if (id == DIALOG_INPUT) {
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            input = new EditText(this);
            input.setHint("Type here...");
            token_prf = getPreferences(MODE_PRIVATE);
            String savedText = token_prf.getString(SAVED_TG_CHANNEL, "");
            input.setText(savedText);
            Toast.makeText(this, "Text loaded", Toast.LENGTH_SHORT).show();
            alertDialog.setTitle(R.string.tg_dialog_title);
            alertDialog.setMessage(R.string.tg_dialog_example);
            alertDialog.setView(input);
            alertDialog.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    token_prf = getPreferences(MODE_PRIVATE);
                    SharedPreferences.Editor ed = token_prf.edit();
                    ed.putString(SAVED_TG_CHANNEL, input.getText().toString());
                    ed.apply();
                    Toast.makeText(getApplicationContext(), "Channel name saved", Toast.LENGTH_SHORT).show();
                }
            });
            alertDialog.create();
            alertDialog.show();
        }
        if (id == FACEBOOK_BUTTON) {
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.create();
            alertDialog.show();
        }

        return null;
    }

    private void fbRestore() {
        mAuth = FirebaseAuth.getInstance();
        if (getIntent().getStringExtra("access_token_fb") != null) {
            fb_token = getIntent().getStringExtra("access_token_fb");
            fb_user_id = getIntent().getLongExtra("user_id_fb", 0);
            fbAccount.access_token_fb = fb_token;
            fbAccount.user_id_fb = fb_user_id;
            fbAccount.saveFb(MainActivity.this);
            fbApi = new Api(fbAccount.access_token_fb, FbConstant.appId);
        }
        fbAccount.restoreFb(this);
        if (fbAccount.access_token_fb != null) {
            fbApi = new Api(fbAccount.access_token_fb, FbConstant.appId);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public void createUI() {
        dialogButton = (ImageButton) findViewById(R.id.dialog_button);
        imgLayout = (LinearLayout) findViewById(R.id.img_layout);
        logoutButton = (ImageButton) findViewById(R.id.log_out);
        addImageIcon = (ImageView) findViewById(R.id.add);
        addImageIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestRead();
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                addImageLauncher.launch(intent);
            }
        });
        postButton = (ImageButton) findViewById(R.id.send_post);
        messageEditText = (EditText) findViewById(R.id.edit_msg);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(LOGOUT_FORM);
            }
        });
        postButton.setOnClickListener(postListener);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CrossposterDialogFragment fragment = new CrossposterDialogFragment(MainActivity.this, vk, fbApi);
                fragment.show(manager, "My Dialog");
                }
        });
        scrollView = (HorizontalScrollView) findViewById(R.id.scroll_image_views);
    }


    public void onVkClick() {
        VK.login(this, Arrays.asList(VKScope.WALL, VKScope.PHOTOS));
    }

    public void vkAction() {
        VK.execute(new VKWallPostCommand(messageEditText.getText().toString(), uris, VK.getUserId(), false, false), new VKApiCallback<Integer>() {
            @Override
            public void success(Integer integer) {
                runOnUiThread(successRunnable);
            }

            @Override
            public void fail(@NotNull Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void onTgClick() {
        showDialog(DIALOG_INPUT);
    }

    public void tgAction() {
        final String appName = "org.telegram.messenger";
        final boolean isAppInstalled = IsAppAvailable.isAppAvailable(this.getApplicationContext(), appName);
        if (isAppInstalled) {
            token_prf = getPreferences(MODE_PRIVATE);
            String savedText = token_prf.getString(SAVED_TG_CHANNEL, "");
            if (!savedText.equals("saved_tg_channel")) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, messageEditText.getText().toString());
                intent.setType("text/plain");
                intent.setPackage(appName);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Channel name is not provided", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Telegram not Installed", Toast.LENGTH_SHORT).show();
        }
    }


    public void onFbClick() {
        loginButton.performClick();
    }

    public void fbAction() {
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.putExtra(Intent.EXTRA_SUBJECT, messageEditText.getText().toString());
        intent.setType("image/jpeg");
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        startActivity(intent);
        runOnUiThread(clipboardRunnable);
    }

    Runnable successRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(getApplicationContext(), "Запись успешно добавлена", Toast.LENGTH_LONG).show();
        }
    };
    Runnable clipboardRunnable = new Runnable() {
        @Override
        public void run() {
            ClipboardManager clipboard = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("", messageEditText.getText().toString());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(MainActivity.this, "Some messengers that you have chosen do not support the auto-complete text function, so your text was copied to the clipboard.", Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        VKAuthCallback callback = new VKAuthCallback() {
            @Override
            public void onLogin(@NotNull VKAccessToken vkAccessToken) {
            }

            @Override
            public void onLoginFailed(int i) {

            }
        };
        if (data == null || !VK.onActivityResult(requestCode, resultCode, data, callback)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;

    public void requestRead() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        } else {
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                // Permission Denied
                Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}


