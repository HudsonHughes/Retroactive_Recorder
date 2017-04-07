package com.hughes.retrorecord.recording;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import com.hughes.retrorecord.ByteRecorder;
import com.hughes.retrorecord.MainApplication;
import com.hughes.retrorecord.messages.RefreshEvent;
import com.hughes.retrorecord.technology.Log;
import com.hughes.retrorecord.technology.WavAudioFormat;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SaveAudio extends AsyncTask<Void, Void, String> {
    private ProgressDialog dialog;
    int sampleRate;
    File wavTarget;
    WavAudioFormat af;
    int amount;
    Context context;
    Activity activity;
    long prog = 0;
    int loop = 1000;
    int all = 0;
    MainApplication mainApplication;
    boolean alreadyRunning = false;

    public SaveAudio(final Activity activity) {
        this.context = activity.getApplicationContext();
        this.activity = activity;
        mainApplication = new MainApplication(context.getApplicationContext());
        WavAudioFormat.Builder wavAudioFormatBuilder = new WavAudioFormat.Builder();
        int extra = 0;
        wavTarget = new File(mainApplication.generateStamp(extra));
        while(wavTarget.exists()){
            extra += 1;
            wavTarget = new File(mainApplication.generateStamp(extra));
        }
        this.amount = mainApplication.getAMOUNT();
        this.sampleRate = mainApplication.getSAMPLERATE();
        this.af = wavAudioFormatBuilder.channels(1).sampleRate(sampleRate).sampleSizeInBits(16).build();
        dialog = new ProgressDialog(activity);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setMax(100);
        dialog.setCancelable(false);
    }

    @Override
    protected void onPreExecute() {
        alreadyRunning = mainApplication.isServiceRunning();
        if(alreadyRunning) {
            Intent intent = new Intent(context, ByteRecorder.class);
            new MainApplication(context).setStartOnBoot(false);
            context.stopService(intent);
        }
        dialog.setMessage("Compiling wav file...");
        dialog.show();
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
        dialog.setProgress((int) prog);
        dialog.setMax(all);
    }
    @Override
    protected void onPostExecute(String result) {
        if(alreadyRunning){
            Intent intent = new Intent(context, ByteRecorder.class);
            new MainApplication(context).setStartOnBoot(true);
            context.startService(intent);
        }
        if(result.equals("OUTOFSPACE")) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "Save failed. Make sure there is enough space available.", Toast.LENGTH_LONG).show();
                }
            });
        }
        if(result.equals("EMPTY")) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "The buffer is empty. There is nothing to save.", Toast.LENGTH_SHORT).show();
                }
            });
        }
        if(result.equals("SUCCESS")) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "Saved successfully", Toast.LENGTH_SHORT).show();
                }
            });
        }
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    @Override
    protected String doInBackground(Void... params) {
        if (BytesToFile.getInstance(context).getLengthOfHash() == 0) {
            return "EMPTY";
        }
        final BytesToFile bytesToFile = BytesToFile.getInstance(context);
        if(bytesToFile.convertLengthToSeconds(bytesToFile.getLengthOfHash()) < mainApplication.getAMOUNT() * 60){

        }
        try {
            Log.d("Hudson Hughes", "Making file");
            if (!new File(BytesToFile.getInstance(context).magicPath()).exists()) {
                new File(BytesToFile.getInstance(context).magicPath()).mkdir();
                return "EMPTY";
            }
            File bufferFile = new File(BytesToFile.getInstance(context).magicPath() + "pcmbuffer.raw");
            bufferFile.delete();
            File folder = new File(BytesToFile.getInstance(context).magicPath());
            float samp = 41000;
            long progress = 0;
            long raw_length = 0;
            if (folder.listFiles() != null) {
                List<File> pcms = Arrays.asList(folder.listFiles());
                for (File file : pcms) {
                    raw_length += file.length();
                }
                float div = sampleRate / samp;
                Log.d("Hudson Hughes", "Sampling Size: " + String.valueOf(sampleRate));
                Log.d("Hudson Hughes", "Stock Sampling Size: " + String.valueOf(samp));
                long needed_length = Math.round(mainApplication.rateToByte(sampleRate) * amount);
                long start_position = raw_length - needed_length;
                if (start_position < 0){
                    start_position = 0;

                }
                Log.d("Hudson Hughes", "Buffer File Size: " + String.valueOf(raw_length));
                Log.d("Hudson Hughes", "Start Position: " + String.valueOf(start_position));
                Log.d("Hudson Hughes", "Needed Length: " + String.valueOf(needed_length));
                Log.d("Hudson Hughes", "div: " + String.valueOf(div));
                Log.d("Hudson Hughes", "Start Time " + String.valueOf(System.currentTimeMillis() / 10000));
                Collections.sort(pcms);
                for (File file : pcms) {
                    if (file.isFile()) {
                        Log.d("Hudson Hughes", "writing " + file.getName());
                        InputStream in = new FileInputStream
                                (file);
                        OutputStream out = new FileOutputStream
                                (bufferFile, true);
                        try{
                            byte[] buf = new byte[1024];
                            int len;
                            while ((len = in.read(buf)) > 0) {
                                progress = progress + len;
                                if (progress > start_position) {
                                    //Log.d("Hudson", "Length: " + String.valueOf(len) + " " + String.valueOf(progress));
                                    out.write(buf, 0, len);
                                }
                                if(loop > 100){
                                    prog = Math.round( (double)100 * (double) progress / (double) raw_length );
                                    prog = progress;
                                    int raw_len = 0;
                                    for (File filse : pcms) {
                                        raw_len += filse.length();
                                    }
                                    all = raw_len;
                                    publishProgress();
                                    loop = 0;
                                }
                                loop ++;
                            }
                            in.close();
                            out.close();
                    } catch (IOException e) {
                        if(in != null)
                            in.close();
                        if(out != null)
                            out.close();
                        return "OUTOFSPACE";
                    }
                    }
                }
                ConvertRawToWav.rawToWave(bufferFile, wavTarget, sampleRate);
//                new MediaScannerWrapper(context, wavTarget.getPath(), "audio/*").scan();
//                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + wavTarget.getPath())));

                MediaScannerConnection.scanFile(context, new String[]{wavTarget.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        // TODO Auto-generated method stub

                    }
                });

            }else{
                return "EMPTY";
            }
        } catch (IOException e) {
            return "OUTOFSPACE";
        }
        EventBus.getDefault().post(new RefreshEvent());
        return "SUCCESS";
    }

    public class MediaScannerWrapper implements MediaScannerConnection.MediaScannerConnectionClient {

        private MediaScannerConnection mConnection;
        private String mPath;
        private String mMimeType;

        public MediaScannerWrapper(Context ctx, String filePath, String mime){
            mPath = filePath;
            mMimeType = mime;
            mConnection = new MediaScannerConnection(ctx, this);
        }

        public void scan(){
            mConnection.connect();
        }

        @Override
        public void onMediaScannerConnected() {
            mConnection.scanFile(mPath, mMimeType);
            Log.d(getClass().getName(), "Media file scanned: "+mPath);
        }

        @Override
        public void onScanCompleted(String arg0, Uri arg1) {
        }

    }

}