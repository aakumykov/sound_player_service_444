package com.github.aakumykov.sound_player_service_444;

import android.Manifest;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.aakumykov.enum_utils.EnumUtils;
import com.github.aakumykov.sound_player_api.PlayerState;
import com.github.aakumykov.sound_player_api.SoundItem;
import com.github.aakumykov.sound_player_api.SoundPlayer;
import com.github.aakumykov.sound_player_api.SoundPlayerCallback;
import com.github.aakumykov.sound_player_service_444.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;


@RuntimePermissions
public class MainActivity extends AppCompatActivity implements ServiceConnection, SoundPlayerCallback {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ActivityMainBinding mBinding;
    @Nullable private SoundPlayer mSoundPlayer;
    private final AtomicBoolean mServiceIsRunning = new AtomicBoolean(false);
    private PlayerInterfaceController mPlayerInterfaceController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prepareLayout();
        prepareInterfaceController();
        startPlayerService();
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart()");
        super.onStart();
        bindPlayerService();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop()");
        super.onStop();
        unsetCallbacksFromPlayer();
        unbindPlayerService();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        Log.d(TAG, "onServiceConnected()");

        final SoundPlayerService soundPlayerService = SoundPlayerService.getFromBinder(binder);
        if (null != soundPlayerService) {
            mSoundPlayer = soundPlayerService.getSoundPlayer();
            mServiceIsRunning.set(true);
            updateStartStopPlayerButton();
        }

        setCallbacksToPlayer();

        if (null != mSoundPlayer)
            onPlayerStateChanged(mSoundPlayer.getCurrentState());
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(TAG, "onServiceDisconnected()");

        unsetCallbacksFromPlayer();

        mSoundPlayer = null;
        mServiceIsRunning.set(false);
        mPlayerInterfaceController.showPlayerState(PlayerState.IDLE);

        updateStartStopPlayerButton();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }


    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    void playAudioFromFolder() {

        final File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        File[] mp3files = downloadsDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".mp3");
            }
        });

        if (null != mp3files) {
            final List<SoundItem> soundItemList = new ArrayList<>();

            for (File mp3file : mp3files) soundItemList.add(new SoundFile(mp3file));

            if (null != mSoundPlayer)
                mSoundPlayer.play(soundItemList);
        }
        else
            Toast.makeText(this, "В каталоге '"+Environment.DIRECTORY_DOWNLOADS+"' нет mp3-файлов", Toast.LENGTH_SHORT).show();
    }


    private void prepareLayout() {
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        mBinding.playPauseButton.setOnClickListener(this::onPlayPauseButtonClicked);
        mBinding.stopButton.setOnClickListener(this::onStopButtonClicked);
        mBinding.skipPrevButton.setOnClickListener(this::onSkipPrevButtonClick);
        mBinding.skipNextButton.setOnClickListener(this::onSkipNextButtonClick);

        mBinding.startStopServiceButton.setOnClickListener(this::startStopService);
    }

    private void prepareInterfaceController() {
        mPlayerInterfaceController = new PlayerInterfaceController(mBinding);
    }


    private void onSkipNextButtonClick(View view) {
        if (null != mSoundPlayer)
            mSoundPlayer.skipToNext();
    }

    private void onPlayPauseButtonClicked(View view) {
        if (null == mSoundPlayer)
            return;

        if (mSoundPlayer.isPaused())
            mSoundPlayer.resume();
        else if (mSoundPlayer.isPlaying())
            mSoundPlayer.pause();
        else
            MainActivityPermissionsDispatcher.playAudioFromFolderWithPermissionCheck(this);
    }

    private void onSkipPrevButtonClick(View view) {
        if (null != mSoundPlayer)
            mSoundPlayer.skipToPrev();
    }

    private void onStopButtonClicked(View view) {
        if (null != mSoundPlayer)
            mSoundPlayer.stop();
    }


    private void startStopService(View view) {
        if (mServiceIsRunning.get())
            stopPlayerService();
        else {
            bindPlayerService();
            startPlayerService();
        }
    }

    private void startPlayerService() {
        startService(SoundPlayerService.intent(this));
    }

    private void stopPlayerService() {
        stopService(SoundPlayerService.intent(this));
    }


    private void bindPlayerService() {
        bindService(SoundPlayerService.intent(this), this, 0);
    }

    private void unbindPlayerService() {
        unbindService(this);
    }



    private void setCallbacksToPlayer() {
        if (null != mSoundPlayer)
            mSoundPlayer.addCallback(this);
    }

    private void unsetCallbacksFromPlayer() {
        if (null != mSoundPlayer)
            mSoundPlayer.removeCallback(this);
    }


    private void updateStartStopPlayerButton() {
        mBinding.startStopServiceButton.setText(
                mServiceIsRunning.get() ?
                        R.string.stop_service :
                        R.string.start_service
        );
    }


    // SoundPlayerCallback
    @Override
    public void onPlayerStateChanged(@NonNull PlayerState playerState) {

        if (null == mSoundPlayer)
            return;

        mPlayerInterfaceController.showPlayerState(playerState);

        switch (playerState) {
            case WAITING:
            case IDLE:
            case STOPPED:
                break;

            case PLAYING:
            case PAUSED:
            case RESUMED:
                mPlayerInterfaceController.showTitle(mSoundPlayer.getCurrentItem());
                break;

            case ERROR:
                mPlayerInterfaceController.showError(mSoundPlayer.getError());
                break;

            default:
                EnumUtils.throwUnknownValue(playerState);
        }
    }

}