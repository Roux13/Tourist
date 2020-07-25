package ru.nehodov.tourist;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;

import ru.nehodov.tourist.adapters.TouristPagerAdapter;
import ru.nehodov.tourist.model.UserLocation;
import ru.nehodov.tourist.viewmodel.TouristViewModel;

import static android.content.pm.PackageManager.PERMISSION_DENIED;

public class TabActivity extends AppCompatActivity
        implements LocationListFragment.LocationListFragmentListener,
        MapFragment.MapFragmentListener {

    private static final int MAP_ITEM_NUMBER = 0;
    private static final int LOCATIONLIST_ITEM_NUMBER = 1;

    private ActivityResultLauncher<String> requestPermissionLauncher;

    private TouristViewModel viewModel;

    private TabLayout tabs;
    private ViewPager2 viewPager;
    private TouristPagerAdapter touristPagerAdapter;

    private String[] tabNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        init();
                    } else {
                        Toast.makeText(
                                this,
                                "The location updates is unavailable because the location "
                                + "updates requires a ACCESS_FINE_LOCATION permission.",
                                Toast.LENGTH_LONG)
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
        viewModel.getUserLocationsLiveData().observe(this, viewModel::setUserLocations);

        tabs = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.viewPager);
        viewPager.setUserInputEnabled(false);

        tabNames = new String[]{getString(R.string.map), getString(R.string.location_list)};

        touristPagerAdapter = new TouristPagerAdapter(this);
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
    public void selectLocation(UserLocation location) {
        viewModel.setSelectedLocation(location);
        viewModel.setIsLocationSelected(true);
        viewPager.setCurrentItem(MAP_ITEM_NUMBER);
    }

    @Override
    public LiveData<UserLocation> subscribeSelectedLocation() {
        return viewModel.getSelectedLocation();
    }

    @Override
    public LiveData<Boolean> subscribeIsLocationSelected() {
        return viewModel.getIsLocationSelected();
    }

    @Override
    public void returnToCurrentLocation() {
        viewModel.setIsLocationSelected(false);
    }
}