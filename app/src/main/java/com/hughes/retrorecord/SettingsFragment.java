package com.hughes.retrorecord;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.hughes.retrorecord.messages.MessageEvent;
import com.turhanoz.android.reactivedirectorychooser.event.OnDirectoryCancelEvent;
import com.turhanoz.android.reactivedirectorychooser.event.OnDirectoryChosenEvent;
import com.turhanoz.android.reactivedirectorychooser.ui.DirectoryChooserFragment;
import com.turhanoz.android.reactivedirectorychooser.ui.OnDirectoryChooserFragmentInteraction;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment implements
        OnDirectoryChooserFragmentInteraction {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

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
    TextView pathView;
    TextView sampleView;
    MainApplication mainApplication;
    @Override
    public void onEvent(OnDirectoryChosenEvent event) {
        File directoryChosenByUser = event.getFile();
        mainApplication.setRecordingDirectory(event.getFile().getPath());
        pathView.setText(mainApplication.getRecordingDirectory());
    }

    @Override
    public void onEvent(OnDirectoryCancelEvent onDirectoryCancelEvent) {

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
        refreshh();
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

// 2. Chain together various setter methods to set the dialog characteristics
                builder.setTitle("Alert")
                        .setMessage("Another app is using the microphone. Kill it before starting this one.").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

// 3. Get the AlertDialog from create()
                AlertDialog dialog = builder.create();
                Intent intent = new Intent(getActivity().getApplicationContext(), ByteRecorder.class);
                SharedPreferences settings = getContext().getSharedPreferences("AppOn", 0);
                if (isChecked) {
                    if(mainApplication.getMicrophoneAvailable()) {
                        getContext().startService(intent);
                        settings.edit().putBoolean("AppOn", true).commit();
                        Log.d("Hudson Hughes", String.valueOf(settings.getBoolean("AppOn", false)));
                    }else{
                        dialog.show();
                    }
                } else {
                    getContext().stopService(intent);
                    settings.edit().putBoolean("AppOn", false).commit();
                }
                refreshh();
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
                                try {
                                    for (File file : new File(getContext().getApplicationContext().getFilesDir() + "/magic/").listFiles()) file.delete();
                                } catch (Exception e) {

                                }
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
                            //BufferSizeLabel.setText(mainApplication.getTIME(getApplicationContext()) + " minutes");
                            dialog.cancel();
                        }
                    });
                    BufferSizeLabel.setText(String.valueOf(mainApplication.getTIME() + " minutes"));
                    BufferSieLabel.setText(String.valueOf(mainApplication.getTIME() + " minutes"));
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
                            BufferSieLabel.setText(seekBar.getProgress() + " minutes");
                            BufferSizeLabel.setText(mainApplication.getTIME() + " minutes");
                            refreshh();
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });
                    dialog.show();
                }
            }
        });

        DirectoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File currentRootDirectory = Environment.getExternalStorageDirectory();
                DirectoryChooserFragment directoryChooserFragment = DirectoryChooserFragment.newInstance(currentRootDirectory);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                directoryChooserFragment.show(transaction, "RDC");
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
                            refreshh();
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
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    public void refreshh(){
        toggle.setChecked(mainApplication.isServiceRunning());
        BufferSizeLabel.setText(mainApplication.getTIME() + " minutes");
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
