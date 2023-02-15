package com.github.aakumykov.sound_player_service_444;

public class SoundPlayerServiceBinder extends android.os.Binder {

    private SoundPlayerService mSoundPlayerService;

    public SoundPlayerServiceBinder(SoundPlayerService soundPlayerService) {
        mSoundPlayerService = soundPlayerService;
    }

    public SoundPlayerService getSoundPlayerService() {
        return mSoundPlayerService;
    }

    public void releaseSoundPlayerService() {
        mSoundPlayerService = null;
    }
}
