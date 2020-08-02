package ru.nehodov.tourist;

import android.Manifest;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import ru.nehodov.tourist.entities.RoutePoint;
import ru.nehodov.tourist.entities.UserLocation;
import ru.nehodov.tourist.entities.UserRoute;

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static androidx.core.content.ContextCompat.checkSelfPermission;
import static androidx.core.content.ContextCompat.getColor;

public class MapFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener {

    private static final String TAG = "MapFragment.class";
    private static final int MIN_TIME_FOR_REQUEST_LOCATION_UPDATE = 1000;
    private static final int POLYLINE_STROKE_WIDTH_PX = 12;

    private ActivityResultLauncher<String> requestPermissionLauncher;

    private MapFragmentListener listener;

    private GoogleMap map;

    private LocationRequest locationRequest;

    private LocationCallback locationCallback;

    private FusedLocationProviderClient fusedLocationClient;

    private Location currentLocation;
    private boolean isUpdateLocationStopped;
    private boolean isLocationSelected;
    private boolean isRouteSelected;
    private UserLocation selectedLocation;
    private UserRoute selectedRoute;
    private boolean isTrackRecordStarted;
    private final List<LatLng> savedLatLngs = new ArrayList<>();
    private Polyline routePolyline;
    private Marker marker;

    private View mapView;

    private TextView trackRecordingLabel;

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (!isGranted) {
                        Snackbar.make(
                                requireActivity().findViewById(R.id.activity_tab),
                                R.string.permission_rationale,
                                Snackbar.LENGTH_LONG)
                                .show();
                    }
                }
        );

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                LatLng selected = null;
                if (locationResult != null) {
                    Log.d(TAG, "Into onLocationResult");
                    if (isUpdateLocationStopped) {
                        if (marker != null) {
                            marker.remove();
                            marker = null;
                        }
                        if (isLocationSelected) {
                            selected = new LatLng(
                                    selectedLocation.getLatitude(),
                                    selectedLocation.getLongitude());
                            marker = map.addMarker(new MarkerOptions()
                                    .position(selected)
                                    .icon(BitmapDescriptorFactory
                                            .defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                                    .title(selectedLocation.getName())
                                    .snippet(selectedLocation.getTime()));
                            marker.showInfoWindow();
                        }
                        if (isRouteSelected) {
                            List<LatLng> latLngs = selectedRoute.getRoutePoints().stream()
                                    .map(routePoint -> new LatLng(
                                            routePoint.getLatitude(), routePoint.getLongitude()))
                                    .collect(Collectors.toList());
                            selected = latLngs.get(0);
                            marker = map.addMarker(new MarkerOptions()
                                    .position(selected)
                                    .title(selectedRoute.getStartPointName()));
                            marker.showInfoWindow();
                            configurePolyline(latLngs);
                        }
                        stopLocationUpdates();
                    } else {
                        currentLocation = locationResult.getLastLocation();
                        selected = new LatLng(
                                currentLocation.getLatitude(), currentLocation.getLongitude());
                        if (isTrackRecordStarted) {
                            if (trackRecordingLabel.getVisibility() == View.GONE) {
                                trackRecordingLabel.setVisibility(View.VISIBLE);
                                savedLatLngs.add(selected);
                            } else {
                                trackRecordingLabel.setVisibility(View.GONE);
                            }
                        }
                    }
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(selected, 15));
                }
            }

        };
        configureLocationService();
    }

    private void configureLocationService() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        if (checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PERMISSION_DENIED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            configureLocationService();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        listener.subscribeIsUpdateLocationStopped().observe(getViewLifecycleOwner(),
                observedIsUpdateLocationStopped ->
                        isUpdateLocationStopped = observedIsUpdateLocationStopped);
        listener.subscribeIsLocationSelected().observe(getViewLifecycleOwner(),
                observedIsLocationSelected -> isLocationSelected = observedIsLocationSelected);
        listener.subscribeIsRouteSelected().observe(getViewLifecycleOwner(),
                observedIsRouteSelected -> isRouteSelected = observedIsRouteSelected);
        listener.subscribeSelectedLocation().observe(getViewLifecycleOwner(),
                observedLocation -> selectedLocation = observedLocation);
        listener.subscribeSelectedRoute().observe(getViewLifecycleOwner(),
                observedSelectedRoute -> selectedRoute = observedSelectedRoute);

        FloatingActionButton addNewLocationFab = view.findViewById(R.id.addNewLocationFab);
        addNewLocationFab.setOnClickListener(this::onAddNewLocationFab);
        FloatingActionButton trackRecordFab = view.findViewById(R.id.trackRecordFab);
        trackRecordFab.setOnClickListener(this::onTrackRecordClick);

        trackRecordingLabel = view.findViewById(R.id.trackRecordingLabel);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Places.initialize(requireActivity(), getString(R.string.google_maps_key));
        AutocompleteSupportFragment autocompleteFragment =
                (AutocompleteSupportFragment) getChildFragmentManager()
                        .findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setPlaceFields(Arrays.asList(
                Place.Field.ID, Place.Field.LAT_LNG, Place.Field.ADDRESS));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                moveFocusToSelectedLocation(place);
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });
        mapView = view;
        return view;
    }

    private void moveFocusToSelectedLocation(Place place) {
        if (place != null) {
            double latitude = place.getLatLng().latitude;
            double longitude = place.getLatLng().longitude;
            UserLocation selectPlace =
                    new UserLocation(place.getAddress(), "", latitude, longitude);
            listener.selectLocation(selectPlace);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "Into onMapReady");
        map = googleMap;
        configureMap();
        drawAllLocationMarkers();
    }

    private void configureMap() {
        if (ActivityCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PERMISSION_DENIED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            configureMap();
        } else {
            map.getUiSettings().setZoomControlsEnabled(true);
            if (mapView != null && mapView.findViewById(Integer.parseInt("1")) != null) {
                // Get the button view
                View locationButton = ((View) mapView.findViewById(Integer.parseInt("1"))
                        .getParent()).findViewById(Integer.parseInt("2"));
                // and next place it, under AutoComplete (as Google Maps app)
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                        locationButton.getLayoutParams();
                // position under AutoComplete
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                layoutParams.setMargins(0, 180, 40, 0);
            }
            map.setMyLocationEnabled(true);
            map.setOnMyLocationButtonClickListener(this);
            map.getUiSettings().setCompassEnabled(true);
        }
    }

    private void drawAllLocationMarkers() {
        Log.d(TAG, "Into drawAllLocationMarkers");
        List<UserLocation> locations = listener.getLocations();
        if (locations != null) {
            Flowable.fromIterable(locations)
                    .subscribeOn(Schedulers.computation())
                    .map(userLocation ->
                            new Pair<>(userLocation, new LatLng(
                                    userLocation.getLatitude(),
                                    userLocation.getLongitude())))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(pair ->
                            map.addMarker(
                                    new MarkerOptions()
                                            .position(pair.second)
                                            .flat(true)
                                            .title(pair.first.getName())
                                            .snippet(pair.first.getTime()
                                            )));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, selectedLocation == null ? "null" : selectedLocation.toString());
        Log.d(TAG, String.valueOf(isUpdateLocationStopped));
        startLocationUpdates();
        if (map != null) {
            drawAllLocationMarkers();
        }
    }

    private void startLocationUpdates() {
        if (checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PERMISSION_DENIED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            startLocationUpdates();
        } else {
            createLocationRequest();
            fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    protected void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(MIN_TIME_FOR_REQUEST_LOCATION_UPDATE);
        locationRequest.setFastestInterval(MIN_TIME_FOR_REQUEST_LOCATION_UPDATE);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
    }

    public void onAddNewLocationFab(View view) {
        Log.d(TAG, "Into onAddNewLocationFab");
        Geocoder geocoder = new Geocoder(requireActivity(), Locale.getDefault());
        List<Address> addresses = new ArrayList<>();
        final Location currentLocation = this.currentLocation;
        if (currentLocation != null) {
            double latitude = currentLocation.getLatitude();
            double longitude = currentLocation.getLongitude();
            Single.just(currentLocation).subscribeOn(Schedulers.computation())
                    .map(curLoc -> {
                        addresses.addAll(
                                geocoder.getFromLocation(latitude, longitude, 1));
                        String locationName = addresses.get(0).getAddressLine(0);
                        String time = DateFormat.getDateTimeInstance()
                                .format(currentLocation.getTime());
                        listener.addNewLocation(
                                new UserLocation(locationName, time, latitude, longitude));
                        return curLoc;
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(curLoc -> map.addMarker(
                            new MarkerOptions().position(new LatLng(latitude, longitude))));
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Log.d(TAG, "Into onMyLocationButtonClick()");
        if (routePolyline != null) {
            routePolyline.remove();
        }
        if (marker != null) {
            marker.remove();
        }
        startLocationUpdates();
        listener.returnToCurrentLocation();
        return false;
    }

    void onTrackRecordClick(View view) {
        listener.returnToCurrentLocation();
        FloatingActionButton thisFab = (FloatingActionButton) view;
        if (isTrackRecordStarted) {
            isTrackRecordStarted = false;
            thisFab.setImageResource(R.drawable.ic_record_24);
            trackRecordingLabel.setVisibility(View.GONE);
            if (!savedLatLngs.isEmpty()) {
                saveRoute();
            }
        } else {
            isTrackRecordStarted = true;
            thisFab.setImageResource(R.drawable.ic_stop_24);
        }
    }

    private void saveRoute() {
        Geocoder geocoder = new Geocoder(requireActivity(), Locale.getDefault());
        StringBuffer startPointName = new StringBuffer();
        StringBuffer endPointName = new StringBuffer();
        try {
            startPointName.append(geocoder.getFromLocation(
                    savedLatLngs.get(0).latitude,
                    savedLatLngs.get(0).longitude,
                    1)
                    .get(0).getAddressLine(0));
            endPointName.append(geocoder.getFromLocation(
                    savedLatLngs.get(savedLatLngs.size() - 1).latitude,
                    savedLatLngs.get(savedLatLngs.size() - 1).longitude,
                    1).get(0).getAddressLine(0));
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }
        Flowable.fromIterable(savedLatLngs)
                .subscribeOn(Schedulers.computation())
                .map(latLng -> new RoutePoint(latLng.latitude, latLng.longitude))
                .collect(Collectors.toList())
                .doOnSuccess(routePoints -> listener.addNewRoute(
                        new UserRoute(startPointName.toString(),
                                endPointName.toString(),
                                routePoints)))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(v -> {
                    configurePolyline(savedLatLngs);
                    map.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(savedLatLngs.get(0), 15));
                    savedLatLngs.clear();
                });
    }

    private void configurePolyline(List<LatLng> latLngs) {
        if (routePolyline != null) {
            routePolyline.remove();
        }
        routePolyline = map.addPolyline(new PolylineOptions());
        routePolyline.setWidth(POLYLINE_STROKE_WIDTH_PX);
        routePolyline.setColor(getColor(requireActivity(), R.color.colorPrimary));
        routePolyline.setJointType(JointType.ROUND);
        routePolyline.setPoints(latLngs);
        routePolyline.setStartCap(new RoundCap());
        routePolyline.setEndCap(new RoundCap());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (routePolyline != null) {
            routePolyline.remove();
        }
        if (marker != null) {
            marker.remove();
        }
    }

    public interface MapFragmentListener {
        void addNewLocation(UserLocation location);

        void addNewRoute(UserRoute route);

        List<UserLocation> getLocations();

        LiveData<UserLocation> subscribeSelectedLocation();

        LiveData<Boolean> subscribeIsUpdateLocationStopped();

        LiveData<Boolean> subscribeIsLocationSelected();

        LiveData<Boolean> subscribeIsRouteSelected();

        LiveData<UserRoute> subscribeSelectedRoute();

        void returnToCurrentLocation();

        void selectLocation(UserLocation location);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            this.listener = (MapFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(String.format("Class %s must implement %s",
                    context.getClass().getSimpleName(), listener.getClass().getSimpleName()));
        }
    }

    @Override
    public void onDetach() {
        this.listener = null;
        super.onDetach();
    }
}