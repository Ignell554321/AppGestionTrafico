package com.example.appgestiontrafico.Servicios;

import android.content.Context;

import com.example.appgestiontrafico.R;
import com.example.appgestiontrafico.Retrofit.IGoogleApi;
import com.example.appgestiontrafico.Retrofit.RetrofitClient;
import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

import retrofit2.Call;

public class GoogleApiService {

    private Context context;


    public GoogleApiService(Context context) {
        this.context = context;
    }

    public Call<String> getDirections(LatLng originLatLng, LatLng destinationLatLng) {
        String baseUrl = "https://maps.googleapis.com";
        String query = "/maps/api/directions/json?mode=driving&transit_routing_preferences=less_driving&"
                + "origin=" + originLatLng.latitude + "," + originLatLng.longitude + "&"
                + "destination=" + destinationLatLng.latitude + "," + destinationLatLng.longitude + "&"
                + "departure_time=" + (new Date().getTime() + (60*60*1000)) + "&"
                + "traffic_model=best_guess&"
                + "key=" + context.getResources().getString(R.string.google_maps_key);
        return RetrofitClient.getClient(baseUrl).create(IGoogleApi.class).getDirections(baseUrl + query);
    }
}
