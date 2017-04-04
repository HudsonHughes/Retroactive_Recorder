package com.hughes.retrorecord.technology;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PcmAudioHelper {

    /**
     * Converts a pcm encoded raw audio stream to a wav file.
     *
     * @param af
     * @param rawSource
     * @param wavTarget
     * @throws IOException
     */
    public static void convertRawToWav(final WavAudioFormat af, final File rawSource, final File wavTarget, final int amount, final int sampling, final Context context, final Activity activity) throws IOException {
        Thread thread = new Thread() {
            @Override
            public void run() {
                ProgressDialog progresss = new ProgressDialog(context);
                try {

                    progresss.setTitle("Compiling");
                    progresss.setMessage("Please wait while the wav file is made...");
                    progresss.show();
                    Log.d("Hudson Hughes", "Making file");
                    if (!new File(context.getFilesDir() + "/magicMic/").exists()) {
                        new File(context.getFilesDir() + "/magicMic/").mkdir();
                    }
                    File bufferFile = new File(context.getFilesDir() + "/magicMic/pcmbuffer.raw");
                    File folder = new File(context.getFilesDir() + "/magic/");
                    bufferFile.delete();
                    double chunksneeded = amount * 6;
                    double dub = 5292000;
                    double samp = 41000;
                    int progress = 0;
                    long raw_length = 0;
                    if (folder.listFiles() == null)return;
                    List<File> pcms = Arrays.asList(folder.listFiles());
                    for(File file:pcms){
                        raw_length += file.length();
                    }
                    double div = (double)sampling / samp;
                    long needed_length = Math.round(div * dub * amount);
                    long start_position = raw_length - needed_length;
                    if(start_position < 0) start_position = 0;
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
                                if(progress > start_position) {
                                    //Log.d("Hudson", "Length: " + String.valueOf(len) + " " + String.valueOf(progress));
                                    out.write(buf, 0, len);
                                }
                            }
                            in.close();
                            out.close();
                        }
                    }
                    rawToWave(bufferFile, wavTarget, sampling);
                    progresss.dismiss();
                } catch (IOException e) {
                    e.printStackTrace();
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Toast.makeText(context, "Save failed. Make sure there is enough space available.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    progresss.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "Save failed. Make sure there is enough space available.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    progresss.dismiss();
                }
            }
        };
        thread.start();
    }

    static void rawToWave(final File rawFile, final File waveFile, int SAMPLE_RATE) throws IOException {


        DataOutputStream output = null;
        try {
            output = new DataOutputStream(new FileOutputStream(waveFile));
            // WAVE header
            // see http://ccrma.stanford.edu/courses/422/projects/WaveFormat/
            writeString(output, "RIFF"); // chunk id
            writeInt(output, 36 + (int) rawFile.length()); // chunk size
            writeString(output, "WAVE"); // format
            writeString(output, "fmt "); // subchunk 1 id
            writeInt(output, 16); // subchunk 1 size
            writeShort(output, (short) 1); // audio format (1 = PCM)
            writeShort(output, (short) 1); // number of channels
            writeInt(output, SAMPLE_RATE); // sample rate
            writeInt(output, SAMPLE_RATE * 2); // byte rate
            writeShort(output, (short) 2); // block align
            writeShort(output, (short) 16); // bits per sample
            writeString(output, "data"); // subchunk 2 id
            writeInt(output, (int) rawFile.length()); // subchunk 2 size
            // Audio data (conversion big endian -> little endian)
            InputStream in = new FileInputStream
                    (rawFile);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                output.write(buf);
            }
            in.close();
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    static void writeInt(final DataOutputStream output, final int value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
        output.write(value >> 16);
        output.write(value >> 24);
    }

    static void writeShort(final DataOutputStream output, final short value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
    }

    static void writeString(final DataOutputStream output, final String value) throws IOException {
        for (int i = 0; i < value.length(); i++) {
            output.write(value.charAt(i));
        }
    }

}