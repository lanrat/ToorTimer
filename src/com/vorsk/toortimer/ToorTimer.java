package com.vorsk.toortimer;

import java.util.Timer;
import java.util.TimerTask;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;

public class ToorTimer {
	/** All possible timer states */
	private final static int RUNNING=0, STOPPED=1, PAUSED=2;
	
	/** The current timer time in milliseconds*/
	private static int mTime = 0;
	private static int mTimeOrig = 0;
	
	/** Internal increment class for the timer */
	private static Timer mTimer = null;
	
	/** The timer's current state */
	private static int mCurrentState = -1;
	
	/** Should the logs be shown */
	private final static boolean LOG = true;
	
	/** Update rate of the internal timer in milliseconds */
	private static final int TIMER_TIC = 100;
	
	/** debug string */
	private final static String TAG = "ToorTimer";
	
	private static AlarmManager mAlarmMgr;
	
	//private static WakeLock mWakeLock;
	
	private static PendingIntent mPendingIntent;
	
	public static boolean timerDone;
	
	/** Handler for the message from the timer service */
	private static Handler mHandler = new Handler() {
		
		@Override
        public void handleMessage(Message msg) {
			
			// The timer is finished
			if(msg.arg1 <= 0){
				
				if(mTimer != null){
					if(LOG) Log.v(TAG,"rcvd a <0 msg = " + msg.arg1);
					
					timerDone();

					
				}
				
			// Update the time
			}else{
				mTime = msg.arg1;
				
				//enterState(RUNNING);
				onUpdateTime();
			}
		}
    };
    
    private static void timerDone() {
    	TimerActivity.BlinkTimer(true);
    	timerDone = true;
    	
		if (mTimer != null) {
			mTimer.cancel();
		}
    }
    
    public static boolean isRunning() {
    	return mCurrentState == RUNNING;
    }
    public static boolean isPaused() {
    	return mCurrentState == PAUSED;
    }
	
	/** 
	 * This only refers to the visual state of the application, used to manage
	 * the view coming back into focus.
	 * 
	 * @param state the visual state that is being entered
	 */
	private static void enterState(int state){
		Button start_button = (Button) TimerActivity.context.findViewById(R.id.start_button);
		
		if(mCurrentState != state){
			
			mCurrentState = state;		
			if(LOG) Log.v(TAG,"Set current state = " + mCurrentState);
			
			switch(state)
			{
				case RUNNING:
				{
					start_button.setText(R.string.start_button_stop);
				}break;
		
				case STOPPED:
				{	
					start_button.setText(R.string.start_button_start);
					clearTime();
				
				}break;
		
				case PAUSED:
				{
					start_button.setText(R.string.start_button_start);
				}break;	
			}
		}
	}
	
	/**
	 * Cancels the alarm portion of the timer
	 */
	private static void stopAlarmTimer(){
		if(LOG) Log.v(TAG,"Stopping the alarm timer ...");
		if (mAlarmMgr != null) {
			mAlarmMgr.cancel(mPendingIntent);
		}
	}
	
	/**
	 * Stops the timer
	 */
	public static void timerStop()
	{		
		if(LOG) Log.v(TAG,"Timer stopped");
		
		timerDone = false;
		
		clearTime();
		
		// Stop our timer service
		enterState(STOPPED);
		if (mTimer != null) {
			mTimer.cancel();
		}
		
		releaseWakeLock();
	}
	
	private static void releaseWakeLock(){
		// Remove the wakelock
		
		/*if(mWakeLock != null && mWakeLock.isHeld()) {
			if(LOG) Log.v(TAG,"Releasing wake lock...");
			mWakeLock.release();
			mWakeLock = null;
		}*/
		
		TimerActivity.context.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

	}
	/**
	 * Acquires the wake lock 
	 */
	private static void aquireWakeLock(){
		// We're going to start a wakelock
		
		if(LOG) Log.v(TAG,"Issuing a wakelock...");
		
		TimerActivity.context.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		/*PowerManager pm = (PowerManager)TimerActivity.context.getSystemService(Context.POWER_SERVICE);
		if(mWakeLock != null) Log.e(TAG,"There's already a wakelock... Shouldn't be there!");
		
		mWakeLock= pm.newWakeLock(
			PowerManager. SCREEN_DIM_WAKE_LOCK
            | PowerManager.ON_AFTER_RELEASE,
            TAG);
		mWakeLock.acquire();*/
	
	}
	
	public static int getProgress() {
		return (int)((((float)mTime)/((float)mTimeOrig))*100);
	}
	
	/**
	 * Starts the timer at the given time
	 * @param time with which to count down
	 * @param service whether or not to start the service as well
	 */
	public static void timerStart(int time)
	{
		timerDone = false;
		if(LOG) Log.v(TAG,"Starting the timer...");
		
		// Star external service
		enterState(RUNNING);
		
		// Internal thread to properly update the GUI
		mTimer = new Timer();
		mTimeOrig = time;
		mTime = time;
		mTimer.scheduleAtFixedRate( new TimerTask(){
	        	public void run() {
	          		timerTic();
	        	}
	      	},
	      	0,
	      	TIMER_TIC);
		
		aquireWakeLock();
	}
	
	/** Resume the time after being paused */
	public static void resumeTimer() 
	{
		if(LOG) Log.v(TAG,"Resuming the timer...");
			
		timerStart(mTime);
		enterState(RUNNING);
	}
	
	/** Pause the timer and stop the timer service */
	public static void pauseTimer()
	{
		if(LOG) Log.v(TAG,"Pausing the timer...");
		
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
		
		stopAlarmTimer();
		
		enterState(PAUSED);
	}
	
	
	/** Called whenever the internal timer is updated */
	protected static void timerTic() 
	{
		mTime -= TIMER_TIC;
		
		if(mHandler != null){
			Message msg = new Message();
			msg.arg1 = mTime;			
			mHandler.sendMessage(msg);
		}
	}
	
	/** Clears the time, sets the image and label to zero */
	private static void clearTime()
	{
		TimerActivity.BlinkTimer(false);
		mTime = TimerActivity.getMSTime();
		onUpdateTime();
		TimerActivity.setTimerColor(TimerActivity.textNormalColor);
	}
	
	
    /**
     * Updates the time 
     */
	private static void onUpdateTime(){
		int seconds = (int) (mTime / 1000) % 60 ;
		int minutes = (int) ((mTime / (1000*60)) % 60);
		int hours   = (int) ((mTime / (1000*60*60)) % 24);
		
		int progress = getProgress();
		if (progress > 20) {
			TimerActivity.setTimerColor(TimerActivity.textNormalColor);
		} else if (progress > 10) {
			TimerActivity.setTimerColor(TimerActivity.textNotifyColor);
		} else if (progress > 2) {
			TimerActivity.setTimerColor(TimerActivity.textWarnColor);
		} else {
			TimerActivity.setTimerColor(TimerActivity.textCriticalColor);
		}
		
		TimerActivity.updateTimerText(hours, minutes, seconds);
    }
	
	
}
