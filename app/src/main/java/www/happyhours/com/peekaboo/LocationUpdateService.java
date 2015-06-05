package www.happyhours.com.peekaboo;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dbatheja on 6/5/2015.
 */
public class LocationUpdateService extends Service implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public Location mCurrentLocation;
    public GoogleApiClient mGoogleApiClient;
    public LocationRequest mLocationRequest;
    public Boolean mRequestingLocationUpdates;
    public Socket mSocket;
    public Context context;
    Emitter.Listener onNewMessage;
    Emitter.Listener onConnectError;
    public Boolean errorTunnel;
    public PowerManager mgr;
    public PowerManager.WakeLock wakeLock;

    public LocationUpdateService()
    {

    }




    @Override
    public void onCreate() {
        super.onCreate();
        errorTunnel = false;
        context = getApplicationContext();

        mgr = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");

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
        onNewMessage = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {


                JSONObject data = (JSONObject) args[0];
                String username;
                String message;
                try {
                    username = data.getString("username");
                    message = data.getString("message");
                } catch (JSONException e) {
                    return;
                }

                if (message.equalsIgnoreCase("shutup"))
                {
                    stopService();}
            }


        };

        onConnectError = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
               disconnectSocket();
                errorTunnel = true;
            }
        };
        connectSocket();
        mGoogleApiClient.connect();
        wakeLock.acquire();

    }

    public void connectSocket(){
        try {
            mSocket = IO.socket(Variables.CHAT_SERVER_URL);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        errorTunnel = false;
        mSocket.on("new message", onNewMessage);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.connect();
        mSocket.emit("add user", Variables.userLoggedIn);
        mSocket.open();
    }

    public void disconnectSocket()
    {
        mSocket.disconnect();
        mSocket.off("new message", onNewMessage);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.close();
    }

    public void stopService()
    {
        wakeLock.release();
        this.stopSelf();
    }
    @Override
    public void onConnected(Bundle bundle) {

            mGoogleApiClient.connect();


        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRequestingLocationUpdates = false;
        mGoogleApiClient.disconnect();
        disconnectSocket();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected void startLocationUpdates() {

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }
    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if(errorTunnel){
            restartService();
        }
        mCurrentLocation = location;
        if(mRequestingLocationUpdates){

            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date(mCurrentLocation.getTime());
            final String formatted = format.format(date);
            final double currentLatitude = mCurrentLocation.getLatitude();
            final double currentLongitude = mCurrentLocation.getLongitude();

            Map<String, String> comment = new HashMap<String, String>();
            comment.put("Latitude", String.valueOf(currentLatitude));
            comment.put("Longitude", String.valueOf(currentLongitude));
            comment.put("Username",Variables.userLoggedIn);
            comment.put("LastUpdated",formatted);
            Map<String,Object> req = new HashMap<String, Object>();
            req.put("RequestType","LocationUpdate");
            req.put("Request",comment);
            String json = new GsonBuilder().create().toJson(req, Map.class);

            if(mSocket.connected() && !errorTunnel)
            mSocket.emit("new message", json);



        }
        else
        {
            disconnectSocket();
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

    }
    public void restartService()
    {
        stopService();
        startService(new Intent(LocationUpdateService.this, LocationUpdateService.class));
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    int i =0;
    }



}
