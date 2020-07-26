package ru.nehodov.tourist.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

import ru.nehodov.tourist.dao.UserLocationDao;
import ru.nehodov.tourist.model.UserLocation;
import ru.nehodov.tourist.storage.TouristDatabase;

public class TouristRepository {

    private final UserLocationDao userLocationDao;
    private final LiveData<List<UserLocation>> userLocations;

    public TouristRepository(Application application) {
        TouristDatabase db = TouristDatabase.getDb(application);
        userLocationDao = db.getUserLocationDao();
        userLocations = userLocationDao.getUserLocations();
    }

    public LiveData<List<UserLocation>> getAllLocations() {
        return this.userLocations;
    }

    public void addLocation(UserLocation location) {
        TouristDatabase.DB_EXECUTOR_SERVICE.execute(() -> userLocationDao.insert(location));
    }

    public void deleteAllLocations() {
        TouristDatabase.DB_EXECUTOR_SERVICE.execute(() -> userLocationDao.deleteAll());
    }

}
