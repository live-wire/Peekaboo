package www.happyhours.com.peekaboo;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Created by dbatheja on 6/5/2015.
 */
public class LocationUpdateReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, LocationUpdateService.class);
        startWakefulService(context,service);
    }
}
