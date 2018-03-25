package com.kinwatt.powermeter.sensor;

import android.app.Activity;
import android.location.Location;

import com.kinwatt.powermeter.data.Position;
import com.kinwatt.powermeter.data.Record;

import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LocationProviderMock extends LocationProvider {

    private Record record;
    private List<Position> positions;
    private int currentIndex = 0;

    private Activity activity;

    private long delay;

    public LocationProviderMock(Activity activity) {
        this.activity = activity;
        this.delay = 1000;
    }

    public Record getRecord() {
        return record;
    }
    public void setRecord(Record record) {
        this.record = record;
        positions = record.getPositions();
    }

    public long getDelay() {
        return delay;
    }
    public void setDelay(long delay) {
        this.delay = delay;
    }

    public void start() {
        currentIndex = 0;
        record.setDate(Calendar.getInstance().getTime());
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                activity.runOnUiThread(task);

                currentIndex++;
                if (currentIndex == positions.size()) {
                    stop();
                }
            }
        };
        timer.schedule(timerTask, delay, delay);
    }
    public void stop() {
        timer.cancel();
    }

    private Timer timer;
    private TimerTask timerTask;

    public Runnable task = new Runnable() {
        @Override
        public void run() {
            Position p = positions.get(currentIndex);
            Location l = new Location("GpsMock");
            l.setTime(p.getTimestamp() + record.getDate().getTime());
            l.setLongitude(p.getLongitude());
            l.setLatitude(p.getLatitude());
            l.setAltitude(p.getAltitude());
            l.setSpeed(p.getSpeed());

            onLocationChanged(l);
        }
    };
}

