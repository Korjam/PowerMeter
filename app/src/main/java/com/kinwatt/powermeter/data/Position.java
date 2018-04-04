package com.kinwatt.powermeter.data;

import android.location.Location;

import com.kinwatt.powermeter.common.MathUtils;

public class Position implements Cloneable {

    private static final float geodesica = 40030000;
    private static final float geodesica_u = geodesica / 360;

    private float latitude, longitude, altitude;
    private float speed, power;
    private long timestamp;

    public Position(float latitude, float longitude) {
        this(latitude, longitude, 0);
    }

    public Position(float latitude, float longitude, float altitude) {
        this(latitude, longitude, altitude, 0);
    }
    
    public Position(float latitude, float longitude, float altitude, float speed) {
        this(latitude, longitude, altitude, speed, 0);
    }
    
    public Position(float latitude, float longitude, float altitude, float speed, long timestamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.timestamp = timestamp;
        this.speed = speed;
    }

    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long seconds) {
        this.timestamp = seconds;
    }

    /***
     * Get the latitude, in degrees.
     * @return
     */
    public float getLatitude() {
        return latitude;
    }
    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    /***
     * Get the longitude, in degrees.
     * @return
     */
    public float getLongitude() {
        return longitude;
    }
    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    /***
     * Get the altitude if available, in meters above the WGS 84 reference ellipsoid.
     * @return
     */
    public float getAltitude() {
        return altitude;
    }
    public void setAltitude(float altitude) {
        this.altitude = altitude;
    }

    /***
     * Get the speed if it is available, in meters/second over ground.
     * @return
     */
    public float getSpeed() {
        return speed;
    }
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    /***
     * Get the power if it is available, in watts.
     * @return
     */
    public float getPower() {
        return power;
    }
    public void setPower(float power) {
        this.power = power;
    }

    public float getDistance(Position position) {
        double dLat = (position.getLatitude() - this.getLatitude()) * geodesica_u;
        double meanLat = MathUtils.average(this.getLatitude(), position.getLatitude());
        double dLon = ((position.getLongitude() - this.getLongitude()) * Math.sin(Math.toRadians(90 - meanLat)) * geodesica_u);
        return (float) Math.sqrt(Math.pow(dLat, 2) + Math.pow(dLon, 2));
    }

    public static Position convert(Location location) {
        Position res = new Position(
                (float) location.getLatitude(),
                (float) location.getLongitude(),
                (float) location.getAltitude());
        res.setSpeed(location.getSpeed());
        return res;
    }

    public static Location convert(Position position) {
        Location res = new Location("NULL");
        res.setLatitude(position.getLatitude());
        res.setLongitude(position.getLongitude());
        res.setAltitude(position.getAltitude());
        res.setSpeed(position.getSpeed());
        return res;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Position) {
            return this.equals((Position)obj);
        }
        else {
            return false;
        }
    }

    public boolean equals(Position other) {
        return this.altitude == other.altitude &&
                this.latitude == other.latitude &&
                this.longitude == other.longitude &&
                this.speed == other.speed &&
                this.timestamp == other.timestamp &&
                this.power == other.power;
    }

    @Override
    public Position clone() {
        Position p = new Position(this.latitude, this.longitude, this.altitude, this.speed, this.timestamp);
        p.power = this.power;
        return p;
    }
}
