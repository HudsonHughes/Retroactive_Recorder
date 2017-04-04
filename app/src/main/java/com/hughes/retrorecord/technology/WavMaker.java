package com.hughes.retrorecord.technology;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

/**
 * Created by Hudson Hughes on 12/23/2015.
 */
class WavMaker extends AsyncTask<String, Void, String> {
    private Context context;
    public static int SAMPLING_RATE = 44100;
    public static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    public static final int CHANNEL_IN_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    public static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLING_RATE, CHANNEL_IN_CONFIG, AUDIO_FORMAT);
    SharedPreferences preferences;
    File bufferfile;
    int time;

    public WavMaker(Context mContext, int mTime) {
        Log.d("Hudson", "WavMaker");
        context = mContext;
        preferences = context.getSharedPreferences("settings", context.MODE_PRIVATE);
        bufferfile = new File(context.getFilesDir() + "/buffer.raw");
        time = mTime;
    }

    @Override
    protected String doInBackground(String... params) {

        SAMPLING_RATE = preferences.getInt("SAMPLING_RATE", SAMPLING_RATE);
        int sRate;
        short nChannels;
        short mBitsPersample;
        int mBufferSize;
        mBitsPersample = 16;
        nChannels = 1;
        sRate = SAMPLING_RATE;
        int aFormat = AUDIO_FORMAT;

        try {
            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto you
            int length = (int) bufferfile.length();
            Log.d("Hudson WAV", "WAV Starting to make file");
            String nam = generateStamp();
            Log.d("Hudson WAV", "WAV" + nam);
            RandomAccessFile randomAccessReader = new RandomAccessFile(bufferfile, "r");
            int minute41k = 5292000;
            int threshhold = minute41k * time * 1 * (SAMPLING_RATE / 41000) - 4;
            byte[] bytes;
            if (length > threshhold) {
                Log.d("Hudson Hughes", "sliceing");
                byte[] bits = new byte[length];
                randomAccessReader.read(bits);
                bytes = new byte[threshhold];
                Log.d("Hudson Hughes", "from " + String.valueOf(length - threshhold) + " + " + String.valueOf(length));
                bytes = Arrays.copyOfRange(bits, 0, threshhold);
                randomAccessReader.read(bytes);
                randomAccessReader.close();
            } else {
                Log.d("Hudson Hughes", "not sliving");
                bytes = new byte[length];
                randomAccessReader.seek(0);
                randomAccessReader.read(bytes, 0, length);
                threshhold = length;
                Log.d("Hudson WAV", "WAV Biggness " + String.valueOf(threshhold));
                randomAccessReader.close();
            }
            Log.d("Hudson WAV", "WAV Biggness " + String.valueOf(threshhold));
            RandomAccessFile randomAccessWriter = new RandomAccessFile(nam, "rw");
            Log.d("Hudson WAV", "WAV BufferInputStream, RandomAccess File Initalized");
            randomAccessWriter.setLength(length); // Set file length to 0, to prevent unexpected behavior in case the file already existed
            randomAccessWriter.write(bytes);
            randomAccessWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("Hudson Hughes", "Cant Save File, No Space");
            //Toast.makeText(context, "Not Enough Space On Internal Storage", Toast.LENGTH_LONG);
        }
        Log.d("Hudson Hughes", "Stopping make file");
        return "Executed";
    }

    @Override
    protected void onPostExecute(String result) {

    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected void onProgressUpdate(Void... values) {
    }

    public String generateStamp() {
        String mFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
        mFileName += String.valueOf(System.currentTimeMillis() / 1000) + ".pcm";
        Log.d("Hudson", mFileName);
        return mFileName;
    }

    public String generateStamp2() {
        String mFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/rewind/";
        mFileName += String.valueOf(System.currentTimeMillis() / 1000) + ".pcm";
        Log.d("Hudson", mFileName);
        return mFileName;
    }
}