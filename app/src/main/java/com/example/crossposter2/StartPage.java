package com.example.crossposter2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.crossposter2.account.Api;
import com.example.crossposter2.vk.VkAuth;
import com.example.crossposter2.vk.VkConstant;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class StartPage extends AppCompatActivity {
    private Button fb_button, vk_button;
    private String id, name, email, gender, birthday;
    private CallbackManager callbackManager;
    private LoginButton fb_hidden_button;
    public static String redirect_url="https://oauth.vk.com/blank.html";
    private static String API_VERSION="5.5";

    ActivityResultLauncher<Intent> authorizeVkIntentLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Intent intent = new Intent(StartPage.this, MainActivity.class);
                        intent.putExtra("access_token_vk", data.getStringExtra("token"));
                        intent.putExtra("user_id_vk", data.getLongExtra("user_id", 0));
                        startActivity(intent);
                    }
                }
            });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callbackManager = CallbackManager.Factory.create();
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        setContentView(R.layout.start_page);
        fbContentCreate();
        vkContentCreate();

    }
    private void fbContentCreate() {
        fb_button = (Button) findViewById(R.id.fb);
        fb_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fb_hidden_button.performClick();
            }
        });
        fb_hidden_button = (LoginButton) findViewById(R.id.fb_login_button);
        List< String > permissionNeeds = Arrays.asList("user_photos", "email",
                "user_birthday", "public_profile", "AccessToken");
        fb_hidden_button.registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {@Override
                public void onSuccess(LoginResult loginResult) {

                    System.out.println("onSuccess");

                    String accessToken = loginResult.getAccessToken()
                            .getToken();
                    Log.i("accessToken", accessToken);

                    GraphRequest request = GraphRequest.newMeRequest(
                            loginResult.getAccessToken(),
                            new GraphRequest.GraphJSONObjectCallback() {@Override
                            public void onCompleted(JSONObject object,
                                                    GraphResponse response) {

                                Log.i("LoginActivity",
                                        response.toString());
                                try {
                                    id = object.getString("id");
                                    try {
                                        URL profile_pic = new URL(
                                                "http://graph.facebook.com/" + id + "/picture?type=large");
                                        Log.i("profile_pic",
                                                profile_pic + "");

                                    } catch (MalformedURLException e) {
                                        e.printStackTrace();
                                    }
                                    name = object.getString("name");
                                    email = object.getString("email");
                                    gender = object.getString("gender");
                                    birthday = object.getString("birthday");
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

                    Intent intent = new Intent(StartPage.this, MainActivity.class);
                    String token = loginResult.getAccessToken().getToken();
                    intent.putExtra("access_token_fb", token);
                    intent.putExtra("user_id_fb", id);
                    startActivity(intent);
                }

                    @Override
                    public void onCancel() {
                        System.out.println("onCancel");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                    exception.printStackTrace();
                        System.out.println("onError");
                        Log.v("StartPage", exception.getCause().toString());
                    }
                });
    }
    private void vkContentCreate() {
        vk_button = (Button) findViewById(R.id.startPageVkButton);
        vk_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), VkAuth.class);
                authorizeVkIntentLauncher.launch(intent);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int responseCode,
                                    Intent data) {
        super.onActivityResult(requestCode, responseCode, data);
        callbackManager.onActivityResult(requestCode, responseCode, data);
    }

}
