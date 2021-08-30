package com.example.crossposter2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.crossposter2.utils.ClickListeners;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.vk.api.sdk.VK;
import com.vk.api.sdk.auth.VKAccessToken;
import com.vk.api.sdk.auth.VKAuthCallback;
import com.vk.api.sdk.auth.VKScope;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class StartPage extends AppCompatActivity {
    private Button fb_button, vk_button, inst_button;
    private static String id, name, email, gender, birthday, page_access_token, page_id;
    private ImageButton reportButton;
    private CallbackManager callbackManager;
    private static AccessToken access;
    private LoginButton hidden_button, hidden_button_inst;
    public static String redirect_url = "https://oauth.vk.com/blank.html";
    private static String API_VERSION = "5.5";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callbackManager = CallbackManager.Factory.create();
        FacebookSdk.sdkInitialize(this);
        setContentView(R.layout.start_page);
        fbContentCreate();
        vkContentCreate();

        reportButton = (ImageButton) findViewById(R.id.warning_button);
        reportButton.setOnClickListener(new ClickListeners().onReportClickListener(StartPage.this));
    }
    private void fbContentCreate() {
        fb_button = (Button) findViewById(R.id.fb);
        fb_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidden_button.performClick();
            }
        });
        hidden_button = (LoginButton) findViewById(R.id.fb_login_button);
        List<String> permissionNeeds = Arrays.asList("email", "pages_show_list");
        //fb_hidden_button.setPermissions(permissionNeeds);
        hidden_button.registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {

                        System.out.println("onSuccess");

                        String accessToken = loginResult.getAccessToken()
                                .getToken();
                        Log.i("accessToken", accessToken);
                        access = loginResult.getAccessToken();

                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject object,
                                                            GraphResponse response) {

                                        Log.i("LoginActivity",
                                                response.toString());
                                        try {
                                            id = object.getString("id");
                                    /*final String client_id = getResources().getString(R.string.APP_ID);
                                    final String secret = getResources().getString(R.string.SECRET_ID);
                                    new GraphRequest(
                                            access,
                                            "oauth/access_token?grant_type=fb_exchange_token&client_id=" + client_id + "&client_secret=" + secret + "&fb_exchange_token=" + accessToken,
                                            null,
                                            HttpMethod.GET,
                                            new GraphRequest.Callback() {
                                                public void onCompleted(GraphResponse response) {
                                                   JSONObject secondObject = response.getJSONObject();
                                                    try {
                                                        String long_lived_access_token = secondObject.getString("access_token");
                                                        Log.i("long_lived_token", long_lived_access_token);
                                                        new GraphRequest(access,

                                                                        id +"/fields=access_token&" +
                                                                        "access_token=" + long_lived_access_token, null, HttpMethod.GET, new GraphRequest.Callback() {
                                                            @Override
                                                            public void onCompleted(@NotNull GraphResponse graphResponse) {
                                                                Log.i("final response", graphResponse.toString());
                                                            }
                                                        });
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                    ).executeAsync();*/
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
                                "id,name,email");
                        request.setParameters(parameters);
                        request.executeAsync();

                        Intent intent = new Intent(StartPage.this, MainActivity.class);
                        String token = loginResult.getAccessToken().getToken();
                        intent.putExtra("access_token_fb", token);
                        intent.putExtra("user_id_fb", id);
                        startActivity(intent);
                        finish();
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
                VK.login(StartPage.this, Arrays.asList(VKScope.WALL, VKScope.PHOTOS));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode,
                                    Intent data) {
        callbackManager.onActivityResult(requestCode, responseCode, data);
        VKAuthCallback callback = new VKAuthCallback() {
            @Override
            public void onLogin(@NotNull VKAccessToken vkAccessToken) {
                Intent intent = new Intent(StartPage.this, MainActivity.class);
                System.out.println("log");
                startActivity(intent);
            }

            @Override
            public void onLoginFailed(int i) {
                System.out.println("fsdfsdfa");
            }
        };
        if (data == null || !VK.onActivityResult(requestCode, responseCode, data, callback)) {
            super.onActivityResult(requestCode, responseCode, data);
        }
    }

}
