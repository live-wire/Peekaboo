package www.happyhours.com.peekaboo;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by dbatheja on 5/26/2015.
 */
public class LatLngInterpolator {

    public LatLng interpolate(float fraction, LatLng a, LatLng b) {
        double lat = (b.latitude - a.latitude) * fraction + a.latitude;
        double lng = (b.longitude - a.longitude) * fraction + a.longitude;
        return new LatLng(lat, lng);
    }
}
