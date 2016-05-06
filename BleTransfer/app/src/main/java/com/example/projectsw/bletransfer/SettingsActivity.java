package com.example.projectsw.bletransfer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    Button submitSettingsButton;
    EditText urlField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        submitSettingsButton = (Button) findViewById(R.id.submitSettingsButton);
        urlField = (EditText) findViewById(R.id.urlField);

        urlField.setText(MainActivity.msg);

        submitSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newUrl = urlField.getText().toString();
                try{
                    MessageData messageData = new MessageData(newUrl, true);
                } catch (Exception e) {
                    Log.e("InvalidURL", "Couldn't convert URL "+newUrl);
                    Toast.makeText(getApplicationContext(), "Couldn't convert URL", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }
}
