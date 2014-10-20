package com.vorsk.toortimer;

import com.vorsk.toortimer.util.AutoResizeTextView;
import com.vorsk.toortimer.util.SystemUiHider;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TimePicker;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class TimerActivity extends Activity {
	
	public static Activity context;
	public static AutoResizeTextView timeText;
	
	public static final int textNormalColor = Color.parseColor("#F7F7F7"); //white
	public static final int textNotifyColor = Color.parseColor("#FEFE00"); //yellow
	public static final int textWarnColor = Color.parseColor("#FE9800"); //orange
	public static final int textCriticalColor = Color.parseColor("#FE0000"); // red
	
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TimerActivity.context = this;
		
		setContentView(R.layout.activity_timer);
		
		//rotate logo
		ImageView imViewAndroid = (ImageView) findViewById(R.id.background_logo);
        imViewAndroid.setImageBitmap(rotateImage(BitmapFactory.decodeResource(getResources(), R.drawable.toorcon_logo),-45));
		
		// set font
		Typeface tf = Typeface.createFromAsset(getAssets(),"fonts/digital-7.ttf");  
		timeText = (AutoResizeTextView) findViewById(R.id.time_text);
		timeText.setTypeface(tf);
		updateTimerText(HOURS, MINUTES, 0);

		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		final View contentView = timeText;

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider
				.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
					// Cached values.
					int mControlsHeight;
					int mShortAnimTime;

					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
							// If the ViewPropertyAnimator API is available
							// (Honeycomb MR2 and later), use it to animate the
							// in-layout UI controls at the bottom of the
							// screen.
							if (mControlsHeight == 0) {
								mControlsHeight = controlsView.getHeight();
							}
							if (mShortAnimTime == 0) {
								mShortAnimTime = getResources().getInteger(
										android.R.integer.config_shortAnimTime);
							}
							controlsView
									.animate()
									.translationY(visible ? 0 : mControlsHeight)
									.setDuration(mShortAnimTime);
						} else {
							// If the ViewPropertyAnimator APIs aren't
							// available, simply show or hide the in-layout UI
							// controls.
							controlsView.setVisibility(visible ? View.VISIBLE
									: View.GONE);
						}

						if (visible && AUTO_HIDE) {
							// Schedule a hide().
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
						}
					}
				});

		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					mSystemUiHider.show();
				}
			}
		});

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		Button time_button2 = (Button) findViewById(R.id.time_button2);
		time_button2.setOnTouchListener(mDelayHideTouchListener);
		time_button2.setOnClickListener(new View.OnClickListener() {	 
			@Override
			public void onClick(View v) {
				ToorTimer.pauseTimer();
				showTimeDialog();
			}
		});
		
		Button time_button1 = (Button) findViewById(R.id.time_button1);
		time_button1.setOnTouchListener(mDelayHideTouchListener);
		time_button1.setOnClickListener(new View.OnClickListener() {	 
			@Override
			public void onClick(View v) {
				ToorTimer.timerStop();
				setTimerColor(textNormalColor);
				HOURS = 0;
				MINUTES = 50;
				updateTimerText(HOURS, MINUTES, 0);
			}
		});
		
		Button time_button0 = (Button) findViewById(R.id.time_button0);
		time_button0.setOnTouchListener(mDelayHideTouchListener);
		time_button0.setOnClickListener(new View.OnClickListener() {	 
			@Override
			public void onClick(View v) {
				ToorTimer.timerStop();
				setTimerColor(textNormalColor);
				HOURS = 0;
				MINUTES = 20;
				updateTimerText(HOURS, MINUTES, 0);
			}
		});
		
		/*Button reset_button = (Button) findViewById(R.id.reset_button);
		reset_button.setOnTouchListener(mDelayHideTouchListener);
		reset_button.setOnClickListener(new View.OnClickListener() {	 
			@Override
			public void onClick(View v) {
				ToorTimer.timerStop();
				setTimerColor(textNormalColor);
			}
		});*/
		
		Button start_button = (Button) findViewById(R.id.start_button);
		start_button.setOnTouchListener(mDelayHideTouchListener);
		start_button.setOnClickListener(new View.OnClickListener() {	 
			@Override
			public void onClick(View v) {
				//check timer state
				if (ToorTimer.isRunning()){
					if (ToorTimer.timerDone) {
						ToorTimer.timerStop();
						setTimerColor(textNormalColor);
					}else {
						ToorTimer.pauseTimer();
					}
				}else if (ToorTimer.isPaused()) {
					ToorTimer.resumeTimer();
				} else {
					ToorTimer.timerStart(getMSTime());
				}
			}
		});
		
	}
	
	public static int getMSTime() {
		int min = (60 * HOURS) + MINUTES;
		int ms = 60000 * min;
		return ms;
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
	}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@SuppressLint("ClickableViewAccessibility")
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}
	
	
	// Timer Picker
	private static int HOURS = 0;
	private static int MINUTES = 50;
	
	protected void showTimeDialog() {
		// set time picker as current time
		Dialog d = new TimePickerDialog(this, timePickerListener, HOURS, MINUTES, true);
		d.show();
	}
	
	private TimePickerDialog.OnTimeSetListener timePickerListener = 
            new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int selectedHour,
				int selectedMinute) {
 			
			HOURS = selectedHour;
			MINUTES = selectedMinute;
			
			// set current time into textview
			ToorTimer.timerStop();
			updateTimerText(selectedHour, selectedMinute, 0);
		}
	};
	
	public static void updateTimerText(int hours, int minutes, int seconds) {
		updateTimerText((hours*60) + minutes, seconds);
	}
	
	public static void updateTimerText(int minutes, int seconds) {
		// set current time into textview
		timeText.setText(new StringBuilder().append(pad(minutes))
				.append(":").append(pad(seconds)));
	}
	
	public static void setTimerColor(int color) {
		timeText.setTextColor(color);
	}
	
	private static String pad(int c) {
		if (c >= 10)
		   return String.valueOf(c);
		else
		   return "0" + String.valueOf(c);
	}
	
	//image rotation
	 public Bitmap rotateImage(Bitmap src, float degree) {
	     // create new matrix object
	     Matrix matrix = new Matrix();
	     // setup rotation degree
	     matrix.postRotate(degree);
	     // return new bitmap rotated using matrix
	     return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
	 }
	 
	 public static void BlinkTimer(boolean blink) {
		 if (blink) {
			 Animation anim = new AlphaAnimation(0.0f, 1.0f);
			 anim.setDuration(50); //You can manage the time of the blink with this parameter
			 anim.setStartOffset(20);
			 anim.setRepeatMode(Animation.REVERSE);
			 anim.setRepeatCount(Animation.INFINITE);
			 timeText.startAnimation(anim);
		 } else {
			 timeText.clearAnimation();
		 }
	 }
}
