package com.hughes.retrorecord;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.hughes.retrorecord.messages.MessageEvent;
import com.hughes.retrorecord.recording.BytesToFile;
import com.obsez.android.lib.filechooser.ChooserDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

import butterknife.ButterKnife;


public class SettingsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {

    };

    Button SampleButton;
    TextView BufferSizeLabel;
    Switch toggle;
    Button BufferSizeButton;
    Button DeleteButton;
    Button DirectoryButton;
    Button helpButton;
    Button goProButton;
    TextView pathView;
    TextView sampleView;
    MainApplication mainApplication;

    public Context getContext(){
        return getActivity();
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
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        mainApplication = new MainApplication(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(getActivity());
        BufferSizeLabel = (TextView) view.findViewById(R.id.BufferSize);
        toggle = (Switch) view.findViewById(R.id.switch1);
        SampleButton = (Button) view.findViewById(R.id.SampleButton);
        BufferSizeButton = (Button) view.findViewById(R.id.BufferSizeButton);
        DeleteButton = (Button) view.findViewById(R.id.DeleteButton);
        DirectoryButton = (Button) view.findViewById(R.id.DirectoryButton);
        pathView = (TextView) view.findViewById(R.id.PathView);
        pathView.setText(mainApplication.getRecordingDirectory());
        sampleView = (TextView) view.findViewById(R.id.SampleView);
        sampleView.setText("Sampling Rate:" + String.valueOf(mainApplication.getSAMPLERATE()));
        helpButton = (Button) view.findViewById(R.id.helpButtonSettings);
        goProButton = (Button) view.findViewById(R.id.goProButtonSettings);
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
        refreshViews();
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

// 2. Chain together various setter methods to set the dialog characteristics
                builder.setTitle("Alert")
                        .setMessage("Either you havent granted this app microphone permissions or another app is using the microphone. Kill it before starting this one.").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

// 3. Get the AlertDialog from create()
                AlertDialog dialog = builder.create();
                Intent intent = new Intent(getActivity().getApplicationContext(), ByteRecorder.class);
                if (isChecked) {
                    if(mainApplication.getMicrophoneAvailable()) {
                        getContext().startService(intent);
                        new MainApplication(getContext()).setStartOnBoot(true);
                    }else{
                        dialog.show();
                    }
                } else {
                    getContext().stopService(intent);
                    new MainApplication(getContext()).setStartOnBoot(false);
                }
                refreshViews();
            }
        });
        DeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                BytesToFile.getInstance(getContext()).wipeCache();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Are you sure?")
                        .setMessage("Pressing yes will wipe the recorded audio buffer. If you press yes now and tap the grab audio button on the main screen you will only get audio collected after you pressed this yes button.")
                        .setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });
        BufferSizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mainApplication.isServiceRunning()) {
                    Toast.makeText(getContext(), "Cannot change while background recording is active.", Toast.LENGTH_SHORT).show();
                } else {

                    final Dialog dialog = new Dialog(getContext());
                    WindowManager.LayoutParams lp = new        WindowManager.LayoutParams();
                    lp.copyFrom(dialog.getWindow().getAttributes());
                    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                    lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                    dialog.setContentView(R.layout.seekbarlayout);
                    dialog.setTitle("Set a buffer size");
                    dialog.setCancelable(true);
                    final SeekBar seekBar = (SeekBar) dialog.findViewById(R.id.seekBar);
                    final TextView BufferSieLabel = (TextView) dialog.findViewById(R.id.BufferSizeLabel);
                    final TextView OKButton = (TextView) dialog.findViewById(R.id.OKButton);
                    OKButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mainApplication.setTIME(seekBar.getProgress());
                            BufferSizeLabel.setText(mainApplication.getTIME() + " minutes");
                            if(mainApplication.getTIME() == 1){
                                BufferSizeLabel.setText(mainApplication.getTIME() + " minute");
                            }
                            //BufferSizeLabel.setText(mainApplication.getTIME(getApplicationContext()) + " minutes");
                            dialog.cancel();
                        }
                    });
                    BufferSizeLabel.setText(String.valueOf(mainApplication.getTIME() + " minutes"));
                    if(mainApplication.getTIME() == 1){
                        BufferSizeLabel.setText(mainApplication.getTIME() + " minute");
                    }
                    BufferSieLabel.setText(String.valueOf(mainApplication.getTIME() + " minutes"));
                    if(mainApplication.getTIME() == 1){
                        BufferSieLabel.setText(mainApplication.getTIME() + " minute");
                    }
                    seekBar.setProgress(mainApplication.getTIME());
                    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if (progress < 1) {
                                seekBar.setProgress(1);
                            }else if (progress > 5) {
                                seekBar.setProgress(5);
                            } else {
                                //mainApplication.setTIME(getApplicationContext(), progress);
                            }
                            //mainApplication.setAMOUNT(getApplicationContext(), mainApplication.getTIME(getApplicationContext()));
                            BufferSizeLabel.setText(String.valueOf(mainApplication.getTIME() + " minutes"));
                            if(mainApplication.getTIME() == 1){
                                BufferSizeLabel.setText(mainApplication.getTIME() + " minute");
                            }
                            BufferSieLabel.setText(String.valueOf(seekBar.getProgress() + " minutes"));
                            if(seekBar.getProgress() == 1){
                                BufferSieLabel.setText(seekBar.getProgress() + " minute");
                            }
                            refreshViews();
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });
                    dialog.getWindow().setAttributes(lp);
                    dialog.show();
                }
            }
        });

        DirectoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File currentRootDirectory = Environment.getExternalStorageDirectory();
                new ChooserDialog().with(getContext())
                        .withFilter(true, false)
                        .withStartFile( new File(mainApplication.getRecordingDirectory()).getParentFile().getAbsolutePath() )
                        .withChosenListener(new ChooserDialog.Result() {
                            @Override
                            public void onChoosePath(String path, File pathFile) {
                                File f = new File(path);
                                if(f.canWrite()) {
                                    mainApplication.setRecordingDirectory(path);
                                    pathView.setText(path);
                                } else {
                                    // no write access
                                    Toast.makeText(getContext(), "Can't write to this folder. Pick another.", Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .build()
                        .show();

            }
        });

        SampleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mainApplication.isServiceRunning()) {
                    Toast.makeText(getActivity(), "Cannot change while background recording is active.", Toast.LENGTH_SHORT).show();
                } else {
                    final Dialog dialog = new Dialog(getContext());
                    dialog.setContentView(R.layout.samplingrates);
                    dialog.setTitle("Set a sampling rate");
                    dialog.setCancelable(true);
                    final RadioGroup radioGroup = (RadioGroup) dialog.findViewById(R.id.radioGroup);
                    RadioButton r11025 = (RadioButton) dialog.findViewById(R.id.r11025);
                    RadioButton r16000 = (RadioButton) dialog.findViewById(R.id.r16000);
                    RadioButton r22050 = (RadioButton) dialog.findViewById(R.id.r22050);
                    RadioButton r8000 = (RadioButton) dialog.findViewById(R.id.r8000);
                    RadioButton r44100 = (RadioButton) dialog.findViewById(R.id.r44100);
                    Button ok_button = (Button) dialog.findViewById(R.id.ok_button);
                    Button cancel_button = (Button) dialog.findViewById(R.id.cancel_action);
                    switch (Math.round(mainApplication.getSAMPLERATE())) {
                        case 11025:
                            r11025.setChecked(true);
                            android.util.Log.d("Hudson Hughes", String.valueOf(radioGroup.getCheckedRadioButtonId()));
                            break;
                        case 16000:
                            r16000.setChecked(true);
                            android.util.Log.d("Hudson Hughes", String.valueOf(radioGroup.getCheckedRadioButtonId()));
                            break;
                        case 22050:
                            r22050.setChecked(true);
                            android.util.Log.d("Hudson Hughes", String.valueOf(radioGroup.getCheckedRadioButtonId()));
                            break;
                        case 8000:
                            r8000.setChecked(true);
                            android.util.Log.d("Hudson Hughes", String.valueOf(radioGroup.getCheckedRadioButtonId()));
                            break;
                        case 44100:
                            r44100.setChecked(true);
                            android.util.Log.d("Hudson Hughes", String.valueOf(radioGroup.getCheckedRadioButtonId()));
                            break;
                    }
                    cancel_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.cancel();
                        }
                    });
                    ok_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!mainApplication.isServiceRunning())
                                switch (radioGroup.getCheckedRadioButtonId()) {
                                    case R.id.r11025:
                                        android.util.Log.d("Hudson Hughes", String.valueOf(radioGroup.getCheckedRadioButtonId()));
                                        int bufferSize = AudioRecord.getMinBufferSize(11025, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                                        if (bufferSize > 0) {
                                            if(mainApplication.getSAMPLERATE() != 11025) if(new File(getContext().getApplicationContext().getFilesDir() + "/magic/").listFiles() != null) for (File file : new File(getContext().getApplicationContext().getFilesDir() + "/magic/").listFiles()) file.delete();
                                            mainApplication.setSAMPLERATE(11025);
                                        } else {
                                            Toast.makeText(getActivity(), "Your phone doesn't support this frequency.", Toast.LENGTH_LONG).show();
                                        }
                                        break;
                                    case R.id.r16000:
                                        android.util.Log.d("Hudson Hughes", String.valueOf(radioGroup.getCheckedRadioButtonId()));
                                        int bufferSize1 = AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                                        if (bufferSize1 > 0) {
                                            if(mainApplication.getSAMPLERATE() != 16000) if(new File(getContext().getApplicationContext().getFilesDir() + "/magic/").listFiles() != null) for (File file : new File(getContext().getApplicationContext().getFilesDir() + "/magic/").listFiles()) file.delete();
                                            mainApplication.setSAMPLERATE(16000);
                                        } else {
                                            Toast.makeText(getActivity(), "Your phone doesn't support this frequency.", Toast.LENGTH_LONG).show();
                                        }
                                        break;
                                    case R.id.r22050:
                                        android.util.Log.d("Hudson Hughes", String.valueOf(radioGroup.getCheckedRadioButtonId()));
                                        int bufferSize2 = AudioRecord.getMinBufferSize(22050, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                                        if (bufferSize2 > 0) {
                                            if(mainApplication.getSAMPLERATE() != 22050) if(new File(getContext().getApplicationContext().getFilesDir() + "/magic/").listFiles() != null) for (File file : new File(getContext().getApplicationContext().getFilesDir() + "/magic/").listFiles()) file.delete();
                                            mainApplication.setSAMPLERATE(22050);
                                        } else {
                                            Toast.makeText(getActivity(), "Your phone doesn't support this frequency.", Toast.LENGTH_LONG).show();
                                        }
                                        break;
                                    case R.id.r8000:
                                        android.util.Log.d("Hudson Hughes", String.valueOf(radioGroup.getCheckedRadioButtonId()));
                                        int bufferSize3 = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                                        if (bufferSize3 > 0) {
                                            if(mainApplication.getSAMPLERATE() != 8000) if(new File(getContext().getApplicationContext().getFilesDir() + "/magic/").listFiles() != null) for (File file : new File(getContext().getApplicationContext().getFilesDir() + "/magic/").listFiles()) file.delete();
                                            mainApplication.setSAMPLERATE(8000);
                                        } else {
                                            Toast.makeText(getActivity(), "Your phone doesn't support this frequency.", Toast.LENGTH_LONG).show();
                                        }
                                        break;
                                    case R.id.r44100:
                                        android.util.Log.d("Hudson Hughes", String.valueOf(radioGroup.getCheckedRadioButtonId()));
                                        int bufferSize4 = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                                        if (bufferSize4 > 0) {
                                            if(mainApplication.getSAMPLERATE() != 44100) if(new File(getContext().getApplicationContext().getFilesDir() + "/magic/").listFiles() != null) for (File file : new File(getContext().getApplicationContext().getFilesDir() + "/magic/").listFiles()) file.delete();
                                            mainApplication.setSAMPLERATE(44100);
                                        } else {
                                            Toast.makeText(getActivity(), "Your phone doesn't support this frequency.", Toast.LENGTH_LONG).show();
                                        }
                                        break;
                                }
                            else {
                                Toast.makeText(getActivity(), "Cannot set while background recording is active.", Toast.LENGTH_LONG).show();
                            }
                            sampleView.setText("Sampling Rate:" + String.valueOf(mainApplication.getSAMPLERATE()));
                            dialog.cancel();
                            refreshViews();
                        }
                    });

                    dialog.show();
                }
            }
        });
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {

    }

    public void refreshViews(){
        toggle.setChecked(mainApplication.isServiceRunning());
        BufferSizeLabel.setText(mainApplication.getTIME() + " minutes");
        BufferSizeLabel.setText(String.valueOf(mainApplication.getTIME() + " minutes"));
        if(mainApplication.getTIME() == 1){
            BufferSizeLabel.setText(mainApplication.getTIME() + " minute");
        }
        sampleView.setText("Sampling Rate:" + String.valueOf(mainApplication.getSAMPLERATE()));
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
    }
}
