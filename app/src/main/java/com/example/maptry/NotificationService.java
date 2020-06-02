package com.example.maptry;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class NotificationService extends IntentService {

    Timer timer;
    TimerTask timerTask;
    String TAG = "Timers";
    int Your_X_SECS = 5;
    //we are going to use a handler to be able to run in our TimerTask
    final Handler handler = new Handler();
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     *
     */
    public NotificationService() {
        super("Notification Service");
    }

//
//    @Override
//    public IBinder onBind(Intent arg0) {
//        return null;
//    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        super.onStartCommand(intent, flags, startId);

        return START_STICKY;
    }

    @Override
    protected void onHandleIntent(Intent intent){
        Log.e(TAG, "onHandleIntent");
        timer = new Timer();

        //initialize the TimerTask's job
//        initializeTimerTask();

        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms



            timerTask = new TimerTask() {
                public void run() {

                    //use a handler to run a toast that shows the current timestamp
                    handler.post(new Runnable() {
                        public void run() {
                            if (intent == null) {
                                for (int i = 0; i < 50; i++) {
                                    Log.d("timer", "porc " + i);
                                    try {
                                        Thread.sleep(1000);
                                    } catch (Exception e) {

                                    }
                                }

                                //TODO CALL NOTIFICATION FUNC
                                Log.d("NOTIFIY", "Closed");

                            }
                            else{
                                for(int i = 0; i<50;i++){
                                    Log.d("timer","ngialost " + i);
                                    try{
                                        Thread.sleep(1000);
                                    }
                                    catch (Exception e){

                                    }
                                }
                                //TODO CALL NOTIFICATION FUNC
                                Log.d("NOTIFIY", "HELLOOOOOO");
                            }
                        }

                    });
                }
            };
        timer.schedule(timerTask, 500, Your_X_SECS * 100); //
    }
        //timer.schedule(timerTask, 5000,1000); //


    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate");


    }

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
//        initializeTimerTask();

        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timer.schedule(timerTask, 5000, Your_X_SECS * 1000); //
        //timer.schedule(timerTask, 5000,1000); //
    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void initializeTimerTask() {

            timerTask = new TimerTask() {
                public void run() {

                    //use a handler to run a toast that shows the current timestamp
                    handler.post(new Runnable() {
                        public void run() {

                            //TODO CALL NOTIFICATION FUNC
                            Log.d("NOTIFIY", "Closed");

                        }
                    });
                }
            };
            timerTask = new TimerTask() {
                public void run() {

                    //use a handler to run a toast that shows the current timestamp
                    handler.post(new Runnable() {
                        public void run() {

                            //TODO CALL NOTIFICATION FUNC
                            Log.d("NOTIFIY", "HELLOOOOOO");

                        }
                    });
                }
            };
        }
}