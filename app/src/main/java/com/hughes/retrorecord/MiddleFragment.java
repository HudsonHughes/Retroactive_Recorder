package com.hughes.retrorecord;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
import com.hughes.retrorecord.technology.ProgressPCM;
import com.hughes.retrorecord.technology.WavAudioFormat;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.Arrays;

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
    private String mParam1;
    private String mParam2;
    long first = 0;
    long latest = 0;
    SeekBar seekBar;
    TextView timeLabel;
    Button button;
    TextView MaxLabel;
    TextView CurrentLength;

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

    MainApplication HelperClass;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_middle, container, false);
        ButterKnife.bind(getActivity());
        HelperClass = new MainApplication(getcontext().getApplicationContext());

        final SharedPreferences sharedPreferences = getcontext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean("showHelp", true)) {
            //startActivity(new Intent(getcontext(), HelpActivity.class));

            AlertDialog.Builder builder = new AlertDialog.Builder(getcontext());
// Add the buttons
            builder.setTitle("Welcome");
            builder.setMessage(helpMessage);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK button
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("showHelp", false);
                    editor.commit();
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


        MaxLabel = (TextView) view.findViewById(R.id.MaxLength);
        CurrentLength = (TextView) view.findViewById(R.id.CurrentLength);
        timeLabel = (TextView) view.findViewById(R.id.timeLabel);
        seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        button = (Button) view.findViewById(R.id.Button);
        seekBar.setProgress(HelperClass.getAMOUNT());
        timeLabel.setText(String.valueOf(HelperClass.getAMOUNT() + " minutes"));
        mHandler.post(onEverySecond);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress >= 1) {
                    HelperClass.setAMOUNT(progress);
                    timeLabel.setText(String.valueOf(HelperClass.getAMOUNT() + " minutes"));
                } else {
                    seekBar.setProgress(1);
                }
                if (progress < HelperClass.getTIME()) {
                    HelperClass.setAMOUNT(progress);
                    timeLabel.setText(String.valueOf(HelperClass.getAMOUNT() + " minutes"));

                } else {
                    seekBar.setProgress(HelperClass.getTIME());
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
                getAudio();
            }
        });
        refresh();

        return view;
    }

    public void refresh() {
        button.setText("click here to save the buffer of the past.");
        long folder_length = 0;
        try {
            folder_length = BytesToFile.getInstance(getContext()).getLengthOfHash();
        } catch (Exception e) {
            e.printStackTrace();
            folder_length = 0;

        }
        latest = folder_length;
        if(latest != first) button.setText("Recording...\n\nClick to save the current buffer.");
        else button.setText("Click to save the current buffer.\nNot Recording...");
        first = latest;
        double di = (double)HelperClass.getSAMPLERATE() / (double)41000;
        double div = (double)folder_length / (HelperClass.rateToByte(HelperClass.getSAMPLERATE()) / (double)60);
        int currentSeconds = (int) Math.round(div);
        if (HelperClass.getTIME() * 60 < currentSeconds) {
            currentSeconds = HelperClass.getTIME() * 60;
        }
        String minutes = String.valueOf(currentSeconds / 60);
        String seconds = String.valueOf(currentSeconds % 60);
        if (minutes.length() == 1) minutes = "0" + minutes;
        if (seconds.length() == 1) seconds = "0" + seconds;
        CurrentLength.setText(minutes + ":" + seconds);
        MaxLabel.setText(String.valueOf(HelperClass.getTIME()) + ":00");
        timeLabel.setText(String.valueOf(HelperClass.getAMOUNT() + " minutes"));
        if (HelperClass.isServiceRunning()) {
            button.setEnabled(true);
        } else {
            button.setText("Activate the background listening in the settings.");
            button.setEnabled(false);
        }
//        fileList.clear();
//        if(new File(getFilesDir() + "/magic/").isDirectory() && new File(getFilesDir() + "/magic/").listFiles().length > 0) {
//            List<File> pcms = Arrays.asList(new File(getFilesDir() + "/magic/").listFiles());
//            Collections.sort(pcms);
//            for (File file : pcms) {
//                fileList.add(file.getName());
//            }
//            adapter.notifyDataSetChanged();
//        }
        if(seekBar.getProgress() > HelperClass.getTIME()){
            seekBar.setProgress(HelperClass.getTIME());
            timeLabel.setText(String.valueOf(HelperClass.getTIME() + " minutes"));
        }
    }

    public void getAudio() {
        File file_raw = new File(getcontext().getFilesDir() + "/buffer.raw");
        if (!file_raw.exists()) {
            return;
        }
        File file_wav = new File(HelperClass.generateStamp());
        if (file_wav.exists()) {
            file_wav.delete();
        }

        try {
            new ProgressPCM(WavAudioFormat.mono16Bit(Math.round(HelperClass.getSAMPLERATE())), file_raw, file_wav, HelperClass.getAMOUNT(), Math.round(HelperClass.getSAMPLERATE()), getActivity().getApplicationContext(), getActivity()).execute();
            //new WavMaker(getcontext(), HelperClass.getTIME(MainActivity.this)).get();

        } catch (Exception e) {
            e.printStackTrace();

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
