package com.github.aakumykov.sound_player_service_444;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationCompat;

class PlayerActionFactory {

    private final Context mContext;
    private final Class<? extends Service> mPendingIntentClass;

    PlayerActionFactory(Context context, Class<? extends Service> pendingIntentClass) {
        mContext = context;
        mPendingIntentClass = pendingIntentClass;
    }


    public NotificationCompat.Action pauseAction() {
        return new NotificationCompat.Action.Builder(
                R.drawable.ic_pause, string(R.string.action_pause),
                pausePendingIntent()
        ).build();
    }

    public NotificationCompat.Action stopAction() {
        return new NotificationCompat.Action.Builder(
                R.drawable.ic_stop, string(R.string.action_stop),
                stopPendingIntent()
        ).build();
    }

    public NotificationCompat.Action playAction() {
        return new NotificationCompat.Action.Builder(
                R.drawable.ic_play, string(R.string.action_play),
                playPendingIntent()
        ).build();
    }

    public NotificationCompat.Action skipNextAction() {
        return new NotificationCompat.Action.Builder(
                R.drawable.ic_skip_next, string(R.string.action_skip_next),
                skipNextPendingIntent()
        ).build();
    }

    public NotificationCompat.Action skipPrevAction() {
        return new NotificationCompat.Action.Builder(
                R.drawable.ic_skip_prev, string(R.string.action_skip_prev),
                skipPrevPendingIntent()
        ).build();
    }


    private PendingIntent pausePendingIntent() {
        return pendingIntentWithAction(SoundPlayerConstants.ACTION_PAUSE, SoundPlayerConstants.CODE_PAUSE);
    }

    private PendingIntent stopPendingIntent() {
        return pendingIntentWithAction(SoundPlayerConstants.ACTION_STOP, SoundPlayerConstants.CODE_STOP);
    }

    private PendingIntent playPendingIntent() {
        return pendingIntentWithAction(SoundPlayerConstants.ACTION_PLAY, SoundPlayerConstants.CODE_PLAY);
    }

    private PendingIntent skipNextPendingIntent() {
        return pendingIntentWithAction(SoundPlayerConstants.ACTION_SKIP_NEXT, SoundPlayerConstants.CODE_SKIP_NEXT);
    }

    private PendingIntent skipPrevPendingIntent() {
        return pendingIntentWithAction(SoundPlayerConstants.ACTION_SKIP_PREV, SoundPlayerConstants.CODE_SKIP_PREV);
    }


    private PendingIntent pendingIntentWithAction(@NonNull final String action, final int requestCode) {
        final Intent intent = new Intent(mContext, mPendingIntentClass);
        intent.setAction(action);
        return PendingIntent.getService(mContext, requestCode, intent, pendingIntentOneShotFlag());
    }


    private int pendingIntentOneShotFlag() {
        return (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) ?
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT : PendingIntent.FLAG_ONE_SHOT;
    }

    private String string(@StringRes int stringRes) {
        return mContext.getString(stringRes);
    }
}
