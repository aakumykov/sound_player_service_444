package com.github.aakumykov.sound_player_service_444;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import com.github.aakumykov.enum_utils.EnumUtils;
import com.github.aakumykov.sound_player_api.PlayerState;
import com.github.aakumykov.sound_player_api.SoundItem;
import com.github.aakumykov.sound_player_service_444.databinding.ActivityMainBinding;
import com.gitlab.aakumykov.exception_utils_module.ExceptionUtils;

public class PlayerInterfaceController {

    private final ActivityMainBinding mBinding;


    public PlayerInterfaceController(ActivityMainBinding binding) {
        mBinding = binding;
    }


    public void showPlayerState(PlayerState playerState) {
        switch (playerState) {
            case IDLE:
            case STOPPED:
            case ERROR:
                showIdle();
                hideTitle();
                break;

            case WAITING:
                showWaiting();
                break;

            case PLAYING:
            case RESUMED:
                showPlaying();
                break;

            case PAUSED:
                showPaused();
                break;

            default:
                EnumUtils.throwUnknownValue(playerState);
        }
    }

    public void showTitle(@Nullable SoundItem soundItem) {
        if (null == soundItem)
            mBinding.titleView.setText(R.string.no_track_name);
        else
            mBinding.titleView.setText(soundItem.getTitle());
    }

    private void hideTitle() {
        mBinding.titleView.setText("");
    }

    public void showError(Throwable error) {
        mBinding.errorView.setText(ExceptionUtils.getErrorMessage(error));
    }

    private void hideError() {
        mBinding.errorView.setText("");
    }



    private void showPaused() {
        changePlayPauseButton(R.drawable.ic_play_pause);
    }

    private void showPlaying() {
        changePlayPauseButton(R.drawable.ic_baseline_pause_24);
    }

    private void showIdle() {
        changePlayPauseButton(R.drawable.ic_baseline_play_arrow_24);
    }

    private void showWaiting() {
        changePlayPauseButton(R.drawable.ic_baseline_access_time_24);
    }

    private void changePlayPauseButton(@DrawableRes int drawableRes) {
        mBinding.playPauseButton.setImageResource(drawableRes);
    }


}
