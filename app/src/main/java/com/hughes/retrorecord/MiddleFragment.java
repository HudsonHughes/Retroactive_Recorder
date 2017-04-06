package com.hughes.retrorecord;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hughes.retrorecord.messages.MessageEvent;
import com.hughes.retrorecord.recording.BytesToFile;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MiddleFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MiddleFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MiddleFragment extends Fragment {

    public void helpAct(View view){
        String helpMessage = "WARNING: Using the task manager to close the application will temporarily interrupt the recording of audio. Interuptting the audio recording via the task manager or settings menu will not wipe the audio buffer. So if you save a file in which the recording was interrupted the audio will skip from where the recording was paused to when it was resumed.\n" +
                "Retroactive Recording is a retroactive audio recording app. The app is constantly recording audio while it is activated but only ever keeps the most recent 1 to 30 minutes of data." +
                " At any time you can tap the save audio button on the main screen to save previously recorded audio. For example, if you turn the app on in the beginning of the day, keep it on and later in the day, someone says something that you want a recording of, you can tap the save audio button and your device will store the past 1 to 30 minutes in a WAV audio file for later listening." +
                " While the app is constantly recording audio, it will only occupy up to 30 minutes worth of audio data in the memory." +
                " In the event that your device runs out of storage space the app will cease to operate.";
        //startActivity(new Intent(getContext(), HelpActivity.class));
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getContext());
// Add the buttons
        builder.setMessage(helpMessage);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
            }
        });
// Set other dialog properties

// Create the AlertDialog
        android.support.v7.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void goPro(View view){
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.hughes.retrorecordpro")));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.hughes.retrorecordpro")));
        }
    }

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    String helpMessage = "WARNING: Using the task manager to close the application will temporarily interrupt the recording of audio. Interuptting the audio recording via the task manager or settings menu will not wipe the audio buffer. So if you save a file in which the recording was interrupted the audio will skip from where the recording was paused to when it was resumed.\n" +
            "Retroactive Recording is a retroactive audio recording app. The app is constantly recording audio while it is activated but only ever keeps the most recent 1 to 30 minutes of data." +
            " At any time you can tap the save audio button on the main screen to save previously recorded audio. For example, if you turn the app on in the beginning of the day, keep it on and later in the day, someone says something that you want a recording of, you can tap the save audio button and your device will store the past 1 to 30 minutes in a WAV audio file for later listening." +
            " While the app is constantly recording audio, it will only occupy up to 30 minutes worth of audio data in the memory." +
            " In the event that your device runs out of storage space the app will cease to operate.";
    // TODO: Rename and change types of parameters
    SeekBar seekBar;
    TextView timeLabel;
    Button button;
    TextView maxLabel;
    TextView currentLength;
    TextView status;
    Button goProButton;
    Button helpButton;
    int periods = 0;
    long timeInBuffer = 0;
    int lengthOfBuffer = 0;
    private OnFragmentInteractionListener mListener;

    public MiddleFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MiddleFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MiddleFragment newInstance() {
        MiddleFragment fragment = new MiddleFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {

    };

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        mHandler.removeCallbacks(onEverySecond);
        mHandler.postDelayed(onEverySecond, 1000);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        mHandler.removeCallbacks(onEverySecond);
    }

    private Handler mHandler = new Handler();

    private Runnable onEverySecond = new Runnable() {
        public void run() {
            try {
                refresh();
            } catch (Exception e) {

            }
            mHandler.postDelayed(onEverySecond, 1000);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    MainApplication mainApplication;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_middle, container, false);
        ButterKnife.bind(getActivity());
        mainApplication = new MainApplication(getcontext().getApplicationContext());

        if (!new MainApplication(getContext()).getStartOnBoot()) {
            //startActivity(new Intent(getcontext(), HelpActivity.class));

            AlertDialog.Builder builder = new AlertDialog.Builder(getcontext());
// Add the buttons
            builder.setTitle("Welcome");
            builder.setMessage(helpMessage);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK button
                    mainApplication.setStartOnBoot(true);
                }
            });
// Set other dialog properties

// Create the AlertDialog
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        //listview = (ListView) findViewById(R.id.listView);
        //adapter = new ArrayAdapter<String>(this,
        //android.R.layout.simple_list_item_1, android.R.id.text1, fileList);
        // Assign adapter to ListView
        //listview.setAdapter(adapter);


        maxLabel = (TextView) view.findViewById(R.id.MaxLength);
        currentLength = (TextView) view.findViewById(R.id.CurrentLength);
        timeLabel = (TextView) view.findViewById(R.id.timeLabel);
        seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        button = (Button) view.findViewById(R.id.Button);
        seekBar.setProgress(mainApplication.getAMOUNT());
        status = (TextView) view.findViewById(R.id.status);
        timeLabel.setText(String.valueOf(mainApplication.getAMOUNT() + " minutes"));
        helpButton = (Button) view.findViewById(R.id.helpButtonMiddle);
        goProButton = (Button) view.findViewById(R.id.goProButtonMiddle);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String helpMessage = "WARNING: Using the task manager to close the application will temporarily interrupt the recording of audio. Interuptting the audio recording via the task manager or settings menu will not wipe the audio buffer. So if you save a file in which the recording was interrupted the audio will skip from where the recording was paused to when it was resumed.\n" +
                        "Retroactive Recording is a retroactive audio recording app. The app is constantly recording audio while it is activated but only ever keeps the most recent 1 to 30 minutes of data." +
                        " At any time you can tap the save audio button on the main screen to save previously recorded audio. For example, if you turn the app on in the beginning of the day, keep it on and later in the day, someone says something that you want a recording of, you can tap the save audio button and your device will store the past 1 to 30 minutes in a WAV audio file for later listening." +
                        " While the app is constantly recording audio, it will only occupy up to 30 minutes worth of audio data in the memory." +
                        " In the event that your device runs out of storage space the app will cease to operate.";
                //startActivity(new Intent(getContext(), HelpActivity.class));
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getContext());
// Add the buttons
                builder.setMessage(helpMessage);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                    }
                });
// Set other dialog properties

// Create the AlertDialog
                android.support.v7.app.AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        goProButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.hughes.retrorecordpro")));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.hughes.retrorecordpro")));
                }
            }
        });
        if(mainApplication.getAMOUNT() == 1){
            timeLabel.setText(mainApplication.getAMOUNT() + " minute");
        }
        mHandler.post(onEverySecond);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress >= 1) {
                    mainApplication.setAMOUNT(progress);
                    timeLabel.setText(String.valueOf(mainApplication.getAMOUNT() + " minutes"));
                    if(mainApplication.getAMOUNT() == 1){
                        timeLabel.setText(mainApplication.getAMOUNT() + " minute");
                    }
                } else {
                    seekBar.setProgress(1);
                }
                if (progress < mainApplication.getTIME()) {
                    mainApplication.setAMOUNT(progress);
                    timeLabel.setText(String.valueOf(mainApplication.getAMOUNT() + " minutes"));
                    if(mainApplication.getAMOUNT() == 1){
                        timeLabel.setText(mainApplication.getAMOUNT() + " minute");
                    }
                } else {
                    seekBar.setProgress(mainApplication.getTIME());
                }
                refresh();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BytesToFile.getInstance(getContext()).saveAudio(getActivity());
            }
        });
        refresh();

        return view;
    }

    public static String repeat(int count, String with) {
        return new String(new char[count]).replace("\0", with);
    }

    public static String secondsToString(int pTime) {
        pTime *= 1;
        return String.format("%02d:%02d", pTime / 60, pTime % 60);
    }

    public static String secondsToString(long pTime) {
        pTime *= 1;
        return String.format("%02d:%02d", pTime / 60, pTime % 60);
    }

    public void refresh() {
        long folder_length = 0;
        try {
            folder_length = BytesToFile.getInstance(getContext()).getLengthOfHash();
        } catch (Exception e) {
            folder_length = 0;
        }
        timeInBuffer = BytesToFile.getInstance(getContext()).convertLengthToSeconds(folder_length);
        currentLength.setText ( secondsToString ( Math.min ( mainApplication.getTIME() * 60, timeInBuffer ) ) );
        maxLabel.setText(secondsToString(mainApplication.getTIME() * 60));
        timeLabel.setText(String.valueOf((mainApplication.getAMOUNT()) + " minutes"));
        if(mainApplication.getAMOUNT() == 1){
            timeLabel.setText(mainApplication.getAMOUNT() + " minute");
        }
        if (mainApplication.isServiceRunning()) {
            periods += 1;
            if(periods > 3) periods = 0;
            status.setText("Recording" + repeat(periods, "."));
        } else {
            status.setText("Activate recording in the settings");
        }
        if(seekBar.getProgress() > mainApplication.getTIME()){
            seekBar.setProgress(mainApplication.getTIME());
            timeLabel.setText(String.valueOf(mainApplication.getTIME() + " minutes"));
            if(mainApplication.getTIME() == 1){
                timeLabel.setText(mainApplication.getTIME() + " minute");
            }
        }
    }

    public Context getcontext() {
        return getContext();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
}
