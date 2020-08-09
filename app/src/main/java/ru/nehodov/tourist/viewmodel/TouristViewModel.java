package ru.nehodov.tourist.viewmodel;

import android.app.Application;
import android.location.Location;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.stream.Collectors;

import ru.nehodov.tourist.RouteCallback;
import ru.nehodov.tourist.entities.DirectionResults;
import ru.nehodov.tourist.entities.RoutePoint;
import ru.nehodov.tourist.entities.UserLocation;
import ru.nehodov.tourist.entities.UserRoute;
import ru.nehodov.tourist.repository.TouristRepository;

public class TouristViewModel extends AndroidViewModel {

    private final static String TAG = TouristViewModel.class.getSimpleName();

    private final TouristRepository repository;
    private final LiveData<List<UserLocation>> userLocationsLiveData;
    private final LiveData<List<UserRoute>> userRoutesLiveData;
    private final LiveData<List<RoutePoint>> routePointsLiveData;
    private final LiveData<DirectionResults> directionResultsData;

    private List<UserLocation> userLocations;
    private List<UserRoute> userRoutes;
    private List<RoutePoint> routePoints;

    private final MutableLiveData<Boolean> isLocationUpdateStopped = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLocationSelected = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isRouteSelected = new MutableLiveData<>();

    private final MutableLiveData<UserLocation> selectedLocation = new MutableLiveData<>();
    private final MutableLiveData<UserRoute> selectedRoute = new MutableLiveData<>();

    public TouristViewModel(Application application) {
        super(application);
        repository = new TouristRepository(application);
        userLocationsLiveData = repository.getAllLocations();
        userRoutesLiveData = repository.getAllRoutes();
        routePointsLiveData = repository.getAllRoutePoints();
        directionResultsData = repository.getDirectionResults();
        isLocationUpdateStopped.setValue(Boolean.FALSE);
        isLocationSelected.setValue(Boolean.FALSE);
        isRouteSelected.setValue(Boolean.FALSE);
    }

    public void observeData(LifecycleOwner lifecycleOwner) {
        userLocationsLiveData.observe(lifecycleOwner,
                locationsData -> userLocations = locationsData);
        userRoutesLiveData.observe(lifecycleOwner, routesData -> userRoutes = routesData);
        routePointsLiveData.observe(lifecycleOwner,
                routePointsData -> routePoints = routePointsData);
    }

    public List<UserLocation> getAllLocations() {
        return this.userLocations;
    }

    public void addLocation(UserLocation location) {
        repository.addLocation(location);
    }

    public void addRoute(UserRoute route) {
        repository.addRoute(route);
    }

    public List<UserRoute> getAllRoutes() {
        return userRoutes;
    }

    public LiveData<UserLocation> getSelectedLocation() {
        return selectedLocation;
    }

    public LiveData<Boolean> getIsLocationUpdateStopped() {
        return isLocationUpdateStopped;
    }

    public LiveData<Boolean> getIsLocationSelected() {
        return isLocationSelected;
    }

    public LiveData<Boolean> getIsRouteSelected() {
        return isRouteSelected;
    }

    public void setSelectedLocation(UserLocation selectedLocation) {
        this.selectedLocation.setValue(selectedLocation);
        this.isRouteSelected.setValue(Boolean.FALSE);
        this.isLocationUpdateStopped.setValue(Boolean.TRUE);
        this.isLocationSelected.setValue(Boolean.TRUE);
    }

    public void setIsLocationUpdateStopped(boolean isLocationUpdateStopped) {
        this.isLocationUpdateStopped.setValue(isLocationUpdateStopped);
    }

    public LiveData<UserRoute> getSelectedRoute() {
        return selectedRoute;
    }

    public void setSelectedRoute(UserRoute route) {
        List<RoutePoint> routePoints = this.routePoints.stream()
                .filter(routePoint -> routePoint.getRouteId() == route.getId())
                .collect(Collectors.toList());
        route.setRoutePoints(routePoints);
        this.selectedRoute.setValue(route);
        this.isLocationSelected.setValue(Boolean.FALSE);
        this.isLocationUpdateStopped.setValue(Boolean.TRUE);
        this.isRouteSelected.setValue(Boolean.TRUE);
    }

    public void setIsLocationSelected(boolean isLocationSelected) {
        this.isLocationSelected.setValue(isLocationSelected);
    }

    public void setIsRouteSelected(boolean isRouteSelected) {
        this.isRouteSelected.setValue(isRouteSelected);
    }

    public LiveData<DirectionResults> getDirectionResultsData() {
        return directionResultsData;
    }

    public void askRoute(Location origin,
                         Location destination,
                         String key,
                         RouteCallback callback) {
        repository.getDirectionResults(origin, destination, key, callback);
    }
}
