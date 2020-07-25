package ru.nehodov.tourist.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import ru.nehodov.tourist.model.UserLocation;
import ru.nehodov.tourist.repository.TouristRepository;

public class TouristViewModel extends AndroidViewModel {

    private TouristRepository repository;
    private LiveData<List<UserLocation>> userLocationsLiveData;

    private List<UserLocation> userLocations;

    private MutableLiveData<UserLocation> selectedLocation = new MutableLiveData<>();

    private MutableLiveData<Boolean> isLocationSelected = new MutableLiveData<>();

    public TouristViewModel(Application application) {
        super(application);
        repository = new TouristRepository(application);
        userLocationsLiveData = repository.getAllLocations();
        isLocationSelected.setValue(Boolean.FALSE);
    }

    public LiveData<List<UserLocation>> getUserLocationsLiveData() {
        return this.userLocationsLiveData;
    }

    public void setUserLocations(List<UserLocation> userLocations) {
        this.userLocations = userLocations;
    }

    public List<UserLocation> getAllLocations() {
        return this.userLocations;
    }

    public void addLocation(UserLocation location) {
        repository.addLocation(location);
    }

    public MutableLiveData<UserLocation> getSelectedLocation() {
        return selectedLocation;
    }

    public MutableLiveData<Boolean> getIsLocationSelected() {
        return isLocationSelected;
    }

    public void setSelectedLocation(UserLocation selectedLocation) {
        this.selectedLocation.setValue(selectedLocation);
    }

    public void setIsLocationSelected(boolean isLocationSelected) {
        this.isLocationSelected.setValue(isLocationSelected);
    }
}
