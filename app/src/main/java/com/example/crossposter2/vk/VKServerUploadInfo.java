package com.example.crossposter2.vk;

public class VKServerUploadInfo {

    private final String uploadUrl;
    private final int albumId;
    private final int userId;
    public final String getUploadUrl() {
        return this.uploadUrl;
    }

    public final int getAlbumId() {
        return this.albumId;
    }

    public final int getUserId() {
        return this.userId;
    }

    public VKServerUploadInfo(String uploadUrl, int albumId, int userId) {
        this.uploadUrl = uploadUrl;
        this.albumId = albumId;
        this.userId = userId;
    }
}
