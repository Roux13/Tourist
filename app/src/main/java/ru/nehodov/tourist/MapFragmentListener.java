package ru.nehodov.tourist;

import android.location.Location;

import androidx.lifecycle.LiveData;

import java.util.List;

import ru.nehodov.tourist.entities.DirectionResults;
import ru.nehodov.tourist.entities.UserLocation;
import ru.nehodov.tourist.entities.UserRoute;

public interface MapFragmentListener {

    void addNewLocation(UserLocation location);

    void addNewRoute(UserRoute route);

    List<UserLocation> getLocations();

    LiveData<UserLocation> subscribeSelectedLocation();

    LiveData<Boolean> subscribeIsUpdateLocationStopped();

    LiveData<Boolean> subscribeIsLocationSelected();

    LiveData<Boolean> subscribeIsRouteSelected();

    LiveData<UserRoute> subscribeSelectedRoute();

    LiveData<DirectionResults> subscribeDirectionResults();

    void askRoute(Location origin, Location destination, String key, RouteCallback callback);

    void returnToCurrentLocation();

    void selectLocation(UserLocation location);

}
