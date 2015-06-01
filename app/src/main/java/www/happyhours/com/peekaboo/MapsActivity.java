package www.happyhours.com.peekaboo;

import www.happyhours.com.peekaboo.*;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;

import com.google.android.gms.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.Property;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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

    public LatLng mFriendLoc;
    public LatLng mSelfLoc;
    public Location mCurrentLocation;
    public String mResponse;
    public int addLat;
    ArrayList<LatLng> mPoints;
    public Marker mLastMarkerSelf;
    public Marker mLastMarkerFriend;
    public ArrayList<Marker> mLastMarkerFriendArray;
    public String mFriendName;
    public String requestType;
    public ScheduledExecutorService scheduleTaskExecutor;
    public ScheduledExecutorService scheduleTaskExecutor2;
    public Polyline polylineRoad;
    public LatLngInterpolator latLngInterpolator;
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
        addLat = 0;

        mLastMarkerFriendArray = new ArrayList<Marker>();
        mPoints = new ArrayList<LatLng>();
         Button b = (Button) findViewById(R.id.Refresh);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRequestingLocationUpdates = true;
                mGoogleApiClient.connect();
                //startLocationUpdates();

                if(requestType.equals("0")){
                    findViewById(R.id.togglebutton).setVisibility(View.VISIBLE);
                    UpdateFriend updateFriend = new UpdateFriend();
                    updateFriend.execute();


                }
                else if(requestType.equals("1"))
                {
                    findViewById(R.id.togglebutton).setVisibility(View.INVISIBLE);
                    addLat = 0;
                    for(int i = 0; i < mLastMarkerFriendArray.size();i++)
                    {
                        mLastMarkerFriendArray.get(i).remove();
                    }
                    mLastMarkerFriendArray.clear();
                    DisplayAllFriends displayAllFriends = new DisplayAllFriends();
                    displayAllFriends.execute();
                }

            }
        });

        ToggleButton toggleButton = (ToggleButton)findViewById(R.id.togglebutton);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    String url = getDirectionsUrl(mFriendLoc , mSelfLoc);

                    DownloadTask downloadTask = new DownloadTask();

                    downloadTask.execute(url);
                    // The toggle is enabled
                } else {

                    UpdateFriend updateFriend = new UpdateFriend();
                    updateFriend.execute();
                    if(polylineRoad!=null)
                        polylineRoad.remove();
                    // The toggle is disabled
                }
            }
        });
        latLngInterpolator = new LatLngInterpolator();
        mFriendName = getIntent().getStringExtra("userName");
        requestType = getIntent().getStringExtra("isShowAll");
        if(requestType.equals("0")){
            findViewById(R.id.togglebutton).setVisibility(View.VISIBLE);
                UpdateFriend updateFriend = new UpdateFriend();
        updateFriend.execute();}
        else if(requestType.equals("1"))
        {
            findViewById(R.id.togglebutton).setVisibility(View.INVISIBLE);
            addLat = 0;

            for(int i = 0; i < mLastMarkerFriendArray.size();i++)
            {
                mLastMarkerFriendArray.get(i).remove();
            }
            mLastMarkerFriendArray.clear();
            DisplayAllFriends displayAllFriends = new DisplayAllFriends();
            displayAllFriends.execute();
        }

    }
    static void animateMarkerToICS(Marker marker, LatLng finalPosition, final LatLngInterpolator latLngInterpolator) {
        TypeEvaluator<LatLng> typeEvaluator = new TypeEvaluator<LatLng>() {
            @Override
            public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
                return latLngInterpolator.interpolate(fraction, startValue, endValue);
            }
        };
        Property<Marker, LatLng> property = Property.of(Marker.class, LatLng.class, "position");
        ObjectAnimator animator = ObjectAnimator.ofObject(marker, property, typeEvaluator, finalPosition);
        animator.setDuration(3000);
        animator.start();
    }

    @Override
    public void onConnected(Bundle connectionHint) {

        if(mRequestingLocationUpdates)
        {
            if(!mGoogleApiClient.isConnected())
                mGoogleApiClient.connect();
            startLocationUpdates();
        }
        //     Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
        //           mGoogleApiClient);

    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }


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
    protected void onDestroy() {
        super.onDestroy();
        if (scheduleTaskExecutor2 != null)
            scheduleTaskExecutor2.shutdownNow();
        if (scheduleTaskExecutor != null)
            scheduleTaskExecutor.shutdownNow();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (scheduleTaskExecutor2 != null)
            scheduleTaskExecutor2.shutdownNow();
        if (scheduleTaskExecutor != null)
            scheduleTaskExecutor.shutdownNow();
        //     if (mGoogleApiClient.isConnected()) {
        //       LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (com.google.android.gms.location.LocationListener) this);
        //     mGoogleApiClient.disconnect();}
    }

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
       // mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        //handleNewLocation(location);
        mCurrentLocation = location;
        mSelfLoc = new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date(mCurrentLocation.getTime());
        final String formatted = format.format(date);
        final double currentLatitude = mCurrentLocation.getLatitude();
        final double currentLongitude = mCurrentLocation.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);
        if(mLastMarkerSelf!=null)
            mLastMarkerSelf.remove();
        MarkerOptions options = new MarkerOptions()

                .position(latLng)
                .title("I am here!")
                .snippet("Last Updated:" + formatted)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                .anchor(0.5f, 1);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
        mLastMarkerSelf = mMap.addMarker(options);
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        //mMap.animateCamera(zoom);
        mRequestingLocationUpdates = false;

    }

    class UpdateFriend extends AsyncTask<String,Void,String>
    {

        @Override
        protected String doInBackground(String... params) {

            HttpClient httpclient = new DefaultHttpClient();

            HttpPost httppost = new HttpPost(Variables.serverHTTP);

            try {

                Map<String, String> comment = new HashMap<String, String>();
                comment.put("Username", Variables.userLoggedIn);
                comment.put("Friend", mFriendName);
                Map<String,Object> req = new HashMap<String, Object>();
                req.put("RequestType","GetLocation");
                req.put("Request",comment);
                String json = new GsonBuilder().create().toJson(req, Map.class);
                httppost.setEntity(new StringEntity(json));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity httpEntity = response.getEntity();
                String responseString = EntityUtils.toString(httpEntity);
                mResponse = responseString;


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

            JsonElement jelement = new JsonParser().parse(s);
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
            double currentLongitude = Double.valueOf(longi);
            LatLng latLng = new LatLng(currentLatitude, currentLongitude);
            mFriendLoc = latLng;
            mPoints.add(latLng);
            //if(mLastMarkerFriend!=null)
            //mLastMarkerFriend.remove();
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(name+" is here!")
                    .snippet("Last Updated:" + time);
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(10);
            if(!(mLastMarkerFriend!=null))
            mLastMarkerFriend = mMap.addMarker(options);
            animateMarkerToICS(mLastMarkerFriend,latLng,latLngInterpolator);
            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            builder.include(mLastMarkerSelf.getPosition());
            builder.include(mLastMarkerFriend.getPosition());

            LatLngBounds bounds = builder.build();
            int padding = 100; // offset from edges of the map in pixels
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            mMap.animateCamera(cu);
            mLastMarkerFriend.showInfoWindow();

            PolylineOptions lineOptions = new PolylineOptions();
            lineOptions.addAll(mPoints);
            mMap.addPolyline(lineOptions.width(10).color(Color.BLUE).geodesic(true));

           /* scheduleTaskExecutor= Executors.newScheduledThreadPool(1);
// This schedule a task to run every 20 seconds:
            scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    // involved your call in UI thread:
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Boolean a = findViewById(R.id.Refresh).performClick();
                        }
                    });
                }
            }, 0, 2, TimeUnit.SECONDS);*/
//{"Response":{"Friend":"kanand","Latitude":"12.9353794","Longitude":"77.6944919","LastUpdated":"23/05/2015 18:38:07"},"ResponseType":"GetLocation"}


        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }

    class DisplayAllFriends extends AsyncTask<String,Void,String>
    {

        @Override
        protected String doInBackground(String... params) {

            HttpClient httpclient = new DefaultHttpClient();

            HttpPost httppost = new HttpPost(Variables.serverHTTP);

            try {

                Map<String, String> comment = new HashMap<String, String>();
                comment.put("Username", Variables.userLoggedIn);
                Map<String,Object> req = new HashMap<String, Object>();
                req.put("RequestType","TrackAll");
                req.put("Request",comment);
                String json = new GsonBuilder().create().toJson(req, Map.class);
                httppost.setEntity(new StringEntity(json));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity httpEntity = response.getEntity();
                String responseString = EntityUtils.toString(httpEntity);
                mResponse = responseString;


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

            JsonElement jelement = new JsonParser().parse(s);
            JsonObject jobject = jelement.getAsJsonObject();
            String jobject2;
            jobject2 = jobject.get("ResponseType").toString();
            String fname = "",lname = "",time = "",lati = "",longi = "";
            jobject2 = jobject2.replaceAll("\"", "");
            JsonArray jarray;
            if(jobject2.equals("TrackAll"))
            {
                jobject = jobject.getAsJsonObject("Response");
                if(jobject !=  null)
                {
                    jarray = jobject.getAsJsonArray("Friends");
                    for (int i = 0; i < jarray.size() ; i++) {

                    jobject = jarray.get(i).getAsJsonObject();
                    fname = jobject.get("FirstName").toString();
                    fname = fname.replaceAll("\"", "");
                    lname = jobject.get("LastName").toString();
                    lname = lname.replaceAll("\"", "");
                    time = jobject.get("LastUpdated").toString();
                    time = time.replaceAll("\"", "");
                    lati = jobject.get("Latitude").toString();
                    lati = lati.replaceAll("\"", "");
                    longi = jobject.get("Longitude").toString();
                    longi = longi.replaceAll("\"", "");
                    double currentLatitude = Double.valueOf(lati);
                    double currentLongitude = Double.valueOf(longi);
                    LatLng latLng = new LatLng(currentLatitude, currentLongitude);
                    mPoints.add(latLng);
                    MarkerOptions options = new MarkerOptions()
                            .position(latLng)
                            .title(fname+" is here!")
                            .snippet("Last Updated:" + time);
                    CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
                    mLastMarkerFriendArray.add(mMap.addMarker(options)) ;
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(zoom);
                    addLat+=1;}

                }
            }
            /*scheduleTaskExecutor2= Executors.newScheduledThreadPool(1);
            scheduleTaskExecutor2.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    // involved your call in UI thread:
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Boolean a = findViewById(R.id.Refresh).performClick();
                        }
                    });
                }
            }, 0, 2, TimeUnit.SECONDS);*/


//{"Response":{"Friend":"kanand","Latitude":"12.9353794","Longitude":"77.6944919","LastUpdated":"23/05/2015 18:38:07"},"ResponseType":"GetLocation"}
            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            builder.include(mLastMarkerSelf.getPosition());
            for(int a =0;a<mLastMarkerFriendArray.size();a++) {
                builder.include(mLastMarkerFriendArray.get(a).getPosition());
            }
            LatLngBounds bounds = builder.build();
            int padding = 100; // offset from edges of the map in pixels
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            mMap.animateCamera(cu);



        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }
    /*String url = getDirectionsUrl(origin, dest);

    DownloadTask downloadTask = new DownloadTask();

    downloadTask.execute(url);*/
    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        // Output format
        String output = "json";

        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }

    private String downloadUrl(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Exception while downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    public class DownloadTask extends AsyncTask<String, Void, String>{

        // Downloading data in non-ui thread


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            findViewById(R.id.togglebutton).setEnabled(false);
        }

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            JSONObject jObject = null;
            List<List<HashMap<String, String>>> routes = null;
            DirectionsJSONParser parser = new DirectionsJSONParser();
            try {
                jObject = new JSONObject(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // Starts parsing data
            routes = parser.parse(jObject);
            /*ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);*/

            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
            String distance = "";
            String duration = "";

            if(routes.size()<1){
            Toast.makeText(getBaseContext(), "No Points", Toast.LENGTH_SHORT).show();
            return;
            }
            // Traversing through all the routes
            for(int i=0;i<routes.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = routes.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);
                    if(j==0){// Get distance from the list
                       distance = (String)point.get("distance");
                       continue;
                        }else if(j==1){ // Get duration from the list
                        duration = (String)point.get("duration");
                        continue;
                        }
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(4);
                lineOptions.color(Color.RED);
            }

            // Drawing polyline in the Google Map for the i-th route
            polylineRoad = mMap.addPolyline(lineOptions);
            Toast.makeText(getBaseContext(),"Duration:"+duration+" Distance:"+distance,Toast.LENGTH_SHORT).show();
            LatLngBounds.Builder builder = new LatLngBounds.Builder();

                builder.include(mLastMarkerSelf.getPosition());
                builder.include(mLastMarkerFriend.getPosition());

            LatLngBounds bounds = builder.build();
            int padding = 100; // offset from edges of the map in pixels
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            mMap.animateCamera(cu);
            mLastMarkerFriend.showInfoWindow();

            findViewById(R.id.togglebutton).setEnabled(true);
        }
    }




}