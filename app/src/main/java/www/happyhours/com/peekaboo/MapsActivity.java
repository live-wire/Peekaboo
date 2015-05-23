package www.happyhours.com.peekaboo;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;

import com.google.android.gms.internal.la;
import com.google.android.gms.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/*
Date date = formatter.parse(toParse); // You will need try/catch around this
        long millis = date.getTime();
        long time= System.currentTimeMillis();
        */

public class MapsActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    public GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    public Boolean mRequestingLocationUpdates;
    public Location mCurrentLocation;
    public String mResponse;
    public Double addLat;
    ArrayList<LatLng> mPoints;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setUpMapIfNeeded();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mRequestingLocationUpdates = true;
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)        // 10 seconds, in milliseconds
                .setFastestInterval(5000); // 1 second, in milliseconds
        addLat = 0.1;
        mPoints = new ArrayList<LatLng>();
    }

    @Override
    public void onConnected(Bundle connectionHint) {

        if(mRequestingLocationUpdates)
        {startLocationUpdates();
        }
        //     Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
        //           mGoogleApiClient);

    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    //    public void handleNewLocation(Location mLastLocation)
//    {
//        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
//        Date date = new Date(mLastLocation.getTime());
//        final String formatted = format.format(date);
//        final double currentLatitude = mLastLocation.getLatitude();
//        final double currentLongitude = mLastLocation.getLongitude();
//        LatLng latLng = new LatLng(currentLatitude, currentLongitude);
//        mMap.clear();
//        MarkerOptions options = new MarkerOptions()
//                .position(latLng)
//                .title("I am here!")
//                .snippet("Last Updated:" + formatted);
//        CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
//        mMap.addMarker(options);
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//        mMap.animateCamera(zoom);
//        new Thread(){
//
//            public void run() {
//                HttpClient httpclient = new DefaultHttpClient();
//
//                HttpPost httppost = new HttpPost("http://3cd66297.ngrok.io");
//
//                try {
//                    // Add your data
//                    Map<String, String> comment = new HashMap<String, String>();
//                    comment.put("Latitude", String.valueOf(currentLatitude));
//                    comment.put("Longitude", String.valueOf(currentLongitude));
//                    comment.put("BusNumber","KA-03-2929");
//                    comment.put("BusName","KIA6");
//                    comment.put("Contact","9945630500");
//                    comment.put("LastUpdated",formatted);
//                    Map<String,Object> req = new HashMap<String, Object>();
//                    req.put("RequestType","AddressUpdate");
//                    req.put("Request",comment);
//                    String json = new GsonBuilder().create().toJson(req, Map.class);
//                    httppost.setEntity(new StringEntity(json));
//
//                    // Execute HTTP Post Request
//                    HttpResponse response = httpclient.execute(httppost);
//                    HttpEntity httpEntity = response.getEntity();
//                    String responseString = EntityUtils.toString(httpEntity);
//
//                } catch (ClientProtocolException e) {
//                    // TODO Auto-generated catch block
//                } catch (IOException e) {
//                    // TODO Auto-generated catch block
//                }
//                // Toast.makeText(getApplicationContext(),SimpleHttpPut.poster(),Toast.LENGTH_SHORT).show();
//                //mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
//            }
//        }.start();
//    }
    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        mGoogleApiClient.connect();

        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        //     if (mGoogleApiClient.isConnected()) {
        //       LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (com.google.android.gms.location.LocationListener) this);
        //     mGoogleApiClient.disconnect();}
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        //handleNewLocation(location);
        mCurrentLocation = location;
        UpdateUI ui = new UpdateUI();

        ui.execute();
    }


    class UpdateUI extends AsyncTask<String,Void,String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date(mCurrentLocation.getTime());
            final String formatted = format.format(date);
            final double currentLatitude = mCurrentLocation.getLatitude();
            final double currentLongitude = mCurrentLocation.getLongitude();
            LatLng latLng = new LatLng(currentLatitude, currentLongitude);
            mMap.clear();
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title("I am here!")
                    .snippet("Last Updated:" + formatted);
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
            mMap.addMarker(options);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(zoom);
        }

        @Override
        protected String doInBackground(String... params) {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date(mCurrentLocation.getTime());
            final String formatted = format.format(date);
            final double currentLatitude = mCurrentLocation.getLatitude();
            final double currentLongitude = mCurrentLocation.getLongitude();
            HttpClient httpclient = new DefaultHttpClient();

            HttpPost httppost = new HttpPost("http://4b171156.ngrok.io");

            try {
                // Add your data
                Map<String, String> comment = new HashMap<String, String>();
                comment.put("Latitude", String.valueOf(currentLatitude));
                comment.put("Longitude", String.valueOf(currentLongitude));
                comment.put("Username","batheja.dhruv");
                comment.put("LastUpdated",formatted);
                Map<String,Object> req = new HashMap<String, Object>();
                req.put("RequestType","LocationUpdate");
                req.put("Request",comment);
                String json = new GsonBuilder().create().toJson(req, Map.class);
                httppost.setEntity(new StringEntity(json));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity httpEntity = response.getEntity();
                String responseString = EntityUtils.toString(httpEntity);
                mResponse = responseString;
                /*Map<String, String> comment = new HashMap<String, String>();
                comment.put("Username", "batheja.dhruv");
                comment.put("Friend", "anand.kanav");
                Map<String,Object> req = new HashMap<String, Object>();
                req.put("RequestType","GetLocation");
                req.put("Request",comment);
                String json = new GsonBuilder().create().toJson(req, Map.class);
                httppost.setEntity(new StringEntity(json));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity httpEntity = response.getEntity();
                String responseString = EntityUtils.toString(httpEntity);
                mResponse = responseString;*/


            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
            } catch (IOException e) {
                // TODO Auto-generated catch block
            }

            return mResponse;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            /*JsonElement jelement = new JsonParser().parse(s);
            JsonObject jobject = jelement.getAsJsonObject();
            String jobject2;
            jobject2 = jobject.get("ResponseType").toString();
            String name = "",time = "",lati = "",longi = "";
            jobject2 = jobject2.replaceAll("\"", "");

            if(jobject2.equals("GetLocation"))
            {
                jobject = jobject.getAsJsonObject("Response");
                if(jobject !=  null)
                {
                    name = jobject.get("Friend").toString();
                    name = name.replaceAll("\"", "");
                    time = jobject.get("LastUpdated").toString();
                    time = time.replaceAll("\"", "");
                    lati = jobject.get("Latitude").toString();
                    lati = lati.replaceAll("\"", "");
                    longi = jobject.get("Longitude").toString();
                    longi = longi.replaceAll("\"", "");

                }
            }


            double currentLatitude = Double.valueOf(lati);
            final double currLatitude = currentLatitude + addLat;
            double currentLongitude = Double.valueOf(longi);
            final double currLongitude = currentLongitude + addLat;
            LatLng latLng = new LatLng(currLatitude, currentLongitude);
            mPoints.add(latLng);
            mMap.clear();
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(name+" is here!")
                    .snippet("Last Updated:" + time);
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
            mMap.addMarker(options);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(zoom);
            addLat+=addLat;
            PolylineOptions lineOptions = new PolylineOptions();
            lineOptions.addAll(mPoints);
            mMap.addPolyline(lineOptions.width(6).color(Color.MAGENTA).geodesic(true));*/
//{"Response":{"Friend":"kanand","Latitude":"12.9353794","Longitude":"77.6944919","LastUpdated":"23/05/2015 18:38:07"},"ResponseType":"GetLocation"}

        }


    }

}
