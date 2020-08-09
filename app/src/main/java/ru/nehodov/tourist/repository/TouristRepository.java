package ru.nehodov.tourist.repository;

import android.app.Application;
import android.location.Location;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.nehodov.tourist.network.DirectionsApiInterface;
import ru.nehodov.tourist.RouteCallback;
import ru.nehodov.tourist.dao.RoutePointDao;
import ru.nehodov.tourist.dao.UserLocationDao;
import ru.nehodov.tourist.dao.UserRouteDao;
import ru.nehodov.tourist.entities.DirectionResults;
import ru.nehodov.tourist.entities.RoutePoint;
import ru.nehodov.tourist.entities.UserLocation;
import ru.nehodov.tourist.entities.UserRoute;
import ru.nehodov.tourist.storage.TouristDatabase;
import ru.nehodov.tourist.network.NetworkService;

public class TouristRepository {

    private static final String TAG = TouristRepository.class.getSimpleName();

    private final DirectionsApiInterface directionsApi;

    private final UserLocationDao userLocationDao;
    private final UserRouteDao userRouteDao;
    private final RoutePointDao routePointDao;

    private final LiveData<List<UserLocation>> userLocations;
    private final LiveData<List<UserRoute>> userRoutes;
    private final LiveData<List<RoutePoint>> routePoints;
    private final MutableLiveData<DirectionResults> directionResultsData = new MutableLiveData<>();

    public TouristRepository(Application application) {
        TouristDatabase db = TouristDatabase.getDb(application);
        userLocationDao = db.getUserLocationDao();
        userLocations = userLocationDao.getUserLocations();
        userRouteDao = db.getUserRouteDao();
        routePointDao = db.getRoutePointDao();
        userRoutes = userRouteDao.getAllRoutes();
        routePoints = routePointDao.getAllRoutePoints();
        directionsApi = new NetworkService().getDirectionsApi();
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

    public LiveData<DirectionResults> getDirectionResults() {
        return directionResultsData;
    }

    public void getDirectionResults(Location origin, Location destination, String key,
                                    RouteCallback callback) {
            Call<DirectionResults> call = directionsApi
                    .getDirectionResults(
                            origin.getLatitude() + "," + origin.getLongitude(),
                            destination.getLatitude() + "," + destination.getLongitude(),
                            key);
            call.enqueue(new Callback<DirectionResults>() {
                @Override
                public void onResponse(Call<DirectionResults> call,
                                       Response<DirectionResults> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        directionResultsData.setValue(response.body());
                        callback.drawRouteToAttraction();
                    } else {
                        try {
                            Log.d(TAG, String.format(
                                    "Request is nt successful, code: %s%nError: %s",
                                    response.code(), response.errorBody().string()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<DirectionResults> call, Throwable t) {
                    Log.d(TAG, t.getMessage());
                }
            });
    }
}
