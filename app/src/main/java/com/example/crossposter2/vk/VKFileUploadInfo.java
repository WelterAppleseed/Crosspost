package com.example.crossposter2.vk;

public final class VKFileUploadInfo {
    private final String server;
    private final String photo;
    private final String hash;

    public VKFileUploadInfo(String server, String photo, String hash) {
        this.server = server;
        this.photo = photo;
        this.hash = hash;
    }
    public final String getServer() {
        return this.server;
    }

    public final String getPhoto() {
        return this.photo;
    }

    public final String getHash() {
        return this.hash;
    }

}