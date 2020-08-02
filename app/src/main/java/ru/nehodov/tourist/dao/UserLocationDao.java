package ru.nehodov.tourist.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import ru.nehodov.tourist.entities.UserLocation;

@Dao
public interface UserLocationDao {

    @Query("SELECT * FROM userlocation")
    LiveData<List<UserLocation>> getUserLocations();

    @Insert
    void insert(UserLocation userLocation);

    @Query("DELETE FROM userlocation")
    void deleteAll();
}
