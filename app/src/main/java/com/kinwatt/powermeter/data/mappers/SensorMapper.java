package com.kinwatt.powermeter.data.mappers;


import com.kinwatt.powermeter.data.SensorData;
import com.kinwatt.powermeter.data.ServiceData;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public final class SensorMapper {
    private static final String SENSORS_TAG = "sensors";

    private static final String NAME_TAG = "name";
    private static final String ADDRESS_TAG = "address";
    private static final String SERVICES_TAG = "services";

    private static final String UUID_TAG = "uuid";

    public static void save(Iterable<SensorData> sensors, String filePath) throws IOException {
        save(sensors, new File(filePath));
    }

    public static void save(Iterable<SensorData> sensors, File file) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter writer = new FileWriter(file);
        save(sensors, writer);
        writer.close();
    }

    public static void save(Iterable<SensorData> sensors, Writer streamWriter) throws IOException {
        try {
            streamWriter.write(createJson(sensors).toString());
        } catch (JSONException e) {
            throw new IOException("Unable to create JSON format.", e);
        }
    }

    private static JSONObject createJson(Iterable<SensorData> sensors) throws JSONException {
        JSONArray list = new JSONArray();
        for (SensorData sensor : sensors) {
            list.put(createJson(sensor));
        }
        return new JSONObject().put(SENSORS_TAG, list);
    }

    private static JSONObject createJson(SensorData sensor) throws JSONException {
        JSONObject base = new JSONObject()
                .put(NAME_TAG, sensor.getName())
                .put(ADDRESS_TAG, sensor.getAddress());

        JSONArray services = new JSONArray();
        for (ServiceData service : sensor.getServices()) {
            services.put(new JSONObject()
                    .put(UUID_TAG, service.getUuid().toString()));
        }
        base.put(SERVICES_TAG, services);

        return base;
    }


    public static List<SensorData> load(String filePath) throws IOException {
        return load(new File(filePath));
    }

    public static List<SensorData> load(File file) throws IOException {
        FileReader reader = new FileReader(file);
        List<SensorData> res = load(reader);
        reader.close();
        return res;
    }

    public static List<SensorData> load(InputStream stream) throws IOException {
        InputStreamReader reader = new InputStreamReader(stream);
        List<SensorData> res = load(reader);
        reader.close();
        return res;
    }

    public static List<SensorData> load(Reader reader) throws IOException {
        try {
            return load(new JSONObject(convertStreamToString(reader)));
        } catch (JSONException e) {
            throw new IOException("Unable to load JSON.", e);
        }
    }

    private static List<SensorData> load(JSONObject object) throws IOException  {
        List<SensorData> res = new ArrayList<>();
        try {
            JSONArray array = object.getJSONArray(SENSORS_TAG);
            for (int i = 0; i < array.length(); i++) {
                res.add(loadSensorData(array.getJSONObject(i)));
            }
        } catch (JSONException e) {
            throw new IOException("Unable to load JSON.", e);
        }
        return res;
    }

    private static SensorData loadSensorData(JSONObject object) throws IOException {
        SensorData sensor = new SensorData();
        try {
            sensor.setName(object.getString(NAME_TAG));
            sensor.setAddress(object.getString(ADDRESS_TAG));

            JSONArray array = object.getJSONArray(SERVICES_TAG);
            for (int i = 0; i < array.length(); i++) {
                sensor.getServices().add(loadServiceData(array.getJSONObject(i)));
            }

            return sensor;
        } catch (JSONException e) {
            throw new IOException("Unable to load JSON.", e);
        }
    }

    private static ServiceData loadServiceData(JSONObject object) throws IOException {
        try {
            return new ServiceData(
                    UUID.fromString(object.getString(UUID_TAG)));
        } catch (JSONException e) {
            throw new IOException("Unable to load JSON.", e);
        }
    }

    private static String convertStreamToString(Reader reader) {
        Scanner s = new Scanner(reader).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}

