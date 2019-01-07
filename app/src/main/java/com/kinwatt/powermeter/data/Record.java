package com.kinwatt.powermeter.data;

import android.location.Location;

import com.kinwatt.powermeter.common.MathUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Record {

    private String name;
    private Date date;
    private ArrayList<Position> positions;

    public Record() {
        positions = new ArrayList<>();
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }

    public List<Position> getPositions() {
        return positions;
    }

    public Position getLastPosition() {
        return positions.isEmpty() ? null : positions.get(positions.size() - 1);
    }

    public float getDistance() {
        float distance = 0;
        for (int i = 0; i < positions.size() - 1; i++) {
            Position p1 = positions.get(i);
            Position p2 = positions.get(i + 1);
            distance += p1.distanceTo(p2);
        }
        return distance;
    }

    public Position addPosition(Location location) {
        if (this.positions.isEmpty()) {
            this.date = new Date(location.getTime());
        }
        Position position = new Position((float)location.getLatitude(), (float)location.getLongitude(), (float)location.getAltitude());
        position.setSpeed(location.getSpeed());
        position.setTimestamp(location.getTime() - date.getTime());
        positions.add(position);
        return position;
    }

    public float getSpeed() {
        List<Float> validValues = new ArrayList<>();
        for (Position p : positions) {
            if (p.getSpeed() != 0) {
                validValues.add(p.getSpeed());
            }
        }

        return MathUtils.average(validValues);
    }
}
