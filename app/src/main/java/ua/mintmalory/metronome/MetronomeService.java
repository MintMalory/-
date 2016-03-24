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
    public static final String INDICATOR_STATE_EXTRA = MetronomeService.class + ".indicator";
    public static final String BROADCAST_ACTION = MetronomeService.class + ".broadcast";

    private Timer timer;
    private TimerTask oneIteration;
    private Camera camera;
    private Intent intent;
    private Handler handler;

    private boolean cameraIsAvailable;
    private boolean vibroOn = false;
    private boolean flashOn = false;
    private boolean soundOn = false;



    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        intent = new Intent(BROADCAST_ACTION);
        timer = new Timer("MetronomeTimer", true);

        handler = new Handler();
        cameraIsAvailable = getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)
                && getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (cameraIsAvailable && flashOn) {
            camera = Camera.open();
        }

        oneIteration = new TimerTask() {
            private ToneGenerator tickSound = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
            private Vibrator vibrator = (Vibrator) getSystemService(getApplicationContext().VIBRATOR_SERVICE);
            private Camera.Parameters p;

            @Override
            public void run() {
                intent.putExtra(INDICATOR_STATE_EXTRA, true);
                sendBroadcast(intent);

                if (cameraIsAvailable && flashOn) {
                    camera = Camera.open();
                    p = camera.getParameters();
					p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
					camera.setParameters(p);
                }

                if (soundOn) {
                    tickSound.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 100);
                }

                if (vibroOn) {
                    vibrator.vibrate(100);
                }

                if (cameraIsAvailable && flashOn) {
                    camera = Camera.open();
                    p = camera.getParameters();
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    camera.setParameters(p);
                }

                intent.putExtra(INDICATOR_STATE_EXTRA, false);
                sendBroadcast(intent);
            }
        };

        handler.removeCallbacks(oneIteration);
        handler.postAtFrontOfQueue(oneIteration);
    }

    @Override
    public void onDestroy() {
        timer.cancel();
        timer.purge();

        if (camera!=null) {
            camera.release();
        }

        handler.removeCallbacks(oneIteration);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        vibroOn = intent.getBooleanExtra(MainActivity.VIBRATION_EXTRA, false);
        flashOn = intent.getBooleanExtra(MainActivity.FLASH_EXTRA, false);
        soundOn = intent.getBooleanExtra(MainActivity.SOUND_EXTRA, false);

        int bpm = intent.getIntExtra(MainActivity.BPM_EXTRA, 100);

        timer.schedule(oneIteration, new Date(), 60000 / bpm);

        return super.onStartCommand(intent, flags, startId);
    }
}
