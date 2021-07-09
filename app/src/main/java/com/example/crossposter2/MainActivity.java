package com.example.crossposter2;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.example.crossposter2.account.Account;
import com.example.crossposter2.account.Api;
import com.example.crossposter2.exceptions.KException;
import com.example.crossposter2.tg.DataFragment;
import com.example.crossposter2.tg.IsAppAvailable;
import com.example.crossposter2.vk.VkAuth;
import com.example.crossposter2.vk.VkConstant;

import org.json.JSONException;
import org.telegram.passport.PassportScope;
import org.telegram.passport.PassportScopeElementOne;
import org.telegram.passport.PassportScopeElementOneOfSeveral;
import org.telegram.passport.TelegramLoginButton;
import org.telegram.passport.TelegramPassport;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private final int SHOW_MESSENGERS_DIALOG = 2;
    private final int DIALOG_INPUT = 3;
    final String SAVED_TG_CHANNEL = "saved_tg_channel";
    SharedPreferences prf;
    EditText input;
    Button logoutButton;
    Button postButton;
    Button dialogButton;
    Button cre;
    EditText messageEditText;
    CheckBox vkBox, tgBox, fbBox;
    TextView crHeadliner, tgChannelHeadliner;
    Account account = new Account();
    FragmentManager fragmentManager;
    DataFragment dataFragment;
    ActivityResultLauncher<Intent> authorizeIntentLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        account.access_token = data.getStringExtra("token");
                        System.out.println(account.access_token);
                        account.user_id = data.getLongExtra("user_id", 0);
                        account.save(MainActivity.this);
                        api = new Api(account.access_token, VkConstant.AppId);
                        showButtons();
                    }
                }
            });
    Api api;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        account.restore(this);

        if (account.access_token != null)
            api = new Api(account.access_token, VkConstant.AppId);
        createUI();
        showButtons();
    }

    public void createUI() {
        dialogButton = (Button) findViewById(R.id.dialog_button);
        logoutButton = (Button) findViewById(R.id.log_out);
        postButton = (Button) findViewById(R.id.send_post);
        messageEditText = (EditText) findViewById(R.id.edit_msg);
        crHeadliner = (TextView) findViewById(R.id.cr_headliner);
        fbBox = (CheckBox) findViewById(R.id.cr_fb_box);
        vkBox = (CheckBox) findViewById(R.id.cr_vk_box);
        tgBox = (CheckBox) findViewById(R.id.cr_tg_box);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                api = null;
                account.access_token = null;
                account.user_id = 0;
                account.save(MainActivity.this);
                showButtons();
            }
        });
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            if (vkBox.isActivated()) {
                                vkAction();
                            }
                            if (tgBox.isActivated()) {
                                tgAction();
                            }
                            if (fbBox.isActivated()) {

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(SHOW_MESSENGERS_DIALOG);
            }
        });
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
        if (id == DIALOG_INPUT) {
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            input = new EditText(this);
            input.setHint("Type here..."); prf = getPreferences(MODE_PRIVATE);
            String savedText = prf.getString(SAVED_TG_CHANNEL, "");
            input.setText(savedText);
            Toast.makeText(this, "Text loaded", Toast.LENGTH_SHORT).show();
            alertDialog.setTitle(R.string.tg_dialog_title);
            alertDialog.setMessage(R.string.tg_dialog_example);
            alertDialog.setView(input);
            alertDialog.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    prf = getPreferences(MODE_PRIVATE);
                    SharedPreferences.Editor ed = prf.edit();
                    ed.putString(SAVED_TG_CHANNEL, input.getText().toString());
                    ed.apply();
                    Toast.makeText(getApplicationContext(), "Channel name saved", Toast.LENGTH_SHORT).show();
                }
            });
            alertDialog.create();
            alertDialog.show();
        }

        return null;
    }

    public void onVkClick() {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), VkAuth.class);
        authorizeIntentLauncher.launch(intent);
    }

    public void vkAction() throws JSONException, IOException, KException {
        String text = messageEditText.getText().toString();
        api.createWallPost(account.user_id, text, null, null, false, false, false, null, null, null, 0L, null, null, 5.131);
        //Показать сообщение в UI потоке
        runOnUiThread(successRunnable);
    }

    public void onTgClick() {
       showDialog(DIALOG_INPUT);
    }

    public void tgAction() {
        final String appName = "org.telegram.messenger";
        final boolean isAppInstalled = IsAppAvailable.isAppAvailable(this.getApplicationContext(), appName);
        if (isAppInstalled) {
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("tg://resolve?domain=partsilicon"));
            startActivity(intent);
        } else {
            Toast.makeText(this, "Telegram not Installed", Toast.LENGTH_SHORT).show();
        }
    }


    public void onFbClick() {

    }

    public void fbAction() {
    }

    Runnable successRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(getApplicationContext(), "Запись успешно добавлена", Toast.LENGTH_LONG).show();
        }
    };

    void showButtons() {
        if (api != null) {
            dialogButton.setVisibility(View.VISIBLE);
            dialogButton.setText("Connect more accounts");
            logoutButton.setVisibility(View.VISIBLE);
            postButton.setVisibility(View.VISIBLE);
            messageEditText.setVisibility(View.VISIBLE);
            fbBox.setVisibility(View.VISIBLE);
            vkBox.setVisibility(View.VISIBLE);
            tgBox.setVisibility(View.VISIBLE);
            crHeadliner.setVisibility(View.VISIBLE);
        } else {
            dialogButton.setVisibility(View.VISIBLE);
            fbBox.setVisibility(View.GONE);
            vkBox.setVisibility(View.GONE);
            tgBox.setVisibility(View.GONE);
            crHeadliner.setVisibility(View.GONE);
            logoutButton.setVisibility(View.GONE);
            postButton.setVisibility(View.GONE);
            messageEditText.setVisibility(View.GONE);
        }
    }

}


