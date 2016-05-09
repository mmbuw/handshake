package de.mobilemedia.thehandshakeapp.mobile_core;


import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import de.mobilemedia.thehandshakeapp.R;


public class MainFragment extends Fragment {

    private TextView mTextView;
    private Button mNewFileButton;
    private TextView mStatusTextView;
    private EditText mFileNameEditText;
    private Button mShakeButton;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);

        mTextView = (TextView) view.findViewById(R.id.textView);
        mNewFileButton = (Button) view.findViewById(R.id.butNewFile);
        mStatusTextView = (TextView) view.findViewById(R.id.textStatus);
        mFileNameEditText = (EditText) view.findViewById(R.id.inputFileName);
        mShakeButton = (Button) view.findViewById(R.id.shakeButton);

        mNewFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String filename = mFileNameEditText.getText().toString();
                filename = ((MainActivity) getActivity()).createNewFileWriters(filename);
                mFileNameEditText.setText("");
                mStatusTextView.setText("Current file has name " + filename);
            }
        });

        mShakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).onScanButtonClick();
            }
        });

        return view;
    }

    public void updateTextView(String text) {
        mTextView.setText(text);
    }

}
