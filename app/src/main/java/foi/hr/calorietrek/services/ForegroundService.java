package foi.hr.calorietrek.services;

/**
 * Created by juras on 2017-11-30.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import foi.hr.calorietrek.R;
import foi.hr.calorietrek.constants.Constants;
import foi.hr.calorietrek.ui.training.view.TrainingActivity;

public class ForegroundService extends Service {

    private Handler trainingHandler = new Handler();
    private Intent intent;
    private long startTime = 0L;
    private long timeInMilliseconds = 0L;
    private long pausedTime = 0L;
    private long updateTime = 0L;
    private boolean stopTimer = false;

    @Override
    public void onCreate()
    {
        super.onCreate();
        intent = new Intent(Constants.ACTION.BROADCAST_ACTION);

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {

            startTime = SystemClock.uptimeMillis();
            trainingHandler.postDelayed(updateTimerThread, 0);

            Intent notificationIntent = new Intent(this, TrainingActivity.class);

            notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);

            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.cklogo);

            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(getString(R.string.training_started))
                    .setSmallIcon(R.drawable.cklogo)
                    .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .build();

            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);

        } else if (intent.getAction().equals(Constants.ACTION.PLAY_ACTION)) {
            startTime = SystemClock.uptimeMillis();
            trainingHandler.postDelayed(updateTimerThread, 0);
        } else if (intent.getAction().equals(Constants.ACTION.PAUSE_ACTION)) {
            pausedTime += timeInMilliseconds;
            trainingHandler.removeCallbacks(updateTimerThread);
        } else if (intent.getAction().equals(Constants.ACTION.STOPFOREGROUND_ACTION)) {
            timeInMilliseconds = 0L;
            startTime = 0L;
            pausedTime = 0L;
            updateTime = 0L;

            stopTimer = true;
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }

    private Runnable updateTimerThread = new Runnable()
    {
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updateTime = pausedTime + timeInMilliseconds;
            intent.putExtra("timeInMilliseconds", updateTime);
            sendBroadcast(intent);

            if (!stopTimer) {
                trainingHandler.postDelayed(this, 0);
            }
        }
    };

    @Override
    public void onDestroy()
    {
        trainingHandler.removeCallbacks(updateTimerThread);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
}