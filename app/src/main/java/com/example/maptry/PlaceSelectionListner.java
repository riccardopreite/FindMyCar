package com.example.maptry;

import android.util.Log;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import static com.android.volley.VolleyLog.TAG;

public class PlaceSelectionListner implements PlaceSelectionListener {
    @Override
    public void onPlaceSelected(Place place) {
        Log.i( TAG, "An porcoo: " + place );

    }

    @Override
    public void onError(Status status) {
        Log.i( TAG, "An error occurred: " + status );
    }
}
