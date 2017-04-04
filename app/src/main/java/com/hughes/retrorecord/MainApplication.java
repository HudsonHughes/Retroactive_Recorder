package com.hughes.retrorecord;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Created by Hudson Hughes on 4/3/2017.
 */

public class MainApplication {
    Context context;
    public MainApplication(Context ctx){
        context = ctx;
    }

    public boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("com.hughes.retrorecord.ByteRecorder".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public boolean getMicrophoneAvailable(){
        AudioRecord audio = null;
        boolean ready = true;
        try{
            int baseSampleRate = 44100;
            int channel = AudioFormat.CHANNEL_IN_MONO;
            int format = AudioFormat.ENCODING_PCM_16BIT;
            int buffSize = AudioRecord.getMinBufferSize(baseSampleRate, channel, format);
            audio = new AudioRecord(MediaRecorder.AudioSource.MIC, baseSampleRate, channel, format, buffSize );
            audio.startRecording();
            short buffer[] = new short[buffSize];
            int audioStatus = audio.read(buffer, 0, buffSize);

            if(audioStatus == AudioRecord.ERROR_INVALID_OPERATION || audioStatus == AudioRecord.STATE_UNINITIALIZED /* For Android 6.0 */)
                ready = false;
        }
        catch(Exception e){
            ready = false;
        }
        finally {
            try{
                audio.release();
            }
            catch(Exception e){}
        }

        return ready;
    }

    public static String defaultpath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/RetroactiveRecorder/";
    static public String milliSecondsToTimer(long milliseconds){
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int)( milliseconds / (1000*60*60));
        int minutes = (int)(milliseconds % (1000*60*60)) / (1000*60);
        int seconds = (int) ((milliseconds % (1000*60*60)) % (1000*60) / 1000);
        // Add hours if there
        if(hours > 0){
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if(seconds < 10){
            secondsString = "0" + seconds;
        }else{
            secondsString = "" + seconds;}

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    /**
     * Function to get Progress percentage
     * @param currentDuration
     * @param totalDuration
     * */
    static public int getProgressPercentage(long currentDuration, long totalDuration){
        Double percentage = (double) 0;

        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);

        // calculating percentage
        percentage =(((double)currentSeconds)/totalSeconds)*100;

        // return percentage
        return percentage.intValue();
    }

    /**
     * Function to change progress to timer
     * @param progress -
     * @param totalDuration
     * returns current duration in milliseconds
     * */
    static public int progressToTimer(int progress, int totalDuration) {
        int currentDuration = 0;
        totalDuration = (int) (totalDuration / 1000);
        currentDuration = (int) ((((double)progress) / 100) * totalDuration);

        // return current duration in milliseconds
        return currentDuration * 1000;
    }
    public int getSAMPLERATE(){
        int SAMPLING_RATE = 8000;
        SharedPreferences preferences = context.getSharedPreferences("settings", context.MODE_PRIVATE);
        for (int rate : new int[]{44100, 22050, 16000, 11025, 8000}) {  // add the rates you wish to check against
            int bufferSize = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            if (bufferSize > 0) {
                // buffer size is valid, Sample rate supported
                SAMPLING_RATE = rate;
                break;
            }
        }
        SAMPLING_RATE = preferences.getInt("SAMPLING_RATE", SAMPLING_RATE);
        return SAMPLING_RATE;
    }
    public int setSAMPLERATE(int SAMPLERATE){
        int SAMPLING_RATE = 8000;
        SharedPreferences preferences = context.getSharedPreferences("settings", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("SAMPLING_RATE", SAMPLERATE);
        editor.commit();
        SAMPLING_RATE = preferences.getInt("SAMPLING_RATE", SAMPLING_RATE);
        return SAMPLING_RATE;
    }

    public int getTIME(){
        SharedPreferences preferences = context.getSharedPreferences("settings", context.MODE_PRIVATE);
        int TIME = preferences.getInt("TIME", 5);
        return TIME;
    }
    public int setTIME(int TIME){
        SharedPreferences preferences = context.getSharedPreferences("settings", context.MODE_PRIVATE);
        int preTIME = preferences.getInt("TIME", TIME);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("TIME", TIME);
        editor.commit();
        TIME = preferences.getInt("TIME", TIME);
        Log.d("Hudson Hughes", "New Time: " + String.valueOf(TIME));
        if(TIME < preTIME){

        }
        return TIME;
    }

    public int getAMOUNT(){
        SharedPreferences preferences = context.getSharedPreferences("settings", context.MODE_PRIVATE);
        int TIME = preferences.getInt("AMOUNT", 5);
        return TIME;
    }
    public int setAMOUNT(int AMOUNT){
        SharedPreferences preferences = context.getSharedPreferences("settings", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("AMOUNT", AMOUNT);
        if(AMOUNT > 0)
            editor.commit();
        AMOUNT = preferences.getInt("AMOUNT", AMOUNT);
        Log.d("Hudson Hughes", "New Time: " + String.valueOf(AMOUNT));
        return AMOUNT;
    }

    public String generateStamp() {
        String mFileName = getRecordingDirectory();
        if (!new File(mFileName).exists()) {
            new File(mFileName).mkdir();
        }
        mFileName += getCurrentTimeStamp() + ".wav";
        return mFileName;
    }

    public String setRecordingDirectory(String path) {
        String mFileName = path;
        SharedPreferences preferences = context.getSharedPreferences("settings", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("PATH", path);
        editor.commit();
        return mFileName;
    }

    public String getRecordingDirectory() {
        SharedPreferences preferences = context.getSharedPreferences("settings", context.MODE_PRIVATE);
        String mFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/RetroactiveRecorder/";
        mFileName = preferences.getString("PATH", mFileName ) + "/";
        if (!new File(mFileName).exists()) {
            new File(mFileName).mkdir();
        }
        return mFileName;
    }

    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MM-yy_HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }
    public static int rateToByte(int rate){
        switch (rate) {
            case 44100:  return 5292000;
            case 22050:  return 5292000 / 2;
            case 16000:  return 1920000;
            case 11025:  return 5292000 / 4;
            case 8000:  return 960000;
            default: return 5292000;
        }
    }
}
