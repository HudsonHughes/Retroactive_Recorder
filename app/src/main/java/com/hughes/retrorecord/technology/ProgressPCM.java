package com.hughes.retrorecord.technology;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;
import com.hughes.retrorecord.ByteRecorder;
import com.hughes.retrorecord.MainApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.hughes.retrorecord.technology.PcmAudioHelper.rawToWave;

public class ProgressPCM extends AsyncTask<Void, Void, String> {
    private ProgressDialog dialog;
    int sampling;
    File wavTarget;
    File rawSource;
    WavAudioFormat af;
    int amount;
    Context context;
    Activity activity;
    long prog = 0;
    int loop = 1000;
    int all = 0;
    MainApplication HelperClass;

    public ProgressPCM(final WavAudioFormat af, final File rawSource, final File wavTarget, final int amount, final int sampling, final Context context, final Activity activity) {
        HelperClass = new MainApplication(context.getApplicationContext());
        this.af = af;
        this.rawSource = rawSource;
        this.wavTarget = wavTarget;
        this.amount = amount;
        this.sampling = sampling;
        this.context = context;
        this.activity = activity;
        dialog = new ProgressDialog(activity);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setMax(100);
        dialog.setCancelable(false);
    }

    @Override
    protected void onPreExecute() {
        Intent intent = new Intent(context, ByteRecorder.class);
        SharedPreferences settings = context.getSharedPreferences("AppOn", 0);
        context.stopService(intent);
        settings.edit().putBoolean("AppOn", false).commit();
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
        Intent intent = new Intent(context, ByteRecorder.class);
        SharedPreferences settings = context.getSharedPreferences("AppOn", 0);
        context.startService(intent);
        settings.edit().putBoolean("AppOn", true).commit();
        if(result.equals("bad")) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "Save failed. Make sure there is enough space available.", Toast.LENGTH_SHORT).show();
                }
            });
        }
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    @Override
    protected String doInBackground(Void... params) {

        try {

            Log.d("Hudson Hughes", "Making file");
            if (!new File(context.getFilesDir() + "/magicMic/").exists()) {
                new File(context.getFilesDir() + "/magicMic/").mkdir();
            }
            File bufferFile = new File(context.getFilesDir() + "/magicMic/pcmbuffer.raw");
            File folder = new File(context.getFilesDir() + "/magic/");
            bufferFile.delete();
            double chunksneeded = amount * 6;
            double dub = 5292000;
            float samp = 41000;
            long progress = 0;
            long raw_length = 0;
            if (folder.listFiles() != null) {
                List<File> pcms = Arrays.asList(folder.listFiles());
                for (File file : pcms) {
                    raw_length += file.length();
                }
                float div = sampling / samp;
                Log.d("Hudson Hughes", "Sampling Size: " + String.valueOf(sampling));
                Log.d("Hudson Hughes", "Stock Sampling Size: " + String.valueOf(samp));
                long needed_length = Math.round(HelperClass.rateToByte(sampling) * amount);
                long start_position = raw_length - needed_length;
                if (start_position < 0) start_position = 0;
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
                                List<File> pcmss = Arrays.asList(folder.listFiles());
                                for (File filse : pcms) {
                                    raw_len += filse.length();
                                }
                                all = raw_len;
                                //Log.d("Hudson", String.valueOf(prog));
                                publishProgress();
                                loop = 0;
                            }
                            loop ++;
                        }
                        in.close();
                        out.close();
                    }
                }
                rawToWave(bufferFile, wavTarget, sampling);
//                new MediaScannerWrapper(context, wavTarget.getPath(), "audio/*").scan();
//                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + wavTarget.getPath())));

                MediaScannerConnection.scanFile(context, new String[]{wavTarget.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        // TODO Auto-generated method stub

                    }
                });

            }else return "bad";
        } catch (Exception e) {
            e.printStackTrace();
            return "bad";
        }

        return "";
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