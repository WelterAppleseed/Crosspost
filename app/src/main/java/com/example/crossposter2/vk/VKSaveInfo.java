package com.example.crossposter2.vk;

public class VKSaveInfo {
    private final int id;
    private final int albumId;
    private final int ownerId;

    public final String getAttachment() {
        return "photo" + this.ownerId + '_' + this.id;
    }

    public final int getId() {
        return this.id;
    }

    public final int getAlbumId() {
        return this.albumId;
    }

    public final int getOwnerId() {
        return this.ownerId;
    }

    public VKSaveInfo(int id, int albumId, int ownerId) {
        this.id = id;
        this.albumId = albumId;
        this.ownerId = ownerId;
    }
}
