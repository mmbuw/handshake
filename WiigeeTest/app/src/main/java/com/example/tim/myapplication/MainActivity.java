package com.example.tim.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.wiigee.control.AndroidWiigee;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initWiiGee();
    }

    public void initWiiGee() {
        AndroidWiigee wiigee = new AndroidWiigee(this);
    }
}
