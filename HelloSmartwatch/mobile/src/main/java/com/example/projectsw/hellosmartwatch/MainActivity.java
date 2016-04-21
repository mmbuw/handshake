package com.example.projectsw.hellosmartwatch;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.mainTextView);

        Intent intent = getIntent();
        byte[] intentData = intent.getByteArrayExtra("TEST_MESSAGE");

        if (intentData != null)
            mTextView.setText(intentData[0] +  ", " + intentData[1] + ", " + intentData[2]);
    }
}
