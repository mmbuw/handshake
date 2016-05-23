package de.mobilemedia.thehandshakeapp.mobile_core;


import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;

import de.mobilemedia.thehandshakeapp.R;
import de.mobilemedia.thehandshakeapp.detection.FileOutputWriter;
import de.mobilemedia.thehandshakeapp.bluetooth.BleConnectionManager;


public class MainFragment extends Fragment {

    MainActivity parentActivity;

    private TextView mTextView;
    private Button mNewFileButton;
    private TextView mStatusTextView;
    private EditText mFileNameEditText;
    private Button mShakeButton;
    private Spinner mSpinner;

    private FileOutputWriter fileOutputWriter;

    private final Handler scanHandler = new Handler();
    private long lastMessageTimestamp = System.currentTimeMillis();
    private int messageCount = 0;


    public MainFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = (MainActivity) getActivity();
        parentActivity.setTitle(R.string.app_name);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);

        mTextView = (TextView) view.findViewById(R.id.mainTextView);
        mNewFileButton = (Button) view.findViewById(R.id.butNewFile);
        mStatusTextView = (TextView) view.findViewById(R.id.textStatus);
        mFileNameEditText = (EditText) view.findViewById(R.id.inputFileName);
        mShakeButton = (Button) view.findViewById(R.id.shakeButton);
        mSpinner = (Spinner) view.findViewById(R.id.spinner);

        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        ArrayList<File> files = getFileList(directory);
        ArrayList<String> spinnerList = new ArrayList<String>();

        for (File f : files) {
            spinnerList.add(f.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(parentActivity,
                                                               android.R.layout.simple_spinner_item,
                                                               spinnerList);
        mSpinner.setAdapter(adapter);


        fileOutputWriter = null;

        mNewFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String filename = mFileNameEditText.getText().toString();
                filename = createNewFileWriters(filename);
                mFileNameEditText.setText("");
                mStatusTextView.setText("Current file has name " + filename);
            }
        });

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
                String filename = adapterView.getItemAtPosition(pos).toString();
                parentActivity.loadNewTrainingFile(path + "/" + filename);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mShakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onScanButtonClick();
            }
        });

        return view;
    }

    public void onScanButtonClick() {

        final BleConnectionManager bleConnectionManager = parentActivity.getBleConnectionManager();

        if (!bleConnectionManager.isScanActive()) {
            bleConnectionManager.startBle();
            scanHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bleConnectionManager.stopBle();
                }
            }, bleConnectionManager.SCAN_PERIOD);
        } else {
            bleConnectionManager.stopBle();
        }
    }

    public String createNewFileWriters(String filename) {

        int unixTime = getCurrentUnixTimestamp();

        if (fileOutputWriter != null) {
            fileOutputWriter.closeStream();
        }

        if (filename.isEmpty()) {
            filename = "watch-" + unixTime + ".txt";
        }
        else {
            if (!filename.endsWith(".txt")) { filename += ".txt"; }
        }

        fileOutputWriter = new FileOutputWriter(filename);
        return filename;
    }

    public void processReceivedValues(float[] receivedValues) {

        /* Write data to current file if present */
        if (fileOutputWriter != null) {
            fileOutputWriter.writeToFile(receivedValues[0] + ", " +
                    receivedValues[1] + ", " +
                    receivedValues[2]);
        }

        /* Update FPS display */
        if (messageCount == 10) {
            long nowTime = System.currentTimeMillis();
            long diffTime = nowTime - lastMessageTimestamp;
            double diffSeconds = diffTime / 1000.0;
            mTextView.setText(10.0 / diffSeconds + "");
            messageCount = 0;
            lastMessageTimestamp = nowTime;
        } else {
            ++messageCount;
        }

    }

    private ArrayList<File> getFileList(File parentDirectory) {

        ArrayList<File> inFiles = new ArrayList<File>();
        File[] files = parentDirectory.listFiles();

        for (File file : files) {
            if (file.isDirectory()) {
                inFiles.addAll(getFileList(file));
            } else {
                if (file.getName().endsWith(".model")) {
                    inFiles.add(file);
                }
            }
        }

        return inFiles;

    }

    private int getCurrentUnixTimestamp() {
        return (int) (System.currentTimeMillis() / 1000L);
    }

}
