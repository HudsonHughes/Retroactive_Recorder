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
import com.hughes.retrorecord.R;
import com.hughes.retrorecord.messages.SongEvent;

import org.apache.commons.io.FilenameUtils;
import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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
    }

    public void refresh(){
        String path = mainApplication.getRecordingDirectory();
        File directory = new File(path);
        File[] files = directory.listFiles();
        if(files != null)
        for(File file: files){
            if(FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("wav")){
                fileArrayList.add(file);
            }
        }
    }

    public void delete(int position){
        boolean deleted = fileArrayList.get(position).delete();
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
        File file = fileArrayList.get(position);
        holder.name.setText(file.getName());
//        Uri uri = Uri.parse(file.getAbsolutePath());
//        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
//        mmr.setDataSource(context, uri);
//        String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
//        int duration = Integer.parseInt(durationStr);
        int duration = 0;
        holder.time.setText(String.format("%d:%d",
                TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
        ));
        Date lastModDate = new Date(file.lastModified());
        holder.date.setText(lastModDate.toString());
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new SongEvent(fileArrayList.get(position)));
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
