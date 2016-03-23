package ua.mintmalory.metronome;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MetronomeService extends Service {
    public static final String VIBRATION_EXTRA = MetronomeService.class + ".vibration";
    public static final String FLASH_EXTRA = MetronomeService.class + ".flash";
    public static final String SOUND_EXTRA = MetronomeService.class + ".sound";
    public static final String BPM_EXTRA = MetronomeService.class + ".bpm";

    private Timer timer;
    private TimerTask tone; //rename
    public static final String BROADCAST_ACTION = "ua.mintmalory.metronome";
    private Intent intent;
    private Handler handler;

    private boolean CAMERA_IS_AVALIBLE;
    private boolean vibroOn = false;
    private boolean flashOn = false;
    private boolean soundOn = false;

    private Camera camera;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        intent = new Intent(BROADCAST_ACTION);
        timer = new Timer("MetronomeTimer", true);

        handler = new Handler();
        CAMERA_IS_AVALIBLE = getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)
                && getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

            if (CAMERA_IS_AVALIBLE) {
            camera = Camera.open();
            Camera.Parameters p = camera.getParameters();
            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(p);
        }

        tone = new TimerTask() {
            private ToneGenerator tick = new ToneGenerator(AudioManager.STREAM_MUSIC, 100); //tickSound
            private Vibrator v = (Vibrator) getSystemService(getApplicationContext().VIBRATOR_SERVICE); //vibrator

            @Override
            public void run() {
                    intent.putExtra("state", true);

                sendBroadcast(intent);

                if (CAMERA_IS_AVALIBLE && flashOn) {
                    camera.startPreview();
                }



                if (soundOn) {
                    tick.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 100);
                }


                if (vibroOn) {
                    v.vibrate(100);
                }



                if (CAMERA_IS_AVALIBLE && flashOn) {
                    camera.stopPreview();
                    camera.release();
                }


                intent.putExtra("state", false);
                sendBroadcast(intent);
            }
        };

        handler.removeCallbacks(tone);
        handler.postAtFrontOfQueue(tone);
    }

    @Override
    public void onDestroy() {
        timer.cancel();
        timer.purge();
        handler.removeCallbacks(tone);

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        vibroOn = intent.getBooleanExtra(VIBRATION_EXTRA, false);
        flashOn = intent.getBooleanExtra(FLASH_EXTRA, false);
        soundOn = intent.getBooleanExtra(SOUND_EXTRA, false);

        int bpm = intent.getIntExtra(BPM_EXTRA, 100);

        timer.schedule(tone, new Date(), 60000 / bpm);

        return super.onStartCommand(intent, flags, startId);
    }
}
