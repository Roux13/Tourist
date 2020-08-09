package ru.nehodov.tourist.entities;

import com.google.gson.annotations.SerializedName;

public class Steps {
    @SerializedName("start_location")
    private android.location.Location startLocation;
    @SerializedName("end_location")
    private android.location.Location endLocation;
    private OverviewPolyLine polyline;

    public android.location.Location getStartLocation() {
        return startLocation;
    }

    public android.location.Location getEndLocation() {
        return endLocation;
    }

    public OverviewPolyLine getPolyline() {
        return polyline;
    }
}
