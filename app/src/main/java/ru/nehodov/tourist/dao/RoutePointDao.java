package ru.nehodov.tourist.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import ru.nehodov.tourist.entities.RoutePoint;

@Dao
public interface RoutePointDao {

    @Insert
    void insert(List<RoutePoint> routePoints);

    @Query("SELECT * FROM routepoint")
    LiveData<List<RoutePoint>> getAllRoutePoints();

}
