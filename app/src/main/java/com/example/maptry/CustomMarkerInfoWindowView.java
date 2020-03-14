package com.example.maptry;

import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomMarkerInfoWindowView implements GoogleMap.InfoWindowAdapter {
    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
//      //  private final View markerItemView;
//        public CustomMarkerInfoWindowView() {
//            View layoutInflater = null;
//          //  markerItemView = layoutInflater.inflate(R.layout.marker_info_window, null);  // 1
//        }
//        @Override
//        public View getInfoWindow(Marker marker) { // 2
//            User user = (User) marker.getTag();  // 3
//            if (user == null) return null;
//            TextView itemNameTextView = markerItemView.findViewById(R.id.itemNameTextView);
//            TextView itemAddressTextView = markerItemView.findViewById(R.id.itemAddressTextView);
//            itemNameTextView.setText(marker.getTitle());
//            itemAddressTextView.setText(user.getAddress());
//            return markerItemView;  // 4
//        }
//        @Override
//        public View getInfoContents(Marker marker) {
//            return null;
//        }
//
}
