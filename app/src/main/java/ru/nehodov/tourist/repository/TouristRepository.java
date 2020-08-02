package ru.nehodov.tourist.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.List;

import ru.nehodov.tourist.dao.RoutePointDao;
import ru.nehodov.tourist.dao.UserLocationDao;
import ru.nehodov.tourist.dao.UserRouteDao;
import ru.nehodov.tourist.entities.RoutePoint;
import ru.nehodov.tourist.entities.UserLocation;
import ru.nehodov.tourist.entities.UserRoute;
import ru.nehodov.tourist.storage.TouristDatabase;

public class TouristRepository {

    private static final String TAG = TouristRepository.class.getSimpleName();

    private final UserLocationDao userLocationDao;
    private final UserRouteDao userRouteDao;
    private final RoutePointDao routePointDao;

    private final LiveData<List<UserLocation>> userLocations;
    private final LiveData<List<UserRoute>> userRoutes;
    private final LiveData<List<RoutePoint>> routePoints;

    public TouristRepository(Application application) {
        TouristDatabase db = TouristDatabase.getDb(application);
        userLocationDao = db.getUserLocationDao();
        userLocations = userLocationDao.getUserLocations();
        userRouteDao = db.getUserRouteDao();
        routePointDao = db.getRoutePointDao();
        userRoutes = userRouteDao.getAllRoutes();
        routePoints = routePointDao.getAllRoutePoints();
    }

    public LiveData<List<UserLocation>> getAllLocations() {
        return this.userLocations;
    }

    public void addLocation(UserLocation location) {
        TouristDatabase.DB_EXECUTOR_SERVICE.execute(() -> userLocationDao.insert(location));
    }

    public LiveData<List<UserRoute>> getAllRoutes() {
        return this.userRoutes;
    }

    public void addRoute(UserRoute route) {
        TouristDatabase.DB_EXECUTOR_SERVICE.execute(() -> {
            Log.d(TAG, "Thread name for addRoute() " + Thread.currentThread().getName());
            long routeId = userRouteDao.insert(route);
            List<RoutePoint> routePoints = route.getRoutePoints();
            routePoints.forEach(routePoint -> routePoint.setRouteId(routeId));
            routePointDao.insert(routePoints);
        });
    }

    public LiveData<List<RoutePoint>> getAllRoutePoints() {
        return routePoints;
    }
}
