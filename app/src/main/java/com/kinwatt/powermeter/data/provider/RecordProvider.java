package com.kinwatt.powermeter.data.provider;

import android.content.Context;
import android.os.Environment;

import com.kinwatt.powermeter.common.FileUtils;
import com.kinwatt.powermeter.data.Record;
import com.kinwatt.powermeter.data.mappers.RecordMapper;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecordProvider {

    private static RecordProvider instance;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);

    private Context context;
    private File filesDir;

    private List<Record> records;

    private RecordProvider(Context context) {
        this.context = context;

        filesDir = context.getFilesDir();

        this.records = new ArrayList<>();
        for (Record record : readRecords()) {
            this.records.add(record);
        }
    }

    private List<Record> readRecords() {
        List<Record> records = new ArrayList<>();

        final String name = "Cycling outdoor_";
        final String extension = ".json";
        for (File file: filesDir.listFiles(pathname -> pathname.getName().startsWith(name) &&
                                                       pathname.getName().endsWith(extension))) {
            String datetime = file.getName().substring(name.length(), file.getName().indexOf('.'));

            Date date = null;

            try {
                date = DATE_FORMAT.parse(datetime);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Record record = new Record();
            record.setName("Cycling outdoor");
            record.setDate(date);
            records.add(record);
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N){
            records.sort((o1, o2) -> o2.getDate().compareTo(o1.getDate()));
        }
        return records;
    }

    public List<Record> getAll() {
        return records;
    }

    public void add(Record record) {
        try {
            RecordMapper.save(record, getFile(record));
            records.add(record);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public File getFile(Record item) {
        return new File(filesDir, String.format("%s_%s.json", item.getName(), DATE_FORMAT.format(item.getDate())));
    }

    public static RecordProvider getProvider(Context context) {
        if (instance == null) {
            instance = new RecordProvider(context);
        }
        return instance;
    }

    public void migrateFiles() {
        final String name = "Cycling outdoor_";
        final String extension = ".json";
        for (File file: Environment.getExternalStorageDirectory().listFiles(pathname ->
                pathname.getName().startsWith(name) &&
                pathname.getName().endsWith(extension))) {
            try {
                FileUtils.copyFile(Environment.getExternalStorageDirectory().getAbsolutePath(), file.getName(), filesDir.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
