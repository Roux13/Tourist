package ru.nehodov.tourist.entities;

import com.google.gson.annotations.SerializedName;

public class OverviewPolyLine {

    @SerializedName("points")
    private String points;

    public String getPoints() {
        return points;
    }
}
