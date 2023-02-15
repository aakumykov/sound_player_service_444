package com.github.aakumykov.sound_player_service_444;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.github.aakumykov.sound_player_api.SoundItem;

import java.io.File;
import java.util.UUID;

public class SoundFile implements SoundItem {

    @NonNull private final String mId;
    @NonNull private final String mTitle;
    @NonNull private final Uri mFileUri;

    public SoundFile(@NonNull String id,
                     @NonNull String title,
                     @NonNull File file) throws NullPointerException
    {
        this(Uri.fromFile(file));
    }

    public SoundFile(@NonNull File file) throws NullPointerException {
        this(Uri.fromFile(file));
    }

    public SoundFile(@NonNull Uri fileUri) throws NullPointerException {
        this(UUID.randomUUID().toString(), uri2fileName(fileUri), fileUri);
    }

    private SoundFile(@NonNull String id,
                      @NonNull String title,
                      @NonNull Uri fileUri) throws NullPointerException
    {
        mId = id;
        mTitle = title;
        mFileUri = fileUri;
    }


    @Override @NonNull
    public String getId() {
        return mId;
    }

    @Override @NonNull
    public Uri getFileUri() {
        return mFileUri;
    }

    @Override
    public File getFile() {
        return null;
    }

    @NonNull
    public String getTitle() {
        return mTitle;
    }


    @NonNull @Override
    public String toString() {
        return "SoundFile{" +
                "mId='" + mId + '\'' +
                ", mTitle='" + mTitle + '\'' +
                ", mFileUri=" + mFileUri +
                '}';
    }


    private static String uri2fileName(Uri fileUri) {
        final String path = fileUri.getPath().trim();
        if ("".equals(path))
            return "";
        final String[] parts = path.split("/");
        return parts[parts.length-1];
    }
}
