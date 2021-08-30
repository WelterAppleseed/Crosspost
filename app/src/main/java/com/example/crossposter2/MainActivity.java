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
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import com.example.crossposter2.account.Account;
import com.example.crossposter2.account.Api;
import com.example.crossposter2.fb.FbConstant;
import com.example.crossposter2.utils.ClickListeners;
import com.example.crossposter2.utils.ImageManager;
import com.example.crossposter2.utils.ImageUI;
import com.example.crossposter2.utils.RealPathUtil;
import com.example.crossposter2.utils.Utils;
import com.example.crossposter2.vk.VKWallPostCommand;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareContent;
import com.facebook.share.model.ShareMedia;
import com.facebook.share.model.ShareMediaContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.widget.ShareDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.vk.api.sdk.VK;
import com.vk.api.sdk.VKApiCallback;
import com.vk.api.sdk.VKTokenExpiredHandler;
import com.vk.api.sdk.auth.VKAccessToken;
import com.vk.api.sdk.auth.VKAuthCallback;
import com.vk.api.sdk.auth.VKScope;


import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    //test
    ImageButton btn_camera;
    ImageButton btn_mic;
    LinearLayout logToolbarShow, addToolbarShow, postToolbarShow;
    LinearLayout[] container;
    RelativeLayout logToolbar, addToolbar, postToolbar;
    RelativeLayout[] content;


    private final int BOX_WIDTH = 185;
    private final int BOX_HEIGHT = 185;
    private final int DIALOG_INPUT = 3;
    private ArrayList<String> imagesEncodedList;
    private ImageView addImageIcon;
    private final String TAG = "Crossposter";
    private String tgChannelName = "", defChannelName = "";
    private FirebaseAuth mAuth;
    private SharedPreferences token_prf, switch_prf, tgPrf;
    private SharedPreferences.Editor editor;
    private String fb_token;
    private long fb_user_id;
    boolean facebook_switch_state, vk_switch_state, telegram_switch_state;
    private CallbackManager callbackManager;
    private LoginButton connectFbPerformedButton;
    private ImageButton logoutSectionButton, connectSectionButton, postSectionButton;
    private static Map<Uri, String> uris = new HashMap<>();
    public LinearLayout imgLayout, bottomButtonsLayout;
    private ShareDialog shareDialog;
    private Switch fS, tS, vS, oS;
    private FragmentManager manager;
    private Button connectVk, connectTg, connectFb, logoutVk, logoutTg, logoutFb, post;
    private ImageButton infoButton, reportButton;
    private TextView copyText;
    private FrameLayout performEditText;
    View.OnClickListener longClickListener;
    View.OnTouchListener imgListener;
    VKTokenExpiredHandler tokenHandler = () -> Toast.makeText(MainActivity.this, "Token has been expired.", Toast.LENGTH_SHORT).show();
    VK vk;
    private static ArrayList<String> images = new ArrayList<>();
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
                        try {
                            imagesEncodedList = new ArrayList<>();
                            assert data != null;
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
                                ImageView imageView = new ImageView(getApplicationContext());
                                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                imageView.setVisibility(View.VISIBLE);
                                imageView.setImageBitmap(bem);
                                imageView.setLayoutParams(params);
                                imageView.setOnTouchListener(imgListener);
                                imageView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_corners));
                                imageView.setForeground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.images_border));
                                imageView.setClipToOutline(true);
                                imgLayout.addView(imageView);
                                uris.put(mImageUri, imageView.getDrawable().toString());
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
                                        uris.put(uri, imageView.getDrawable().toString());
                                    }
                                    System.out.println(images.size());
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
        setContentView(R.layout.activity_main);
        //FB
        facebookShowDialogInit();
        fbRestore();
        //VK
        VK.addTokenExpiredHandler(tokenHandler);
        //
        setToolbarY();
        checkAuth();
    }

    public static Map<Uri, String> getUris() {
        return uris;
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


    private void facebookShowDialogInit() {
        callbackManager = CallbackManager.Factory.create();
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
    }

    private void setToolbarY() {
        logToolbarShow = (LinearLayout) findViewById(R.id.log_show);
        addToolbarShow = (LinearLayout) findViewById(R.id.add_show);
        postToolbarShow = (LinearLayout) findViewById(R.id.post_show);
        logToolbarShow.setY(Utils.dpToPx(110));
        addToolbarShow.setY(Utils.dpToPx(110));
        postToolbarShow.setY(Utils.dpToPx(110));
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
            getIntent().removeExtra("access_token_fb");
            getIntent().removeExtra("user_id_fb");
        }
        fbAccount.restoreFb(this);
        if (fbAccount.access_token_fb != null) {
            fbApi = new Api(fbAccount.access_token_fb, FbConstant.appId);
        }
    }

    private void createToolbarUI() {
        logToolbar = (RelativeLayout) findViewById(R.id.log_toolbar);
        addToolbar = (RelativeLayout) findViewById(R.id.add_toolbar);
        postToolbar = (RelativeLayout) findViewById(R.id.post_toolbar);
        content = new RelativeLayout[]{logToolbar, addToolbar, postToolbar};
        container = new LinearLayout[]{logToolbarShow, addToolbarShow, postToolbarShow};


        switch_prf = MainActivity.this.getSharedPreferences("state", Context.MODE_PRIVATE);
        facebook_switch_state = switch_prf.getBoolean("facebook_switch_state", false);
        vk_switch_state = switch_prf.getBoolean("vk_switch_state", false);
        telegram_switch_state = switch_prf.getBoolean("telegram_switch_state", false);

        fS = (Switch) findViewById(R.id.facebook_switch);
        vS = (Switch) findViewById(R.id.vk_switch);
        tS = (Switch) findViewById(R.id.telegram_switch);

        fS.setOnCheckedChangeListener(new ClickListeners().onFacebookSwitchClickListener(MainActivity.this));

        logoutVk = (Button) findViewById(R.id.logout_vk);
        logoutTg = (Button) findViewById(R.id.log_out_twitter);
        logoutFb = (Button) findViewById(R.id.log_out_facebook);


        connectVk = (Button) findViewById(R.id.add_vk);
        connectTg = (Button) findViewById(R.id.add_twitter);
        connectFb = (Button) findViewById(R.id.add_facebook);
        connectFbPerformedButton = (LoginButton) findViewById(R.id.perform_login_button);
        connectFbPerformedButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                String[] info = {"id", "name", "email", "gender", "birthday"};
                String accessToken = loginResult.getAccessToken()
                        .getToken();
                Log.i("accessToken", accessToken);

                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object,
                                                    GraphResponse response) {

                                Log.i("LoginActivity",
                                        response.toString());
                                try {
                                    info[0] = object.getString("id");
                                    try {
                                        URL profile_pic = new URL(
                                                "http://graph.facebook.com/" + info[0] + "/picture?type=large");
                                        Log.i("profile_pic",
                                                profile_pic + "");

                                    } catch (MalformedURLException e) {
                                        e.printStackTrace();
                                    }
                                    info[1] = object.getString("name");
                                    info[2] = object.getString("email");
                                    info[3] = object.getString("gender");
                                    info[4] = object.getString("birthday");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields",
                        "id,name,email,gender, birthday");
                request.setParameters(parameters);
                request.executeAsync();
                fbAccount.access_token_fb = accessToken;
                fbAccount.user_id_fb = Long.parseLong(info[0]);
                fbAccount.saveFb(MainActivity.this);
                fbApi = new Api(fbAccount.access_token_fb, FbConstant.appId);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

        if (fbApi == null) {
            Utils.setDisable(MainActivity.this, fS, logoutFb);
        } else {
            Utils.setEnable(MainActivity.this, fS, connectFb, facebook_switch_state);
        }
        if (!VK.isLoggedIn()) {
            Utils.setDisable(MainActivity.this, vS, logoutVk);
        } else {
            Utils.setEnable(MainActivity.this, vS, connectVk, vk_switch_state);
        }
        /*if (tgChannelName.equals(defChannelName)) {
            System.out.println("1");
            Utils.setDisable(MainActivity.this, tS, logoutTg);
        } else {
            System.out.println("2");
            Utils.setEnable(MainActivity.this, tS, connectTg, telegram_switch_state);
        }*/

        logoutVk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VK.logout();
                Utils.onLogout(MainActivity.this, vS, logoutVk, connectVk);
                checkAuth();
            }
        });
        logoutFb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fbApi = null;
                fbAccount.access_token_fb = null;
                fbAccount.user_id_fb = 0;
                fbAccount.saveFb(MainActivity.this);
                LoginManager.getInstance().logOut();
                Utils.onLogout(MainActivity.this, fS, logoutFb, connectFb);
                checkAuth();
            }
        });
       /* logoutTg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor = getSharedPreferences("channel", Context.MODE_PRIVATE).edit();
                editor.putString("channel_name", getResources().getString(R.string.type_here));
                editor.apply();
                ClickListeners.onLogout(MainActivity.this, tS, logoutTg, connectTg);
                checkAuth();
            }
        });*/
        connectVk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onVkClick();
            }
        });
        connectFb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFbClick();
            }
        });
        //connectTg.setOnClickListener(new ClickListeners().onTelegramConnectListener(MainActivity.this, connectTg, logoutTg, tS));

        post = (Button) findViewById(R.id.btn_post);
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (vS.isChecked()) {
                                System.out.println("VS");
                                vkAction();
                            }
                            if (tS.isChecked()) {
                                System.out.println("TS");
                                tgAction();
                            }
                            if (fS.isChecked()) {
                                System.out.println("FS");
                                fbAction();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        infoButton = (ImageButton) findViewById(R.id.question_button);
        reportButton = (ImageButton) findViewById(R.id.warning_button);

        infoButton.setOnClickListener(new ClickListeners().onInfoClickListener(MainActivity.this));
        reportButton.setOnClickListener(new ClickListeners().onReportClickListener(MainActivity.this));
    }
    private void checkAuth() {
           /* tgPrf = MainActivity.this.getSharedPreferences("channel", Context.MODE_PRIVATE);
            defChannelName = getResources().getString(R.string.type_here);
            tgChannelName = tgPrf.getString("channel_name", defChannelName);*/
            if (!VK.isLoggedIn() && fbApi == null /*&& defChannelName.equals(tgChannelName)*/) {
                Intent intent = new Intent(MainActivity.this, StartPage.class);
                startActivity(intent);
                finish();
            } else {
                createUI();
            }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void createUI() {
        createToolbarUI();
        logoutSectionButton = (ImageButton) findViewById(R.id.log_out);
        connectSectionButton = (ImageButton) findViewById(R.id.add_button);
        postSectionButton = (ImageButton) findViewById(R.id.send_post);

        logoutSectionButton.setOnClickListener(new ClickListeners().onBottomLayoutCallListener(container, logToolbarShow, content, logToolbar));
        connectSectionButton.setOnClickListener(new ClickListeners().onBottomLayoutCallListener(container, addToolbarShow, content, addToolbar));
        postSectionButton.setOnClickListener(new ClickListeners().onBottomLayoutCallListener(container, postToolbarShow, content, postToolbar));

        imgLayout = (LinearLayout) findViewById(R.id.img_layout);
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

        messageEditText = (EditText) findViewById(R.id.edit_msg);
        messageEditText.requestFocus();
        performEditText = (FrameLayout) findViewById(R.id.perform_edit_text);
        performEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(messageEditText, InputMethodManager.SHOW_IMPLICIT);
            }
        });
        bottomButtonsLayout = (LinearLayout) findViewById(R.id.bottom_buttons_layout);

    }

    private void onVkClick() {
        VK.login(this, Arrays.asList(VKScope.WALL, VKScope.PHOTOS));
    }

    private void vkAction() {
        Collection<Uri> values = uris.keySet();
        ArrayList<Uri> uriList = new ArrayList<>(values);

        VK.execute(new VKWallPostCommand(messageEditText.getText().toString(), uriList, VK.getUserId(), false, false), new VKApiCallback<Integer>() {
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


    private void tgAction() {
       /* if (!tgChannelName.equals(defChannelName)) {
            try {
                Collection<Uri> values = uris.keySet();
                ArrayList<Uri> uriList = new ArrayList<>(values);
                Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_SUBJECT, messageEditText.getText().toString());
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList);
                startActivity(intent);
                System.out.println("321");
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Telegram not Installed", Toast.LENGTH_SHORT).show();
            }
        }*/
    }


    private void onFbClick() {
        connectFbPerformedButton.performClick();
    }

    private void fbAction() {
        Collection<Uri> values = uris.keySet();
        ArrayList<Uri> uriList = new ArrayList<>(values);
        if (uriList.size() > 6) {
            Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            intent.putExtra(Intent.EXTRA_SUBJECT, messageEditText.getText().toString());
            intent.setType("image/jpeg");
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList);
            startActivity(intent);
        } else {
            if (Utils.isAppInstalled(MainActivity.this, "com.facebook.katana")) {
                if (ShareDialog.canShow(ShareMediaContent.class) | ShareDialog.canShow(ShareContent.class)) {
                    ShareContent content = setShareContent(uriList);
                    shareDialog.show(content);
                    runOnUiThread(clipboardRunnable);
                }
            } else {

                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + "com.facebook.katana")));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + "com.facebook.katana")));
                }
            }
        }
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
                Utils.onLogin(MainActivity.this, vS, connectVk, logoutVk);
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

    @Override
    protected void onPause() {
        super.onPause();
        editor = MainActivity.this.getSharedPreferences("state", Context.MODE_PRIVATE).edit();
        editor.putBoolean("facebook_switch_state", fS.isChecked());
        editor.putBoolean("vk_switch_state", vS.isChecked());
        editor.putBoolean("telegram_switch_state", tS.isChecked());
        editor.apply();
    }

   private ShareContent setShareContent(ArrayList<Uri> uris) {
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

    private void requestRead() {
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


