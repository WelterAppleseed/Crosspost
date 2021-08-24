package com.example.crossposter2.vk;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.zxing.common.StringUtils;
import com.vk.api.sdk.VKApiManager;
import com.vk.api.sdk.VKApiResponseParser;
import com.vk.api.sdk.VKHttpPostCall;
import com.vk.api.sdk.VKMethodCall;
import com.vk.api.sdk.exceptions.VKApiException;
import com.vk.api.sdk.exceptions.VKApiIllegalResponseException;
import com.vk.api.sdk.internal.ApiCommand;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class VKWallPostCommand extends ApiCommand<Integer> {
    private final String message;
    private final List<Uri> photos;
    private final int ownerId;
    private final boolean friendsOnly;
    private final boolean fromGroup;
    ArrayList<String> attachments = new ArrayList<>();
    public static final int RETRY_COUNT = 3;

    public VKWallPostCommand(String message, List<Uri> photos, Integer ownerId, boolean friendsOnly, boolean fromGroup) {
        this.message = message;
        this.photos = photos;
        this.ownerId = ownerId;
        this.friendsOnly = friendsOnly;
        this.fromGroup = fromGroup;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected Integer onExecute(VKApiManager manager) throws InterruptedException, VKApiException, IOException {
        VKMethodCall.Builder callBuilder = new VKMethodCall.Builder();
        callBuilder
                .method("wall.post")
                .args("friends_only", this.friendsOnly ? 1 : 0)
                .args("from_group", this.fromGroup ? 1 : 0)
                .version(manager.getConfig().getVersion());

        if (this.message != null) {
            callBuilder.args("message", message);
        }

        if (this.ownerId != 0) {
            callBuilder.args("owner_id", this.ownerId);
        }
        if (photos!= null) {
            VKServerUploadInfo uploadInfo = getServerUploadInfo(manager);
            for (Uri uri: photos) {
                attachments.add(uploadPhoto(uri, uploadInfo, manager));
            }
            callBuilder.args("attachments", String.join(",", attachments));
        }
        return (Integer) manager.execute(callBuilder.build(), new ResponseApiParser());
    }

    private final VKServerUploadInfo getServerUploadInfo(VKApiManager manager) throws InterruptedException, VKApiException, IOException {
        VKMethodCall uploadInfoCall = new VKMethodCall.Builder()
                .method("photos.getWallUploadServer")
                .version(manager.getConfig().getVersion())
                .build();
        return (VKServerUploadInfo) manager.execute(uploadInfoCall, new ServerUploadInfoParser());
    }

    private final String uploadPhoto(Uri uri, VKServerUploadInfo serverUploadInfo, VKApiManager manager) throws InterruptedException, VKApiException, IOException {
        System.out.println(uri + "!!!!!!!!!!!!!!");
        VKHttpPostCall fileUploadCall = new VKHttpPostCall.Builder()
                .url(serverUploadInfo.getUploadUrl())
                .args("photo", uri, "image.jpg")
                .timeout(TimeUnit.MINUTES.toMillis(5))
                .retryCount(RETRY_COUNT)
                .build();
        VKFileUploadInfo fileUploadInfo = (VKFileUploadInfo) manager.execute(fileUploadCall, null, new FileUploadInfoParser());

        VKMethodCall saveCall = new VKMethodCall.Builder()
                .method("photos.saveWallPhoto")
                .args("server", fileUploadInfo.getServer())
                .args("photo", fileUploadInfo.getPhoto())
                .args("hash", fileUploadInfo.getHash())
                .version(manager.getConfig().getVersion())
                .build();
        VKSaveInfo saveInfo = (VKSaveInfo) manager.execute(saveCall, new SaveInfoParser());

        return saveInfo.getAttachment();
    }

    private static final class ResponseApiParser implements VKApiResponseParser {
        ResponseApiParser() {
        }

        public Integer parse(String response) {
            try {
                return (new JSONObject(response)).getJSONObject("response").getInt("post_id");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private static final class ServerUploadInfoParser implements VKApiResponseParser {
        ServerUploadInfoParser() {
        }

        public VKServerUploadInfo parse(String response) {
            try {
                JSONObject joResponse = (new JSONObject(response)).getJSONObject("response");
                return new VKServerUploadInfo(joResponse.getString("upload_url"), joResponse.getInt("album_id"), joResponse.getInt("user_id"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private static final class FileUploadInfoParser implements VKApiResponseParser {
        public FileUploadInfoParser() {
        }

        public VKFileUploadInfo parse(String response) {
            try {
                JSONObject joResponse = new JSONObject(response);
                return new VKFileUploadInfo(joResponse.getString("server"), joResponse.getString("photo"), joResponse.getString("hash"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    private static final class SaveInfoParser implements VKApiResponseParser {
        SaveInfoParser() {
        }

        public VKSaveInfo parse(String response) {
            try {
                JSONObject joResponse = (new JSONObject(response)).getJSONArray("response").getJSONObject(0);
                return new VKSaveInfo(joResponse.getInt("id"), joResponse.getInt("album_id"), joResponse.getInt("owner_id"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    public static final class Companion {
        private Companion() {
        }
    }
}
