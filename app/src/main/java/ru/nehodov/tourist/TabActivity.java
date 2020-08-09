package ru.nehodov.tourist;

import android.Manifest;
import android.location.Location;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;

import ru.nehodov.tourist.adapters.TouristPagerAdapter;
import ru.nehodov.tourist.entities.DirectionResults;
import ru.nehodov.tourist.entities.UserLocation;
import ru.nehodov.tourist.entities.UserRoute;
import ru.nehodov.tourist.viewmodel.TouristViewModel;

import static android.content.pm.PackageManager.PERMISSION_DENIED;

public class TabActivity extends AppCompatActivity
        implements LocationListFragment.LocationListFragmentListener,
        MapFragmentListener, RouteListFragment.RouteListFragmentListener {

    private static final int MAP_ITEM_NUMBER = 0;
    private static final int LOCATION_LIST_ITEM_NUMBER = 1;
    private static final int ROUTES_LIST_ITEM_NUMBER = 2;

    private ActivityResultLauncher<String> requestPermissionLauncher;

    private TouristViewModel viewModel;

    private ViewPager2 viewPager;

    private String[] tabNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        init();
                    } else {
                        Snackbar.make(
                                findViewById(R.id.activity_tab),
                                R.string.permission_rationale,
                                Snackbar.LENGTH_LONG)
                                .show();
                    }
                });
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PERMISSION_DENIED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            init();
        }
    }

    public void init() {
        setContentView(R.layout.activity_tab);
        viewModel = new ViewModelProvider(this).get(TouristViewModel.class);
        viewModel.observeData(this);

        TabLayout tabs = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.viewPager);
        viewPager.setUserInputEnabled(false);

        tabNames = new String[]{
                getString(R.string.map),
                getString(R.string.location_list),
                getString(R.string.route_list)};

        TouristPagerAdapter touristPagerAdapter = new TouristPagerAdapter(this);
        viewPager.setAdapter(touristPagerAdapter);
        new TabLayoutMediator(tabs, viewPager,
                (tab, position) -> tab.setText(tabNames[position])).attach();
    }

    @Override
    public List<UserLocation> getLocations() {
        return viewModel.getAllLocations();
    }

    @Override
    public void addNewLocation(UserLocation location) {
        viewModel.addLocation(location);
    }

    @Override
    public void addNewRoute(UserRoute route) {
        viewModel.addRoute(route);
    }

    @Override
    public void selectLocation(UserLocation location) {
        viewModel.setSelectedLocation(location);
        viewPager.setCurrentItem(MAP_ITEM_NUMBER);
    }

    @Override
    public void selectRoute(UserRoute route) {
        viewModel.setSelectedRoute(route);
        viewPager.setCurrentItem(MAP_ITEM_NUMBER);
    }

    @Override
    public LiveData<UserLocation> subscribeSelectedLocation() {
        return viewModel.getSelectedLocation();
    }

    @Override
    public LiveData<Boolean> subscribeIsUpdateLocationStopped() {
        return viewModel.getIsLocationUpdateStopped();
    }

    @Override
    public LiveData<UserRoute> subscribeSelectedRoute() {
        return viewModel.getSelectedRoute();
    }

    @Override
    public LiveData<Boolean> subscribeIsLocationSelected() {
        return viewModel.getIsLocationSelected();
    }

    @Override
    public LiveData<Boolean> subscribeIsRouteSelected() {
        return viewModel.getIsRouteSelected();
    }

    @Override
    public List<UserRoute> getRoutes() {
        return viewModel.getAllRoutes();
    }

    @Override
    public LiveData<DirectionResults> subscribeDirectionResults() {
        return viewModel.getDirectionResultsData();
    }

    @Override
    public void askRoute(Location origin,
                         Location destination,
                         String key,
                         RouteCallback callback) {
        viewModel.askRoute(origin, destination, key, callback);
    }

    @Override
    public void returnToCurrentLocation() {
        viewModel.setIsLocationUpdateStopped(false);
        viewModel.setIsRouteSelected(false);
        viewModel.setIsLocationSelected(false);
    }
}