package com.hughes.retrorecord;

import android.app.ActivityManager;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import com.github.pwittchen.prefser.library.Prefser;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Created by Hudson Hughes on 4/3/2017.
 */

public class MainApplication {
    Context context;
    Prefser prefs;
    public MainApplication(Context ctx){
        context = ctx;
        prefs = new Prefser(context);
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
        for (int rate : new int[]{44100, 22050, 16000, 11025, 8000}) {  // add the rates you wish to check against
            int bufferSize = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            if (bufferSize > 0) {
                // buffer size is valid, Sample rate supported
                SAMPLING_RATE = rate;
                break;
            }
        }
        SAMPLING_RATE = prefs.get("SAMPLING_RATE", Integer.class, SAMPLING_RATE);
        return SAMPLING_RATE;
    }

    public boolean getStartTutorial(){
        return prefs.get("tutorial", Boolean.class, false);
    }

    public boolean setStartTutorial(boolean value){
        prefs.put("tutorial", value);
        return value;
    }

    public boolean getStartOnBoot(){
        return prefs.get("supposedToBeOn", Boolean.class, false);
    }

    public boolean setStartOnBoot(boolean value){
        prefs.put("supposedToBeOn", value);
        return value;
    }

    public int setSAMPLERATE(int SAMPLERATE){
        int SAMPLING_RATE = 8000;
        prefs.put("SAMPLING_RATE", SAMPLERATE);
        SAMPLING_RATE = prefs.get("SAMPLING_RATE", Integer.class,SAMPLING_RATE);
        return SAMPLING_RATE;
    }

    public int getTIME(){
        int TIME = prefs.get("TIME", Integer.class, 5);
        return TIME;
    }
    public int setTIME(int TIME){
        int preTIME = prefs.get("TIME", Integer.class ,TIME);
        prefs.put("TIME", TIME);
        TIME = prefs.get("TIME", Integer.class, TIME);
        Log.d("Hudson Hughes", "New Time: " + String.valueOf(TIME));
        if(TIME < preTIME){

        }
        return TIME;
    }

    public int getAMOUNT(){
        int TIME = prefs.get("AMOUNT", Integer.class, 5);
        return TIME;
    }
    public int setAMOUNT(int AMOUNT){
        if(AMOUNT > 0)
            prefs.put("AMOUNT", AMOUNT);
        AMOUNT = prefs.get("AMOUNT", Integer.class, AMOUNT);
        Log.d("Hudson Hughes", "New Time: " + String.valueOf(AMOUNT));
        return AMOUNT;
    }

    public String generateStamp(int extra) {
        String mFileName = getRecordingDirectory();
        if (!new File(mFileName).exists()) {
            new File(mFileName).mkdir();
        }
        if(extra > 0)
            return mFileName + "/" + getCurrentTimeStamp() + "(" + String.valueOf(extra) + ")" + ".wav";
        return mFileName + "/" + getCurrentTimeStamp() + ".wav";
    }

    public String setRecordingDirectory(String path) {
        String mFileName = path;
        prefs.put("PATH", path);
        return mFileName;
    }

    public String getRecordingDirectory() {
        String mFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/RetroactiveRecorder/";
        mFileName = prefs.get("PATH", String.class, mFileName );
        if (!new File(mFileName).exists()) {
            new File(mFileName).mkdir();
        }
        return mFileName;
    }

    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("MM-dd-yy_HH:mm:ss");//dd/MM/yyyy
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
