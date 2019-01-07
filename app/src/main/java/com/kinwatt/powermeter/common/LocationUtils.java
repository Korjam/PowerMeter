package com.kinwatt.powermeter.common;

import android.location.Location;

import com.kinwatt.powermeter.data.Position;
import com.kinwatt.powermeter.data.Record;
import com.kinwatt.powermeter.model.CyclingOutdoorPowerAlgorithm;

import java.util.List;

public class LocationUtils {

    private static final int INTERPOLATION_STEP = 1000; // 1s
    private static final float ALTITUDE_ERROR = 10;

    public static Position interpolate(Position p1, Position p2, long v) {
        float latitude = MathUtils.interpolate(
                p1.getTimestamp(), p1.getLatitude(),
                p2.getTimestamp(), p2.getLatitude(), v);
        float longitude = MathUtils.interpolate(
                p1.getTimestamp(), p1.getLongitude(),
                p2.getTimestamp(), p2.getLongitude(), v);
        float altitude = MathUtils.interpolate(
                p1.getTimestamp(), p1.getAltitude(),
                p2.getTimestamp(), p2.getAltitude(), v);
        float speed = MathUtils.interpolate(
                p1.getTimestamp(), p1.getSpeed(),
                p2.getTimestamp(), p2.getSpeed(), v);

        return new Position(latitude,
                longitude,
                altitude,
                speed,
                v);
    }

    public static Function<Long, Position> interpolate(Position p1, Position p2) {
        final Function<Long, Float> latitude = MathUtils.interpolate(
                p1.getTimestamp(), p1.getLatitude(),
                p2.getTimestamp(), p2.getLatitude());
        final Function<Long, Float> longitude = MathUtils.interpolate(
                p1.getTimestamp(), p1.getLongitude(),
                p2.getTimestamp(), p2.getLongitude());
        final Function<Long, Float> altitude = MathUtils.interpolate(
                p1.getTimestamp(), p1.getAltitude(),
                p2.getTimestamp(), p2.getAltitude());
        final Function<Long, Float> speed = MathUtils.interpolate(
                p1.getTimestamp(), p1.getSpeed(),
                p2.getTimestamp(), p2.getSpeed());

        return v -> new Position(latitude.apply(v),
                longitude.apply(v),
                altitude.apply(v),
                speed.apply(v),
                v);
    }

    public static Location interpolate(Location p1, Location p2, long v) {
        double latitude = MathUtils.interpolate(
                p1.getTime(), p1.getLatitude(),
                p2.getTime(), p2.getLatitude(), v);
        double longitude = MathUtils.interpolate(
                p1.getTime(), p1.getLongitude(),
                p2.getTime(), p2.getLongitude(), v);
        double altitude = MathUtils.interpolate(
                p1.getTime(), p1.getAltitude(),
                p2.getTime(), p2.getAltitude(), v);
        float speed = MathUtils.interpolate(
                p1.getTime(), p1.getSpeed(),
                p2.getTime(), p2.getSpeed(), v);

        Location res = new Location(p1.getProvider());
        res.setLatitude(latitude);
        res.setLongitude(longitude);
        res.setAltitude(altitude);
        res.setSpeed(speed);
        res.setTime(v);
        return res;
    }

    public static Function<Long, Location> interpolate(Location p1, Location p2) {
        final Function<Long, Double> latitude = MathUtils.interpolate(
                p1.getTime(), p1.getLatitude(),
                p2.getTime(), p2.getLatitude());
        final Function<Long, Double> longitude = MathUtils.interpolate(
                p1.getTime(), p1.getLongitude(),
                p2.getTime(), p2.getLongitude());
        final Function<Long, Double> altitude = MathUtils.interpolate(
                p1.getTime(), p1.getAltitude(),
                p2.getTime(), p2.getAltitude());
        final Function<Long, Float> speed = MathUtils.interpolate(
                p1.getTime(), p1.getSpeed(),
                p2.getTime(), p2.getSpeed());

        final String provider = p1.getProvider();

        return v -> {
            Location res = new Location(provider);
            res.setLatitude(latitude.apply(v));
            res.setLongitude(longitude.apply(v));
            res.setAltitude(altitude.apply(v));
            res.setSpeed(speed.apply(v));
            res.setTime(v);
            return res;
        };
    }

    public static Record interpolate(Record record) {
        return interpolate(record, INTERPOLATION_STEP);
    }

    public static Record interpolate(Record record, int interpolationStep) {
        Record res = new Record();
        res.setName(record.getName());
        res.setDate(record.getDate());
        res.getPositions().add(record.getPositions().get(0));

        for (int i = 0; i < record.getPositions().size() - 1; i++) {
            interpolatePositions(res, record.getPositions().get(i), record.getPositions().get(i + 1), interpolationStep);
        }

        return res;
    }

    private static void interpolatePositions(Record record, Position p1,  Position p2, int interpolationStep) {
        Function<Long, Position> interpolation = LocationUtils.interpolate(p1, p2);

        long start = record.getLastPosition().getTimestamp() + interpolationStep;
        long end = p2.getTimestamp() + (interpolationStep - p2.getTimestamp() % interpolationStep);

        for (long i = start; i <= end; i += interpolationStep) {
            Position lastPosition = record.getLastPosition();
            Position interpolatedPosition = interpolation.apply(i);

            //TODO: Calculate degrees from 5s ago;
            /*
            if (record.getPositions().size() >= 5) {
                Position target = buffer.peek(4);
                float hDiff = interpolatedPosition.getAltitude() - target.getAltitude();
                float grade = hDiff / target.getDistance(interpolatedPosition);

                interpolatedPosition.setPower(CyclingOutdoorPower.calculatePower(lastPosition, interpolatedPosition, grade));
            }
            else {
                interpolatedPosition.setPower(CyclingOutdoorPower.calculatePower(lastPosition, interpolatedPosition));
            }
            */

            interpolatedPosition.setPower(new CyclingOutdoorPowerAlgorithm(null).calculatePower(Position.Companion.convert(lastPosition), Position.Companion.convert(interpolatedPosition)));

            record.getPositions().add(interpolatedPosition);
        }
    }

    public static void normalize(List<Position> positions) {
        for (int i = 0; i < positions.size(); i++) {
            normalize(i, positions);
        }
    }

    private static void normalize(int i, List<Position> positions) {
        Position position = positions.get(i);
        Position previous = i > 0 ? positions.get(i - 1) : null;

        if (previous != null && position.getAltitude()  == 0) {
            //if (previous != null && previous.getAltitude() - position.getAltitude() > ALTITUDE_ERROR) {
            int j = getNextValidIndex(i, previous.getAltitude(), positions);

            Position next = j > 0 ? positions.get(j) : null;
            if (next != null) {

                Function<Long, Position> interpolation = interpolate(previous, next);

                for (int k = i; k < j; k++) {
                    position = positions.get(k);

                    Position expected = interpolation.apply(position.getTimestamp());
                    if (position.getSpeed() == 0) {
                        position.setSpeed(expected.getSpeed());
                    }
                    position.setAltitude(expected.getAltitude());
                }
            }
        }
    }

    private static int getNextValidIndex(int i, float validAltitude, List<Position> positions) {
        for (int j = i; j < positions.size(); j++) {
            if (positions.get(j).getAltitude() != 0) {
                //if (validAltitude - positions.get(j).getAltitude() < ALTITUDE_ERROR) {
                return j;
            }
        }
        return -1;
    }
}
