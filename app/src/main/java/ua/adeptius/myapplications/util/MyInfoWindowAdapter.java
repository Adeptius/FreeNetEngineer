package ua.adeptius.myapplications.util;

import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;


public class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    @Override
    public View getInfoWindow(Marker marker) {
        System.out.println("getInfoWindow " + marker);
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        System.out.println("getInfoContents " + marker);
        return null;
    }
}
