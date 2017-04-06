package com.hughes.retrorecord.playback;

import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hughes.retrorecord.MainApplication;
import com.hughes.retrorecord.MiddleFragment;
import com.hughes.retrorecord.R;
import com.hughes.retrorecord.messages.SongEvent;
import com.hughes.retrorecord.technology.Log;

import org.apache.commons.io.FilenameUtils;
import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by Hudson Hughes on 4/3/2017.
 */

class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    MainApplication mainApplication;
    ArrayList<File> fileArrayList = new ArrayList<>();
    Context context;

    public ListAdapter(Context ctx){
        context = ctx;
        mainApplication = new MainApplication(context);
        refresh();
    }

    public void refresh(){
        String path = mainApplication.getRecordingDirectory();
        File directory = new File(path);
        File[] files = directory.listFiles();
        fileArrayList.clear();
        if(files != null)
        for(File file: files){
            if(FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("wav")){
                fileArrayList.add(file);
            }
        }

        Collections.sort(fileArrayList, new Comparator<File>() {
            @Override
            public int compare(File file, File t1) {
                return -file.compareTo(t1);
            }
        });
    }

    public void delete(int position){
        fileArrayList.get(position).delete();
        refresh();
        notifyDataSetChanged();
    }

    @Override
    public ListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View musicView = inflater.inflate(R.layout.musicitem, parent, false);

        ViewHolder viewHolder = new ViewHolder(musicView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ListAdapter.ViewHolder holder, final int position) {
        final File file = new File(fileArrayList.get(position).getAbsolutePath());
        holder.name.setText(file.getName());
        Uri uri = Uri.parse(file.getAbsolutePath());
        String durationStr;
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            FileInputStream fileInputStream = new FileInputStream(file.getAbsolutePath());
            mmr.setDataSource(fileInputStream.getFD());
            durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        }catch (Exception e){
            e.printStackTrace();
            durationStr = "0";
        }
        int duration = Integer.parseInt(durationStr);
        Log.d("Hudson", "Time: " + duration);
        holder.time.setText(
                MiddleFragment.secondsToString(duration / 1000)
        );
        Date lastModDate = new Date(file.lastModified());
        holder.date.setText(lastModDate.toString());
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new SongEvent(file));
            }
        });
        holder.parentLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new AlertDialog.Builder(context)
                        .setMessage("Delete File?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                delete(position);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return fileArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView name;
        public TextView date;
        public TextView time;
        public RelativeLayout parentLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            date = (TextView) itemView.findViewById(R.id.date);
            time = (TextView) itemView.findViewById(R.id.time);
            parentLayout = (RelativeLayout) itemView.findViewById(R.id.parentLayout);
        }
    }
}
