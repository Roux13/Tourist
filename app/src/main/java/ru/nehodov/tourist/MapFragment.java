package ru.nehodov.tourist;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.text.DateFormat;
import java.util.List;
import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import ru.nehodov.tourist.model.UserLocation;

import static android.content.pm.PackageManager.*;
import static androidx.core.content.ContextCompat.*;

public class MapFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener {

    private static final String TAG = "MapFragment.class";
    private static final int MIN_TIME_FOR_REQUEST_LOCATION_UPDATE = 1000;

    private MapFragmentListener listener;

    private GoogleMap map;

    private FusedLocationProviderClient fusedLocationClient;

    private LocationListener locationListener;

    private Location location;
    private boolean isSelectedLocation;
    private UserLocation selectedLocation;

    private List<UserLocation> locations;

    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        FloatingActionButton addNewLocationFab = view.findViewById(R.id.addNewLocationFab);

        listener.subscribeIsLocationSelected().observe(
                getViewLifecycleOwner(),
                observedIsSelectedLocation -> isSelectedLocation = observedIsSelectedLocation);
        listener.subscribeSelectedLocation().observe(
                getViewLifecycleOwner(), observedLocation -> selectedLocation = observedLocation);

        addNewLocationFab.setOnClickListener(this::onAddNewLocationFab);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        if (checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(
                    requireActivity(),
                    location -> {
                        if (location != null) {
                            Log.d(TAG, "Into addOnSuccessListener");
                            MapFragment.this.location = location;
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(
                                            location.getLatitude(),
                                            location.getLongitude()),
                                    150));
                        }
                    });
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, selectedLocation == null ? "null" : selectedLocation.toString());
        Log.d(TAG, String.valueOf(isSelectedLocation));
        if (map != null) {
            drawAllLocationMarkers();
        }
    }

    private void drawAllLocationMarkers() {
        Log.d(TAG, "Into drawAllLocationMarkers");
        locations = listener.getLocations();
        if (locations != null) {
            locations.stream()
                    .map(userLocation -> new Pair<UserLocation, LatLng>(userLocation,
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
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "Into onMapReady");
        map = googleMap;
        map.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(true);
        map.setOnMyLocationButtonClickListener(this);
        drawAllLocationMarkers();
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, "Into onLocationChanged");
                if (isSelectedLocation) {
                    LatLng selected = new LatLng(
                            selectedLocation.getLatitude(),
                            selectedLocation.getLongitude());
                    Marker marker = map.addMarker(new MarkerOptions()
                            .position(new LatLng(selectedLocation.getLatitude(),
                                    selectedLocation.getLongitude()))
                            .icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                            .title(selectedLocation.getName())
                            .snippet(selectedLocation.getTime()));
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(selected, 15));
                    marker.showInfoWindow();
                } else {
                    MapFragment.this.location = location;
                    LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 15));
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) { }

            @Override
            public void onProviderEnabled(String s) { }

            @Override
            public void onProviderDisabled(String s) { }
        };
        LocationManager locationManager =
                (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME_FOR_REQUEST_LOCATION_UPDATE,
                0,
                locationListener);
    }

    public void onAddNewLocationFab(View view) {
        Log.d(TAG, "Into onAddNewLocationFab");
        Geocoder geocoder = new Geocoder(requireActivity(), Locale.getDefault());
        List<Address> addresses;
        try {
            if (location != null) {
                addresses = geocoder.getFromLocation(
                        location.getLatitude(),
                        location.getLongitude(),
                        1);
                String locationName = addresses.get(0).getAddressLine(0);
                String time = DateFormat.getDateTimeInstance().format(location.getTime());
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                listener.addNewLocation(new UserLocation(locationName, time, latitude, longitude));
                map.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)));
//                Single.just("").subscribeOn(Schedulers.computation())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .doOnDispose(() -> {
//                            String locationName = addresses.get(0).getAddressLine(0);
//                            String time = DateFormat.getDateTimeInstance()
//                                    .format(location.getTime());
//                            double latitude = location.getLatitude();
//                            double longitude = location.getLongitude();
//                            listener.addNewLocation(new UserLocation(
//                                    locationName,
//                                    time,
//                                    latitude,
//                                    longitude)
//                            ); }).subscribe((v) -> map.addMarker(new MarkerOptions().position(
//                                new LatLng(
//                                        location.getLatitude(),
//                                        location.getLongitude()))));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
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