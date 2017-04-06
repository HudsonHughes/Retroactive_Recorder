package com.hughes.retrorecord.recording;

import android.app.Activity;
import android.content.Context;

import com.github.pwittchen.prefser.library.Prefser;
import com.hughes.retrorecord.MainApplication;
import com.hughes.retrorecord.technology.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BytesToFile {

    public static BytesToFile singleton;
    Context context;
    Prefser prefs;
    MainApplication mainApplication;
    int counter = 0;
    public static BytesToFile getInstance(Context ctx){

        if(singleton == null) {
            singleton = new BytesToFile(ctx);
        }
        return singleton;
    }

    public BytesToFile(Context ctx){
        context = ctx;
        prefs = new Prefser(context);
        mainApplication = new MainApplication(ctx);
    }

    public void saveAudio(Activity activity){
            new SaveAudio(activity).execute();
    }

    public void removeFileFromHash(String name){
        HashMap<String, Double> fileMap = prefs.get("fileMap", HashMap.class, new HashMap<String, Double>());
        File file = new File(magicPath() + name);
        file.delete();
        fileMap.remove(name);
        prefs.put("fileMap", fileMap);
    }

    public void removeOldestFromHash(){
        HashMap<String, Double> fileMap = prefs.get("fileMap", HashMap.class, new HashMap<String, Double>());
        ArrayList<String> files = new ArrayList<String>(fileMap.keySet());
        Collections.sort(files);
        removeFileFromHash(files.get(0));
    }

    public Integer addTimetoHash(String name, int length){
        HashMap<String, Double> fileMap = prefs.get("fileMap", HashMap.class, new HashMap<String, Double>());
        double current = Math.round(fileMap.containsKey(name) ? fileMap.get(name) : 0.0);
        fileMap.put(name, current + length);
        prefs.put("fileMap", fileMap);
        return (int) Math.round(current) + length;
    }

    public Integer getLengthOfHash(){
        HashMap<String, Double> fileMap = prefs.get("fileMap", HashMap.class, new HashMap<String, Double>());
        if(fileMap.isEmpty()) return 0;
        Integer length = 0;
        for (Map.Entry<String, Double> entry : fileMap.entrySet()) {
            int i = length;
            int j = (int) Math.round(entry.getValue());
            length = i + j;
        }
        return length;
    }

    public void trimHash(){
        long current = convertLengthToSeconds(getLengthOfHash());
        int max = (mainApplication.getTIME() + 1) * 60;
        while(convertLengthToSeconds(getLengthOfHash()) > (mainApplication.getTIME() + 1) * 60){
            Log.d("Hudson", "Deleted File");
            removeOldestFromHash();
        }
    }

    public String magicPath(){
        return context.getFilesDir() + "/magic/";
    }

    public long convertLengthToSeconds(long length){
        return 2 * length / getBytesPerSecond();
    }

    public Integer convertSecondsToLength(Integer seconds){
        return seconds * getBytesPerSecond();
    }

    public Integer getBytesPerSecond(){
        return Math.round(new MainApplication(context).getSAMPLERATE()) * 2  * 1 * 16 / 8;
    }

    public void wipeCache(){
        try {
            for (File file : new File(magicPath()).listFiles()) file.delete();
            HashMap<String, Double> fileMap = prefs.get("fileMap", HashMap.class, new HashMap<String, Double>());
            fileMap.clear();
            prefs.put("fileMap", fileMap);
        } catch (Exception e) {

        }
    }

    public void writeToFile(byte[] message) throws IOException{
        String name = String.valueOf(System.currentTimeMillis()/30000) + ".wav";
        try {
            if(!new File(magicPath()).isDirectory())new File(magicPath()).mkdir();
            File nowFile = new File(magicPath() + name);
            FileOutputStream fos = new FileOutputStream(nowFile, true);
            fos.write(message);
            fos.close();
            addTimetoHash(name, message.length);
            if(counter > 30) {
                trimHash();
                counter = 0;
            }
            counter += 1;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}