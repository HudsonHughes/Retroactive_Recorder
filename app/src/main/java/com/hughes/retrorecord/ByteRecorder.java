package com.hughes.retrorecord;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import com.hughes.retrorecord.technology.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class ByteRecorder extends Service {
    Context context;
    private NotificationManager mNM;
    private int NOTIFICATION = 1598;
    private PcmAudioRecorder mRecorder;

    @Override
    public void onCreate() {
        context = this;
        mNM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mRecorder = PcmAudioRecorder.getInstanse(context);
        mRecorder.setOutputFile(new File(context.getFilesDir() + "/buffer.raw").getAbsolutePath());

        if (PcmAudioRecorder.State.INITIALIZING == mRecorder.getState()) {
            mRecorder.prepare();
            mRecorder.start();
        } else if (PcmAudioRecorder.State.ERROR == mRecorder.getState()) {
            mRecorder.release();
            mRecorder = PcmAudioRecorder.getInstanse(context);
            mRecorder.setOutputFile(new File(context.getFilesDir() + "/buffer.raw").getAbsolutePath());
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
    public void onDestroy() {
        if (null != mRecorder) {
            mRecorder.release();
        }
        cancelNotification();
//        try {
//            for (File file : new File(getApplicationContext().getFilesDir() + "/magicMic/").listFiles()) {
//                file.delete();
//            }
//        }catch (Exception e){
//
//        }

    }

    public static void copyFile(File src, File dst) throws IOException
    {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try
        {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        }
        finally
        {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
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
