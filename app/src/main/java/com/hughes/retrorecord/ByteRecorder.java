package com.hughes.retrorecord;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.hughes.retrorecord.messages.ShutdownEvent;
import com.hughes.retrorecord.messages.SongEvent;
import com.hughes.retrorecord.recording.*;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class ByteRecorder extends IntentService {
    Context context;
    private NotificationManager mNM;
    private int NOTIFICATION = 1598;
    private PcmAudioRecorder mRecorder;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ShutdownEvent shutdownEvent) {

    };

    public ByteRecorder(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        context = this;
        mNM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mRecorder = PcmAudioRecorder.getInstance(context);

        if (PcmAudioRecorder.State.INITIALIZING == mRecorder.getState()) {
            mRecorder.prepare();
            mRecorder.start();
        } else if (PcmAudioRecorder.State.ERROR == mRecorder.getState()) {
            mRecorder.release();
            mRecorder = PcmAudioRecorder.getInstance(context);
        } else {
            mRecorder.stop();
            mRecorder.reset(context);
        }
        showNotification();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job


        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }

    @Override
    public void onDestroy() {
        if (null != mRecorder) {
            mRecorder.release();
        }
        cancelNotification();


    }
    private void showNotification() {
        Intent intent = new Intent(context, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                NOTIFICATION, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(context)
                .setContentTitle("Retroactive Recorder")
                .setContentText("Currently Recording")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                ;
        Notification n;

        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            n = builder.build();
        } else {
            n = builder.getNotification();
        }

        n.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        mNM.notify(NOTIFICATION, n);
    }
    public void cancelNotification(){
        mNM.cancel(NOTIFICATION);
    }
}
