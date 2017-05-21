package ru.bokhonin.montana;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;


public class MainActivity extends Activity {

    private Button mStartButton;
    private Button mStopButton;
    private Switch mVibrationSwitch;
    private Switch mSoundSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mStartButton = (Button)findViewById(R.id.startButton);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Service is STARTED", Toast.LENGTH_LONG).show();
                MontanaService.setServiceAlarm(MainActivity.this, true);
                SetCurrentStatusService();
                SaveAllPreferences();
            }
        });

        mStopButton = (Button)findViewById(R.id.stopButton);
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Service is FINISHED", Toast.LENGTH_LONG).show();
                MontanaService.setServiceAlarm(MainActivity.this, false);
                SetCurrentStatusService();
                SaveAllPreferences();
            }
        });

        mVibrationSwitch = (Switch)findViewById(R.id.vibrationSwitch);
        mVibrationSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetStatusButtonStart();
                ChangeDescriptionButtonStart();
            }
        });

        mSoundSwitch = (Switch)findViewById(R.id.soundSwitch);
        mSoundSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetStatusButtonStart();
                ChangeDescriptionButtonStart();
            }
        });

        SetCurrentStatusService();
        LoadPreferences();
        SaveAllPreferences();
        SetStatusButtonStart();

    }

    private void ChangeDescriptionButtonStart() {
        if (MontanaService.isServiceAlarmOn(MainActivity.this)) {
            mStartButton.setText("RESTART");
        }
    }

    private void SetStatusButtonStart() {
        if (!mVibrationSwitch.isChecked() && !mSoundSwitch.isChecked()) {
            mStartButton.setEnabled(false);
        } else {
            mStartButton.setEnabled(true);
        }
    }

    private void SetCurrentStatusService() {
        boolean serviceStarted = MontanaService.isServiceAlarmOn(MainActivity.this);

        TextView mCurrentStatusService = (TextView)findViewById(R.id.currentStatusServiceTextView);

        String text = serviceStarted ? "Service is started" : "Service is not started";
        mCurrentStatusService.setText(text);
    }

    private void SavePreferences(String key, boolean value) {
        SharedPreferences sharedPreferences = getSharedPreferences("montana_preferences" , 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(key, value);
        editor.apply();
    }

    private void SaveAllPreferences() {
        SavePreferences("mVibrationSwitch", mVibrationSwitch.isChecked());
        SavePreferences("mSoundSwitch", mSoundSwitch.isChecked());
    }

    private void LoadPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("montana_preferences", 0);

        boolean savedVibrationSwitch = sharedPreferences.getBoolean("mVibrationSwitch", true);
        mVibrationSwitch.setChecked(savedVibrationSwitch);

        boolean savedSoundSwitch = sharedPreferences.getBoolean("mSoundSwitch", false);
        mSoundSwitch.setChecked(savedSoundSwitch);
    }

 }
