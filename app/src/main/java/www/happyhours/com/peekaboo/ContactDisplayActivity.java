package www.happyhours.com.peekaboo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


public class ContactDisplayActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener {


    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */

    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    public CharSequence mTitle;
    public GoogleMap mMap; // Might be null if Google Play services APK is not available.
    public GoogleApiClient mGoogleApiClient;
    public LocationRequest mLocationRequest;
    public Boolean mRequestingLocationUpdates;
    public Location mCurrentLocation;
    public String mResponse;
    public Double addLat;
    public ArrayList<LatLng> mPoints;

    public TextView mDisplay;
    public GoogleCloudMessaging gcm;
    public AtomicInteger msgId = new AtomicInteger();
    public SharedPreferences prefs;
    public Context context;
    public String regid;

    @Override
    public void onConnected(Bundle connectionHint) {

        if(mRequestingLocationUpdates)
        {
            startLocationUpdates();
        }



    }

    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();

        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }

    }
    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_display);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

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
        context = getApplicationContext();
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        /*Registration register = new Registration();
        register.execute();*/




    }
    class Registration extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... params) {
            String msg = "";
            try {
                if (gcm == null) {
                    gcm = GoogleCloudMessaging.getInstance(context);
                }
                regid = gcm.register("214619967549");
                msg = "Device registered, registration ID=" + regid;

            } catch (IOException ex) {
                msg = "Error :" + ex.getMessage();
                // If there is an error, don't just keep trying to register.
                // Require the user to click a button again, or perform
                // exponential back-off.
            }
            Log.i("Kaju",msg);
            return msg;

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(context,s,Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();

    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.contact_display, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        if(mRequestingLocationUpdates){
        UpdateUI ui = new UpdateUI();

        ui.execute();}
        else
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public  ListView friendsList;
        public String mResponse;
        class GetContacts extends AsyncTask<String,Void,String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(String... params) {
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                HttpClient httpclient = new DefaultHttpClient();

                HttpPost httppost = new HttpPost(Variables.serverHTTP);

                try {
                    // Add your data
                    Map<String, String> comment = new HashMap<String, String>();
                    comment.put("Username",Variables.userLoggedIn);
                    Map<String,Object> req = new HashMap<String, Object>();
                    req.put("RequestType","AppOpen");
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
                //s = "{\"ResponseType\"unsure emoticon"AppOpen\",\"Response\":{\"Friends\":[{\"Username\"unsure emoticon"anand.kanav\",\"FirstName\"unsure emoticon"Kanav\",\"LastName\"unsure emoticon"Anand\",\"LocationSharedTill\"unsure emoticon"23/05/2015 17:58:56\"}]}}";System.out.println(s);

                JsonElement jelement = new JsonParser().parse(s);
                JsonObject jobject = jelement.getAsJsonObject();
                String requestType;
                requestType = jobject.get("ResponseType").toString();
                String name = "",time = "",lati = "",longi = "";
                requestType = requestType.replaceAll("\"", "");
                final ArrayList<String> list = new ArrayList<String>();

                final List<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();

                if(requestType.equals("AppOpen"))
                {
                    jobject = jobject.getAsJsonObject("Response");
                    if(jobject !=  null)
                    {
                        JsonArray friendsArray = jobject.getAsJsonArray("Friends");



                        for (int i = 0; i < friendsArray.size() ; i++) {
                            HashMap<String, String> friendsMap = new HashMap<String, String>();
                            JsonObject friend = friendsArray.get(i).getAsJsonObject();
                            friendsMap.put("userName",friend.get("Username").toString().replaceAll("\"", "") );
                            friendsMap.put("fname", friend.get("FirstName").toString().replaceAll("\"", ""));
                            friendsMap.put("lname", friend.get("LastName").toString().replaceAll("\"", ""));
                            friendsMap.put("timestamp", friend.get("LocationSharedTill").toString().replaceAll("\"", ""));
                            data.add(friendsMap);
                            //   friendsMap.clear();
                            //   friendsMap = new HashMap<String,String>();

                            list.add(friend.get("FirstName").toString().replaceAll("\"", "")+" "+friend.get("LastName").toString().replaceAll("\"", ""));

                        }


                    }
                }
                if (abc.getActivity()!= null) {
                    final CustomAdapter adapter = new CustomAdapter(abc.getActivity(), list);
                    friendsList.setAdapter(adapter);
                    friendsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        @Override
                        public void onItemClick(AdapterView<?> parent, final View view,
                                                final int position, long id) {

                            final String item = (String) parent.getItemAtPosition(position);
                            String userName = data.get(position).get("userName");
                            Intent myintent = new Intent(abc.getActivity(), MapsActivity.class);
                            myintent.putExtra("userName", userName);
                            myintent.putExtra("isShowAll", "0");
                            startActivity(myintent);

                        }

                    });
                    //{"Response":{"Friend":"kanand","Latitude":"12.9353794","Longitude":"77.6944919","LastUpdated":"23/05/2015 18:38:07"},"ResponseType":"GetLocation"}
                }
            }


        }
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public PlaceholderFragment abc;
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
            abc = this;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_contact_display, container, false);
            friendsList = (ListView) rootView.findViewById(R.id.listview);
            GetContacts apiCall = new GetContacts();
            apiCall.execute();
//            String[] values = new String[] { "Android", "iPhone", "WindowsMobile",
//                    "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
//                    "Linux", "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux",
//                    "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux", "OS/2",
//                    "Android", "iPhone", "WindowsMobile" };





            return rootView;
        }





        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((ContactDisplayActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    class UpdateUI extends AsyncTask<String,Void,String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date(mCurrentLocation.getTime());
            final String formatted = format.format(date);
            final double currentLatitude = mCurrentLocation.getLatitude();
            final double currentLongitude = mCurrentLocation.getLongitude();
            HttpClient httpclient = new DefaultHttpClient();

            HttpPost httppost = new HttpPost(Variables.serverHTTP);

            try {
                // Add your data
                Map<String, String> comment = new HashMap<String, String>();
                comment.put("Latitude", String.valueOf(currentLatitude));
                comment.put("Longitude", String.valueOf(currentLongitude));
                comment.put("Username",Variables.userLoggedIn);
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
            mRequestingLocationUpdates = false;

        }


    }





}