package ru.nehodov.tourist.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkService {

    private static final String TAG = NetworkService.class.getSimpleName();

    private static final String DIRECTIONS_API_URL =
            "https://maps.googleapis.com/";

    private final DirectionsApiInterface directionsApi;

    public NetworkService() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);

        OkHttpClient loggingClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(DIRECTIONS_API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(loggingClient)
                .build();
        directionsApi = retrofit.create(DirectionsApiInterface.class);
    }

    public DirectionsApiInterface getDirectionsApi() {
        return this.directionsApi;
    }
}
