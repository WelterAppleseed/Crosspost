package com.example.crossposter2;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;


import com.example.crossposter2.account.Account;
import com.example.crossposter2.account.Api;
import com.example.crossposter2.fb.FbConstant;
import com.example.crossposter2.tg.IsAppAvailable;
import com.example.crossposter2.utils.ImageUI;
import com.example.crossposter2.vk.VKWallPostCommand;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.widget.ShareButton;
import com.facebook.share.widget.ShareDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.vk.api.sdk.VK;
import com.vk.api.sdk.VKApiCallback;
import com.vk.api.sdk.VKTokenExpiredHandler;
import com.vk.api.sdk.auth.VKAccessToken;
import com.vk.api.sdk.auth.VKAuthCallback;
import com.vk.api.sdk.auth.VKScope;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private final int SHOW_MESSENGERS_DIALOG = 2;
    private final int DIALOG_INPUT = 3;
    private final int LOGOUT_FORM = 4;
    private int imgCount = 5;
    private float xCoOrdinate, yCoOrdinate;
    private ArrayList<String> imagesEncodedList;
    ImageView fi_im, se_im, th_im, fo_im, fv_im;
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
    Button logoutButton;
    ArrayList<Uri> uris = new ArrayList<>();
    LoginButton logoutButton1;
    ShareButton shareButton;
    LinearLayout imgLayout;
    ShareDialog shareDialog;
    Switch fS, tS, vS, oS;
    Button postButton, post, addImageButton;
    Button dialogButton;
    View.OnTouchListener imgListener = new ImageUI().getImageListener();
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
                @Override
                public void onActivityResult(ActivityResult result) {
                    int lenCount = 0;
                    Intent data = result.getData();
                    Bitmap bitmap = null;
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(270, 350);
                    try {
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        imagesEncodedList = new ArrayList<String>();
                        if (data.getData() != null) {
                            Uri mImageUri = data.getData();
                            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mImageUri);
                            imgGroup[lenCount].setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                            imgGroup[lenCount].setLayoutParams(params);
                            imgGroup[lenCount].setVisibility(View.VISIBLE);
                            imgGroup[lenCount].setColorFilter(515);
                            imgGroup[lenCount].setImageBitmap(bitmap);
                            images[lenCount] = bitmap;
                            uris.add(mImageUri);
                        } else {
                            if (data.getClipData() != null) {
                                ClipData mClipData = data.getClipData();
                                ArrayList<Uri> mArrayUri = new ArrayList<Uri>();
                                for (int i = 0; i < mClipData.getItemCount(); i++) {
                                    ClipData.Item item = mClipData.getItemAt(i);
                                    Uri uri = item.getUri();
                                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                                    imgGroup[lenCount].setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                                    imgGroup[lenCount].setLayoutParams(params);
                                    imgGroup[lenCount].setVisibility(View.VISIBLE);
                                    imgGroup[lenCount].setColorFilter(515);
                                    imgGroup[lenCount].setImageBitmap(bitmap);
                                    images[lenCount] = bitmap;
                                    uris.add(uri);
                                    lenCount++;
                                }
                                imgLayout.setVisibility(View.VISIBLE);
                                Log.v("LOG_TAG", "Selected Images " + mArrayUri.size());
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_LONG)
                                .show();
                        e.printStackTrace();
                    }
                }
            });

    Api fbApi;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
        dialogButton = (Button) findViewById(R.id.dialog_button);
        addImageButton = (Button) findViewById(R.id.add_image);

        fi_im = (ImageView) findViewById(R.id.fi);
        fi_im.setOnTouchListener(imgListener);
        se_im = (ImageView) findViewById(R.id.se);
        se_im.setOnTouchListener(imgListener);
        th_im = (ImageView) findViewById(R.id.th);
        th_im.setOnTouchListener(imgListener);
        fo_im = (ImageView) findViewById(R.id.fo);
        fo_im.setOnTouchListener(imgListener);
        fv_im = (ImageView) findViewById(R.id.fv);
        fv_im.setOnTouchListener(imgListener);

        imgGroup = new ImageView[]{fi_im, se_im, th_im, fo_im, fv_im};
        imgLayout = (LinearLayout) findViewById(R.id.img_layout);
        logoutButton = (Button) findViewById(R.id.log_out);
        postButton = (Button) findViewById(R.id.send_post);
        messageEditText = (EditText) findViewById(R.id.edit_msg);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(LOGOUT_FORM);
            }
        });
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog switchDialog = new Dialog(MainActivity.this);
                switchDialog.setContentView(R.layout.switches);
                fS = (Switch) switchDialog.findViewById(R.id.facebook_switch);
                vS = (Switch) switchDialog.findViewById(R.id.vk_switch);
                tS = (Switch) switchDialog.findViewById(R.id.telegram_switch);
                oS = (Switch) switchDialog.findViewById(R.id.unknown_switch);
                //Getting switch states
                switch_prf = getSharedPreferences("test", Context.MODE_PRIVATE);
                facebook_switch_state = switch_prf.getBoolean("facebook_switch_state", false);
                vk_switch_state = switch_prf.getBoolean("vk_switch_state", false);
                telegram_switch_state = switch_prf.getBoolean("telegram_switch_state", false);
                unknown_switch_state = switch_prf.getBoolean("unknown_switch_state", false);
                //Setting checked
                fS.setChecked(facebook_switch_state);
                tS.setChecked(telegram_switch_state);
                oS.setChecked(unknown_switch_state);
                vS.setChecked(vk_switch_state);
                switchDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        editor = getSharedPreferences("test", Context.MODE_PRIVATE).edit();
                        editor.putBoolean("facebook_switch_state", fS.isChecked());
                        editor.putBoolean("vk_switch_state", vS.isChecked());
                        editor.putBoolean("telegram_switch_state", tS.isChecked());
                        editor.putBoolean("unknown_switch_state", oS.isChecked());
                        editor.apply();
                        switchDialog.cancel();
                    }
                });
                post = (Button) switchDialog.findViewById(R.id.post);
                post.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    if (vS.isChecked()) {
                                        System.out.println("VS");
                                        //vkAction();
                                    }
                                    if (tS.isChecked()) {
                                        System.out.println("TS");
                                        //tgAction();
                                    }
                                    if (fS.isChecked()) {
                                        System.out.println("FS");
                                        //fbAction();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    }
                });
                switchDialog.show();
            }
        });
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(SHOW_MESSENGERS_DIALOG);
            }
        });
        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uris.clear();
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                addImageLauncher.launch(intent);
            }
        });
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

}


