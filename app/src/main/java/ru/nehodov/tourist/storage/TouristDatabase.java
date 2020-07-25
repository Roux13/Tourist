package ru.nehodov.tourist.storage;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.nehodov.tourist.dao.UserLocationDao;
import ru.nehodov.tourist.model.UserLocation;

@Database(entities = UserLocation.class, version = 1, exportSchema = false)
public abstract class TouristDatabase extends RoomDatabase {

    private static volatile TouristDatabase instance;
    private static final String DB_NAME = "tourist_db";
    private static final int NUMBER_OF_THREADS = 4;

    public static final ExecutorService DB_EXECUTOR_SERVICE
            = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public abstract UserLocationDao getUserLocationDao();

    public static TouristDatabase getDb(final Context context) {
        if (instance == null) {
            synchronized (TouristDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            TouristDatabase.class,
                            DB_NAME)
                            .build();
                }
            }
        }
        return instance;
    }

}
