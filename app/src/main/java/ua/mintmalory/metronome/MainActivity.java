package ua.mintmalory.metronome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ToggleButton;


public class MainActivity extends AppCompatActivity {
    public static final String VIBRATION_EXTRA = MainActivity.class + ".vibration";
    public static final String FLASH_EXTRA = MainActivity.class + ".flash";
    public static final String SOUND_EXTRA = MainActivity.class + ".sound";
    public static final String BPM_EXTRA = MainActivity.class + ".bpm";
    public static final String START_STOP_EXTRA = MainActivity.class + ".start";

    private static final String TAG = MainActivity.class.toString();

    private ToggleButton mVibrationToggleButton;
    private ToggleButton mFlashToggleButton;
    private ToggleButton mSoundToggleButton;

    private ToggleButton mStartStopToggleButton;
    private SeekBar mBmpSeekBar;
    private EditText mBmpEditText;
    private ImageView mIndicatorImageView;
    private BroadcastReceiver mMetronomeServiceReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initBroadcastReceiver();
        initManualModeButtonsListeners();
        initBpmChangersListeners();
        initStartBtnListener();
    }

    private void initViews() {
        mVibrationToggleButton = (ToggleButton) findViewById(R.id.vibro_btn);
        mFlashToggleButton = (ToggleButton) findViewById(R.id.flash_btn);
        mSoundToggleButton = (ToggleButton) findViewById(R.id.sound_btn);
        mStartStopToggleButton = (ToggleButton) findViewById(R.id.start_stop_button);
        mBmpSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mBmpEditText = (EditText) findViewById(R.id.bpm_editText);
        mIndicatorImageView = (ImageView) findViewById(R.id.indicator_imgView);
    }

    private void initBroadcastReceiver() {
        mMetronomeServiceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean indicatorIsOn = intent.getBooleanExtra(MetronomeService.INDICATOR_STATE_EXTRA, false);

                if (indicatorIsOn) {
                    mIndicatorImageView.setBackgroundDrawable(getResources().getDrawable(R.drawable.indicator_on));
                } else {
                    mIndicatorImageView.setBackgroundDrawable(getResources().getDrawable(R.drawable.indicator_off));
                }
            }
        };

        registerReceiver(mMetronomeServiceReceiver, new IntentFilter(MetronomeService.BROADCAST_ACTION));
    }

    private void initManualModeButtonsListeners() {
        mVibrationToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateService();
            }
        });

        mFlashToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateService();
            }
        });

        mSoundToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateService();
            }
        });
    }

    private void initBpmChangersListeners() {
        mBmpSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mBmpEditText.setText(Integer.toString(seekBar.getProgress()));

                updateService();
            }
        });

        mBmpEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int i = Integer.parseInt(s.toString());

                    if (i > 208) {
                        i = 208;

                        mBmpEditText.setText(Integer.toString(i));
                    }

                    mBmpSeekBar.setProgress(i);
                    updateService();
                } catch (NumberFormatException e) {
                    Log.d(TAG, e.getMessage());
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Only integer can be set as the bmp value!", Toast.LENGTH_LONG);
                    toast.show();

                    mBmpEditText.setText(Integer.toString(mBmpSeekBar.getProgress()));
                }
            }
        });
    }

    private void initStartBtnListener() {
        mStartStopToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateService();
            }
        });
    }

    private void updateService() {
        Intent intent = new Intent(this, MetronomeService.class);
        stopService(intent);

        if ((mStartStopToggleButton.isChecked()) && (mBmpSeekBar.getProgress() > 0)) {
            intent.putExtra(VIBRATION_EXTRA, mVibrationToggleButton.isChecked());
            intent.putExtra(FLASH_EXTRA, mFlashToggleButton.isChecked());
            intent.putExtra(SOUND_EXTRA, mSoundToggleButton.isChecked());
            intent.putExtra(BPM_EXTRA, mBmpSeekBar.getProgress());

            startService(intent);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(VIBRATION_EXTRA, mVibrationToggleButton.isChecked());
        outState.putBoolean(FLASH_EXTRA, mFlashToggleButton.isChecked());
        outState.putBoolean(SOUND_EXTRA, mSoundToggleButton.isChecked());
        outState.putInt(BPM_EXTRA, mBmpSeekBar.getProgress());
        outState.putBoolean(START_STOP_EXTRA, mSoundToggleButton.isChecked());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mVibrationToggleButton.setChecked(savedInstanceState.getBoolean(VIBRATION_EXTRA));
        mFlashToggleButton.setChecked(savedInstanceState.getBoolean(FLASH_EXTRA));
        mSoundToggleButton.setChecked(savedInstanceState.getBoolean(SOUND_EXTRA));
        mStartStopToggleButton.setChecked(savedInstanceState.getBoolean(START_STOP_EXTRA));

        int bpm = savedInstanceState.getInt(BPM_EXTRA);
        mBmpEditText.setText(Integer.toString(bpm));
        mBmpSeekBar.setProgress(bpm);
    }

    @Override
    public void onBackPressed() {
        stopService(new Intent(this, MetronomeService.class));
        unregisterReceiver(mMetronomeServiceReceiver);
        super.onBackPressed();
    }
}
