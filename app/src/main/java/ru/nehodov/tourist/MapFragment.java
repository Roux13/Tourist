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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;

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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.text.DateFormat;
import java.util.List;
import java.util.Locale;

import ru.nehodov.tourist.model.UserLocation;

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static androidx.core.content.ContextCompat.checkSelfPermission;

public class MapFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener {

    private static final String TAG = "MapFragment.class";
    private static final int MIN_TIME_FOR_REQUEST_LOCATION_UPDATE = 1000;

    private ActivityResultLauncher<String> requestPermissionLauncher;

    private MapFragmentListener listener;

    private GoogleMap map;

    private LocationRequest locationRequest;

    private LocationCallback locationCallback;

    private FusedLocationProviderClient fusedLocationClient;

    private Location currentLocation;
    private boolean isSelectedLocation;
    private UserLocation selectedLocation;

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
                LatLng selected;
                if (locationResult != null) {
                    Log.d(TAG, "Into onLocationResult");
                    if (isSelectedLocation) {
                        selected = new LatLng(
                                selectedLocation.getLatitude(), selectedLocation.getLongitude());
                        Marker marker = map.addMarker(new MarkerOptions()
                                .position(selected)
                                .icon(BitmapDescriptorFactory
                                        .defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                                .title(selectedLocation.getName())
                                .snippet(selectedLocation.getTime()));
                        marker.showInfoWindow();
                        stopLocationUpdates();
                    } else {
                        currentLocation = locationResult.getLastLocation();
                        selected = new LatLng(
                                currentLocation.getLatitude(), currentLocation.getLongitude());
                    }
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(selected, 15));
                }
            }

        };

        configureLocationService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        listener.subscribeIsLocationSelected().observe(
                getViewLifecycleOwner(),
                observedIsSelectedLocation -> isSelectedLocation = observedIsSelectedLocation);
        listener.subscribeSelectedLocation().observe(
                getViewLifecycleOwner(), observedLocation -> selectedLocation = observedLocation);

        FloatingActionButton addNewLocationFab = view.findViewById(R.id.addNewLocationFab);

        addNewLocationFab.setOnClickListener(this::onAddNewLocationFab);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        return view;
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
            map.setMyLocationEnabled(true);
            map.setOnMyLocationButtonClickListener(this);
        }
    }

    private void drawAllLocationMarkers() {
        Log.d(TAG, "Into drawAllLocationMarkers");
        List<UserLocation> locations = listener.getLocations();
        if (locations != null) {
            locations.stream()
                    .map(userLocation -> new Pair<>(userLocation,
                            new LatLng(userLocation.getLatitude(), userLocation.getLongitude())))
                    .forEach(pair ->
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
        Log.d(TAG, String.valueOf(isSelectedLocation));
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
        List<Address> addresses;
        final Location currentLocation = this.currentLocation;
        try {
            if (currentLocation != null) {
                double latitude = currentLocation.getLatitude();
                double longitude = currentLocation.getLongitude();
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
                String locationName = addresses.get(0).getAddressLine(0);
                String time = DateFormat.getDateTimeInstance().format(currentLocation.getTime());
                listener.addNewLocation(new UserLocation(locationName, time, latitude, longitude));
                map.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        startLocationUpdates();
        listener.returnToCurrentLocation();
        return false;
    }

    public interface MapFragmentListener {
        void addNewLocation(UserLocation location);

        List<UserLocation> getLocations();

        LiveData<UserLocation> subscribeSelectedLocation();

        LiveData<Boolean> subscribeIsLocationSelected();

        void returnToCurrentLocation();
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