package com.github.aakumykov.sound_player_service_444;

import android.app.Notification;
import android.app.Service;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.gitlab.aakumykov.exception_utils_module.ExceptionUtils;

class PlayerNotificationsController {

    private final String mChannelId;
    private final CharSequence mChannelName;
    private final String mChannelDescription;
    private final PlayerActionFactory mPlayerActionFactory;
    private Context mContext;
    private Service mService;
    private NotificationManagerCompat mNotificationManagerCompat;

    // TODO: передавать NotificationManager?
    PlayerNotificationsController(Service service,
                                         String channelId,
                                         CharSequence channelName,
                                         String channelDescription,
                                         PlayerActionFactory playerActionFactory) {
        mContext = service;
        mService = service;
        mChannelId = channelId;
        mChannelName = channelName;
        mChannelDescription = channelDescription;
        mPlayerActionFactory = playerActionFactory;

        prepare();
    }


    public void showPlayingNotification(String title) {

        final Notification notification = notificationBuilder()
                .setSmallIcon(R.drawable.ic_play)
                .setContentText(title)
                .setSubText(string(R.string.state_playing))
                .addAction(mPlayerActionFactory.skipPrevAction())
                .addAction(mPlayerActionFactory.pauseAction())
                .addAction(mPlayerActionFactory.skipNextAction())
                .addAction(mPlayerActionFactory.stopAction())
                .setStyle(
                        new androidx.media.app.NotificationCompat.MediaStyle()
                                .setShowActionsInCompactView(0,1,2)
                )
                .build();

        showPersistentNotification(notification);
    }

    public void showPauseNotification(String title) {
        final Notification notification = notificationBuilder()
                .setSmallIcon(R.drawable.ic_pause)
                .setContentText(title)
                .setSubText(string(R.string.state_pause))
                .addAction(mPlayerActionFactory.skipPrevAction())
                .addAction(mPlayerActionFactory.playAction())
                .addAction(mPlayerActionFactory.skipNextAction())
                .addAction(mPlayerActionFactory.stopAction())
                .setStyle(
                        new androidx.media.app.NotificationCompat.MediaStyle()
                                .setShowActionsInCompactView(0,1,2)
                )
                .build();

        showPersistentNotification(notification);
    }

    public void showErrorNotification(Throwable throwable) {
        final Notification errorNotification = notificationBuilder()
                .setSmallIcon(R.drawable.ic_error)
                .setContentTitle(string(R.string.error))
                .setContentText(ExceptionUtils.getErrorMessage(throwable))
                .setStyle(new NotificationCompat.BigTextStyle())
                .build();

        mNotificationManagerCompat.notify(R.id.sound_player_service_notification, errorNotification);
    }

    public void hidePlayerNotification() {
        mService.stopForeground(true);
    }

    public void release() {
        mService = null;
        mContext = null;
        mNotificationManagerCompat = null;
    }



    private void prepare() {
        mNotificationManagerCompat = NotificationManagerCompat.from(mContext);
        mNotificationManagerCompat.createNotificationChannel(notificationChannel());
    }

    private NotificationChannelCompat notificationChannel() {
        return new NotificationChannelCompat.Builder(mChannelId, NotificationManagerCompat.IMPORTANCE_LOW)
                .setName(mChannelName)
                .setDescription(mChannelDescription)
                .build();
    }

    private NotificationCompat.Builder notificationBuilder() {
        return new NotificationCompat.Builder(mContext, mChannelId);
    }

    private String string(@StringRes int stringRes) {
        return mContext.getString(stringRes);
    }

    private void showPersistentNotification(@NonNull Notification notification) {
        mService.startForeground(R.id.sound_player_service_notification, notification);
    }
}
