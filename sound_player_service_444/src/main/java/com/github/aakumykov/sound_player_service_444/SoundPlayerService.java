package com.github.aakumykov.sound_player_service_444;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.aakumykov.custom_exo_player.CustomExoPlayer;
import com.github.aakumykov.sound_player_api.PlayerState;
import com.github.aakumykov.sound_player_api.SoundItem;
import com.github.aakumykov.sound_player_api.SoundPlayer;
import com.github.aakumykov.sound_player_api.SoundPlayerCallback;

public class SoundPlayerService extends Service implements SoundPlayerCallback {

    private static final String TAG = SoundPlayerService.class.getSimpleName();

    private final static String CHANNEL_ID = "sound_player_service_id";

    @Nullable
    private SoundPlayerServiceBinder mServiceBinder;

    private PlayerNotificationsController mPlayerNotificationsController;

    private SoundPlayer mSoundPlayer;


    // Статическия методы
    public static Intent intent(@NonNull Context context) {
        return new Intent(context, SoundPlayerService.class);
    }

    @Nullable
    public static SoundPlayerService getFromBinder(IBinder binder) {
        if (binder instanceof SoundPlayerServiceBinder)
            return ((SoundPlayerServiceBinder) binder).getSoundPlayerService();
        else
            return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        prepareNotificationsController();
        prepareSoundPlayer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        releaseSoundPlayer();
        releaseServiceBinder();
        releaseNotificationsController();
    }

    @Nullable @Override
    public IBinder onBind(Intent intent) {
        prepareServiceBinder();
        return mServiceBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        final String action = intent.getAction();

        if (null == action)
            return super.onStartCommand(intent, flags, startId);

        switch (action) {
            case SoundPlayerConstants.ACTION_PLAY:
                mSoundPlayer.resume();
                break;

            case SoundPlayerConstants.ACTION_PAUSE:
                mSoundPlayer.pause();
                break;

            case SoundPlayerConstants.ACTION_STOP:
                mSoundPlayer.stop();
                break;

            case SoundPlayerConstants.ACTION_SKIP_NEXT:
                mSoundPlayer.skipToNext();
                break;

            case SoundPlayerConstants.ACTION_SKIP_PREV:
                mSoundPlayer.skipToPrev();
                break;

            default:
                return super.onStartCommand(intent, flags, startId);
        }

        return START_NOT_STICKY;
    }


    public SoundPlayer getSoundPlayer() {
        return mSoundPlayer;
    }


    private void prepareServiceBinder() {
        if (null == mServiceBinder)
            mServiceBinder = new SoundPlayerServiceBinder(this);
    }

    private void prepareNotificationsController() {

        final PlayerActionFactory playerActionFactory = new PlayerActionFactory(this, getClass());

        mPlayerNotificationsController = new PlayerNotificationsController(
                this,
                CHANNEL_ID,
                getString(R.string.SOUND_PLAYER_SERVICE_notification_channel_name),
                getString(R.string.SOUND_PLAYER_SERVICE_notification_channel_description),
                playerActionFactory
        );
    }

    private void prepareSoundPlayer() {
        mSoundPlayer = new CustomExoPlayer(this);
        mSoundPlayer.addCallback(this);
    }


    private void releaseSoundPlayer() {
        mSoundPlayer.removeCallback(this);
        mSoundPlayer.release();
        mSoundPlayer = null;
    }

    private void releaseServiceBinder() {
        if (null != mServiceBinder)
            mServiceBinder.releaseSoundPlayerService();
        mServiceBinder = null;
    }

    private void releaseNotificationsController() {
        mPlayerNotificationsController.release();
    }


    // SoundPlayer.Callback
    @Override
    public void onPlayerStateChanged(@NonNull PlayerState playerState) {

        if (null == mSoundPlayer)
            return;

        final SoundItem soundItem = mSoundPlayer.getCurrentItem();
        final String title = (null != soundItem) ? soundItem.getTitle() : getString(R.string.no_title);

        switch (playerState) {
            case IDLE:
            case STOPPED:
                mPlayerNotificationsController.hidePlayerNotification();
                break;

            case PLAYING:
            case RESUMED:
                mPlayerNotificationsController.showPlayingNotification(title);
                break;

            case PAUSED:
                mPlayerNotificationsController.showPauseNotification(title);
                break;

            case WAITING:
                break;

            case ERROR:
                mPlayerNotificationsController.showErrorNotification(mSoundPlayer.getError());
                break;

            default:
                SoundPlayerCallback.super.onPlayerStateChanged(playerState);
        }
    }
}
