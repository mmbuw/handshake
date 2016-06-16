package de.mobilemedia.thehandshakeapp.mobile_core;


import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import de.mobilemedia.thehandshakeapp.R;
import de.mobilemedia.thehandshakeapp.bluetooth.BTLEConnectionManager;
import de.mobilemedia.thehandshakeapp.detection.FileOutputWriter;
import de.mobilemedia.thehandshakeapp.bluetooth.Util;


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

        mShakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onScanButtonClick();
            }
        });

        return view;
    }

    public void onScanButtonClick() {

        final BTLEConnectionManager bleConnectionManager = parentActivity.getBleConnectionManager();
        bleConnectionManager.setButtonToGreyOut(mShakeButton);
        bleConnectionManager.scanBTLE(true);
        bleConnectionManager.advertiseBTLE(true);
    }

    public String createNewFileWriters(String filename) {

        int unixTime = Util.getCurrentUnixTimestamp();

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

            if (receivedValues.length == 3) {
                fileOutputWriter.writeToFile(receivedValues[0] + ", " +
                        receivedValues[1] + ", " +
                        receivedValues[2]);
            }
            else if (receivedValues.length == 6) {
                fileOutputWriter.writeToFile(receivedValues[0] + ", " +
                        receivedValues[1] + ", " +
                        receivedValues[2] + ", " +
                        receivedValues[3] + ", " +
                        receivedValues[4] + ", " +
                        receivedValues[5]);
            }
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

}
