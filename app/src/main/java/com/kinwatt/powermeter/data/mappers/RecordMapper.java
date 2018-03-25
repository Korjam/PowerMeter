package com.kinwatt.powermeter.data.mappers;

import android.location.Location;
import android.util.Log;

import com.kinwatt.powermeter.data.Position;
import com.kinwatt.powermeter.data.Record;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public final class RecordMapper {

    private static final String NAME_TAG = "name";
    private static final String DATE_TAG = "date";
    private static final String POSITIONS_TAG = "positions";

    private static final String TIME_TAG = "time";
    private static final String LATITUDE_TAG = "latitude";
    private static final String LONGITUDE_TAG = "longitude";
    private static final String ALTITUDE_TAG = "altitude";
    private static final String SPEED_TAG = "speed";
    private static final String POWER_TAG = "power";

    public static void save(Record record, String filePath) throws IOException {
        save(record, new File(filePath));
    }

    public static void save(Record record, File file) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter writer = new FileWriter(file);
        save(record, writer);
        writer.close();
    }

    public static void save(Record record, Writer streamWriter) throws IOException {
        try {
            JSONObject json = createJson(record);
            streamWriter.write(json.toString());
        } catch (JSONException e) {
            throw new IOException("Unable to create JSON format.", e);
        }
    }

    public static void save(List<Location> record, String filePath) throws IOException {
        save(record, new File(filePath));
    }

    public static void save(List<Location> record, File file) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter writer = new FileWriter(file);
        save(record, writer);
        writer.close();
    }

    public static void save(List<Location> record, Writer streamWriter) throws IOException {
        streamWriter.write(createJson(record).toString());
    }

    private static JSONObject createJson(Record record) throws JSONException {
        JSONObject base = new JSONObject();

        base.put(NAME_TAG, record.getName())
                .put(DATE_TAG, record.getDate().getTime());

        JSONArray positions = new JSONArray();
        for (Position position : record.getPositions()) {
            positions.put(new JSONObject()
                    .put(TIME_TAG, position.getTimestamp())
                    .put(LATITUDE_TAG, position.getLatitude())
                    .put(LONGITUDE_TAG, position.getLongitude())
                    .put(ALTITUDE_TAG, position.getAltitude())
                    .put(SPEED_TAG, position.getSpeed())
                    .put(POWER_TAG, position.getPower()));
        }
        base.put(POSITIONS_TAG, positions);

        return base;
    }

    private static JSONObject createJson(List<Location> locations) {
        JSONObject base = new JSONObject();

        try
        {
            Location baseLocation = locations.get(0);

            base.put("name", "Cycling outdoor raw")
                    .put("date", baseLocation.getTime());

            JSONArray positions = new JSONArray();
            for (Location location : locations) {
                positions.put(new JSONObject()
                        .put("time", location.getTime() - baseLocation.getTime())
                        .put("speed", location.getSpeed())
                        .put("rpm", location.getExtras().getFloat("rpm"))
                        .put("power", location.getExtras().getFloat("power"))
                        .put("location", new JSONObject()
                                .put("longitude", location.getLongitude())
                                .put("latitude", location.getLatitude())
                                .put("horizontal error", location.getAccuracy())
                                .put("altitude", location.getAltitude())
                        )
                );
            }
            base.put("tracks", positions);
        }
        catch (JSONException ex)
        {
            Log.e("MAPPER", ex.getMessage());
        }

        return base;
    }

    public static Record load(String filePath) throws IOException {
        return load(new File(filePath));
    }

    public static Record load(File file) throws IOException {
        FileReader reader = new FileReader(file);
        Record res = load(reader);
        reader.close();
        return res;
    }

    public static Record load(InputStream stream) throws IOException {
        InputStreamReader reader = new InputStreamReader(stream);
        Record res = load(reader);
        reader.close();
        return res;
    }

    public static Record load(Reader reader) throws IOException {
        Record record = new Record();

        try {
            JSONObject object = new JSONObject(convertStreamToString(reader));

            long timestamp = object.getLong(DATE_TAG);
            timestamp -= timestamp % 1000;
            record.setName(object.getString(NAME_TAG));
            record.setDate(new Date(timestamp));

            JSONArray array = object.getJSONArray(POSITIONS_TAG);
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                record.getPositions().add(new Position(
                        (float)item.getDouble(LATITUDE_TAG),
                        (float)item.getDouble(LONGITUDE_TAG),
                        (float)item.getDouble(ALTITUDE_TAG),
                        (float)item.getDouble(SPEED_TAG),
                        item.getLong(TIME_TAG)));
            }

            return record;
        } catch (JSONException e) {
            throw new IOException("Unable to load JSON.", e);
        }
    }

    private static String convertStreamToString(Reader reader) {
        Scanner s = new Scanner(reader).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
