package com.hughes.retrorecord.playback;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hughes.retrorecord.MainApplication;
import com.hughes.retrorecord.R;
import com.hughes.retrorecord.messages.RefreshEvent;
import com.hughes.retrorecord.messages.SongEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.hughes.retrorecord.R.id.CurrentlyPlaying;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PlaybackFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PlaybackFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlaybackFragment extends Fragment implements MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.pauseButton)
    ImageButton pauseButton;

    @BindView(R.id.firstTime)
    TextView firstTime;

    @BindView(R.id.secondTime)
    TextView secondTime;

    @BindView(R.id.seekBar)
    SeekBar seekBar;

    @BindView(CurrentlyPlaying)
    TextView currentlyPlaying;

    @BindView(R.id.folder)
    TextView folder;

    public PlaybackFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PlaybackFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PlaybackFragment newInstance() {
        PlaybackFragment fragment = new PlaybackFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(RefreshEvent event) {
        Log.d("Hudson", "Refresh Event");
        refreshMusicList();
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SongEvent track) {
        playSong(track.file);
    };

    public File currentTrack;
    private  MediaPlayer mp;
    // Handler to update UI timer, progress bar etc,.
    private Handler mHandler = new Handler();

    public void playSong(final File song){
        if(song == currentTrack)
            return;
        Log.d("Hudson", song.getAbsolutePath());
        // Play song
        try {
            try{
                mp.reset();
            }catch (Exception e){
                e.printStackTrace();
            }
             //song.getAbsolutePath()));
            FileInputStream fileInputStream = new FileInputStream(song.getAbsolutePath());
            mp = new MediaPlayer();
            mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    Log.d("Hudson", "Error");
                    resetView();
                    return false;
                }
            });
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    Log.d("Hudson", "Completion");
                    mp.seekTo(0);
                    mp.pause();
                    pauseButton.setEnabled(true);
                }
            });
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mp.setDataSource(fileInputStream.getFD());
            mp.prepare();
            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mp.start();
                    pauseButton.setEnabled(true);
                    currentlyPlaying.setText("Current Track: " + song.getName());
                    currentTrack = song;
                    seekBar.setEnabled(true);
                    // set Progress bar values
                    seekBar.setProgress(0);
                    seekBar.setMax(100);
                    // Updating progress bar
                    updateProgressBar();
                }
            });


        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            try {
                if(mp.isPlaying()){
                    pauseButton.setImageResource(android.R.drawable.ic_media_pause);
                }else{
                    pauseButton.setImageResource(android.R.drawable.ic_media_play);
                }
                long totalDuration = mp.getDuration();
                long currentDuration = mp.getCurrentPosition();

                // Displaying Total Duration time
                secondTime.setText("" + milliSecondsToTimer(totalDuration));
                // Displaying time completed playing
                firstTime.setText("" + milliSecondsToTimer(currentDuration));

                // Updating progress bar
                int progress = (int) (getProgressPercentage(currentDuration, totalDuration));
                //Log.d("Progress", ""+progress);
                seekBar.setProgress(progress);

                // Running this thread after 100 milliseconds
                mHandler.postDelayed(this, 100);
            }catch (IllegalStateException e){
                resetView();
            }
        }
    };

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    /**
     * When user stops moving the progress hanlder
     * */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = mp.getDuration();
        int currentPosition = progressToTimer(seekBar.getProgress(), totalDuration);

        // forward or backward to certain seconds
        mp.seekTo(currentPosition);

        // update timer progress again
        updateProgressBar();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {

    }

    @Override
    public void onCompletion(MediaPlayer arg0) {
        playSong(currentTrack);

    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        mp.release();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("Hudson", "onResume");
        resetView();
        refreshMusicList();

    }

    public void resetView(){
        try{
            mp.reset();
        }catch (Exception e){
            e.printStackTrace();
        }
        currentlyPlaying.setText("");
        currentTrack = null;
        seekBar.setEnabled(false);
        // set Progress bar values
        seekBar.setProgress(0);
        seekBar.setMax(100);
        pauseButton.setEnabled(false);
        pauseButton.setImageResource(android.R.drawable.ic_media_play);
        firstTime.setText(milliSecondsToTimer(0));
        secondTime.setText(milliSecondsToTimer(0));
        // Updating progress bar
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_playback, container, false);
        ButterKnife.bind(getActivity());
        seekBar = (SeekBar) view.findViewById(R.id.seekBar2);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        currentlyPlaying = (TextView) view.findViewById(R.id.CurrentlyPlaying);
        pauseButton = (ImageButton) view.findViewById(R.id.pauseButton);
        folder = (TextView) view.findViewById(R.id.folder);
        folder.setText(new MainApplication(getContext()).getRecordingDirectory());
        firstTime = (TextView) view.findViewById(R.id.firstTime);
        secondTime = (TextView) view.findViewById(R.id.secondTime);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new ListAdapter(getActivity()));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mp = new MediaPlayer();
        seekBar.setEnabled(false);
        seekBar.setOnSeekBarChangeListener(this); // Important
        mp.setOnCompletionListener(this); // Important
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Hudson", "pausePressed");
                if(mp.isPlaying()){
                    if(mp != null && currentTrack != null){
                        mp.pause();
                        // Changing button image to play button
                        pauseButton.setImageResource(android.R.drawable.ic_media_play);
                    }
                }else{
                    // Resume song
                    if(mp != null && currentTrack != null){
                        mp.start();
                        // Changing button image to pause button
                        pauseButton.setImageResource(android.R.drawable.ic_media_pause);
                    }
                }
            }
        });
        return view;
    }

    public void refreshMusicList(){
        folder.setText(new MainApplication(getContext()).getRecordingDirectory());
        ListAdapter listAdapter = new ListAdapter(getContext());
        recyclerView.setAdapter(listAdapter);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        Log.e(getActivity().getPackageName(), String.format("Error(%s%s)", what, extra));
        if(what == MediaPlayer.MEDIA_ERROR_SERVER_DIED)
            if(mp != null)
                mp.reset();
        else if(what == MediaPlayer.MEDIA_ERROR_UNKNOWN)
            if(mp != null)
                mp.reset();
        currentlyPlaying.setText("");
        currentTrack = null;
        seekBar.setProgress(0);
        seekBar.setEnabled(false);
        resetView();
        return false;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public String milliSecondsToTimer(long milliseconds){
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
    public int getProgressPercentage(long currentDuration, long totalDuration){
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
    public int progressToTimer(int progress, int totalDuration) {
        int currentDuration = 0;
        totalDuration = (int) (totalDuration / 1000);
        currentDuration = (int) ((((double)progress) / 100) * totalDuration);

        // return current duration in milliseconds
        return currentDuration * 1000;
    }
}
