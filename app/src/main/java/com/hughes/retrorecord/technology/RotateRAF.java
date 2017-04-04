package com.hughes.retrorecord.technology;

import android.content.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RotateRAF {
    public RotateRAF(byte[] message, Context context, int Size) throws IOException {
        int length;
        try{
            length = new File(context.getFilesDir() + "/magic/").listFiles().length;
        }catch(Exception e){
            length = 0;
        }
        if (length <= (Size * 2) + 2)
            WriteToRAF(message, context);
        else {
            WriteToRAF(message, context);
            deleteRAF(context, Size);
        }

    }
    public static void WriteToRAF(byte[] message, Context context)
            throws IOException {
        try {
            if(!new File(context.getFilesDir() + "/magic/").isDirectory())new File(context.getFilesDir() + "/magic/").mkdir();
            File nowFile = new File(context.getFilesDir() + "/magic/" + String.valueOf(System.currentTimeMillis()/30000) + ".wav");
            FileOutputStream fos = new FileOutputStream(nowFile, true);
            fos.write(message);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void deleteRAF(Context context, int Size) throws IOException {

        List<File> pcms = Arrays.asList(new File(context.getFilesDir() + "/magic/").listFiles());
        Collections.sort(pcms);
        while(new File(context.getFilesDir() + "/magic/").listFiles().length > (Size * 2) + 2){
            List<File> raws = Arrays.asList(new File(context.getFilesDir() + "/magic/").listFiles());
            Collections.sort(raws);
            Log.d("Hudson Hughes", "Deleted " + raws.get(0));
            raws.get(0).delete();
        }

    }

    public static long folderSize(File directory) {
        if(!directory.exists())return 0;
        if(directory.list().length == 0) return 0;
        Collections.sort(Arrays.asList(directory.list()));
        long length = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile())
                length += file.length();
            else
                length += folderSize(file);
        }
        return length;
    }

}