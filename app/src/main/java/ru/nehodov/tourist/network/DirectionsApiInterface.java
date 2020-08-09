package ru.nehodov.tourist.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import ru.nehodov.tourist.entities.DirectionResults;

public interface DirectionsApiInterface {

    @GET("maps/api/directions/json?")
    Call<DirectionResults> getDirectionResults(@Query("origin") String origin,
                                               @Query("destination") String destination,
                                               @Query("key") String key);
}
