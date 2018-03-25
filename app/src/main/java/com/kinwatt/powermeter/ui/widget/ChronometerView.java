package com.kinwatt.powermeter.ui.widget;

import android.content.Context;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@SuppressWarnings("all")
public class ChronometerView extends TextView {
    private static final SimpleDateFormat DURATION_FORMAT = new SimpleDateFormat("HH:mm:ss");

    private boolean mVisible = false;
    private boolean mRunning = false;
    private boolean mStarted = false;
    private long mBaseTime = 0;

    public ChronometerView(Context context) {
        super(context);
        DURATION_FORMAT.setTimeZone(TimeZone.getTimeZone("GTM"));
    }

    public ChronometerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        DURATION_FORMAT.setTimeZone(TimeZone.getTimeZone("GTM"));
    }

    public ChronometerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        DURATION_FORMAT.setTimeZone(TimeZone.getTimeZone("GTM"));
    }

    public ChronometerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        DURATION_FORMAT.setTimeZone(TimeZone.getTimeZone("GTM"));
    }

    public void start() {
        if (mBaseTime == 0) {
            reset();
        }
        mStarted = true;
        updateRunning();
    }

    public void restart() {
        reset();
        mStarted = true;
        updateRunning();
    }

    public void pause() {
        //TODO: implement pause
    }

    public void resume() {
        //TODO: implement resume
    }

    public void stop() {
        mStarted = false;
        updateRunning();
    }

    public void reset() {
        mBaseTime = SystemClock.elapsedRealtime();
        updateText();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mVisible = false;
        updateRunning();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        mVisible = visibility == VISIBLE;
        updateRunning();
    }

    private void updateRunning() {
        boolean running = mVisible && mStarted && isShown();
        if (running != mRunning) {
            if (running) {
                postDelayed(mTickRunnable, 1000);
            } else {
                removeCallbacks(mTickRunnable);
            }
            mRunning = running;
        }
    }

    private void updateText() {
        setText(DURATION_FORMAT.format(new Date(SystemClock.elapsedRealtime() - mBaseTime)));
    }

    private final Runnable mTickRunnable = new Runnable() {
        @Override
        public void run() {
            if (mRunning) {
                updateText();
                postDelayed(mTickRunnable, 1000);
            }
        }
    };
}

