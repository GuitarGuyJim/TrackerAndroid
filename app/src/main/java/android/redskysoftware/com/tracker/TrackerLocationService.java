package android.redskysoftware.com.tracker;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;

/**
 * This service is responsible for collecting user location and updating track related information.
 * This is done in this service so it will continue to happen, even when the Tracker activity is
 * not currently active.
 *
 * This service also runs a thread to keep track of how long the track is active (duration).  That
 * thread is started at the same time the service is started (and location data is requested).
 *
 * The collecting of location data and the duration task will both continue until the service is
 * stopped (the user of the service unbinds from this service).
 */
public class TrackerLocationService extends Service {

    /** Meters per degree of longitude at the equator */
    private final Double METERS_PER_DEGREE = 111319.9;

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        TrackerLocationService getService() {
            return TrackerLocationService.this;
        }
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    private LocationRequest mLocationRequest;

    private Location mPreviousLocation;
    private Location mCurrentLocation;
    private float mDistanceMeters;
    private boolean mFirstPosition = true;

    private long UPDATE_INTERVAL = 1 * 1000;  /* 5 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    @Override
    public void onCreate() {

        //startLocationCollecting();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        // Tell the user we stopped.
        // Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show();
        int break_here = 1;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void startLocationCollecting(Context context) {

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(context);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        try {
         //   Log.i(TAG, "requesting location");
            LocationServices.getFusedLocationProviderClient(context).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {

                            // New location is available...
                            synchronized (this) {

                                Location temp = locationResult.getLastLocation();
                                if (temp != null) {

                                    if (!mFirstPosition) {

                                        mCurrentLocation = temp;

                                        float accuracyMeters = 3.0f;

                                        if (mCurrentLocation.hasAccuracy()) {
                                            accuracyMeters = mCurrentLocation.getAccuracy();
                                        }

                                        float deltaMeters = mCurrentLocation.distanceTo(mPreviousLocation);

                                        if (deltaMeters >= accuracyMeters) {
                                            mPreviousLocation = mCurrentLocation;
                                            mDistanceMeters += deltaMeters;
                                        }

                                        //try {
                                        //    String str = String.format("%f %f %f\n",
                                        //            mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), deltaMeters);

                                        //    outputStream.write(str.getBytes());
                                        //    outputStream.flush();
                                        //} catch (IOException ioe) {
                                        //    int break_here = 1;
                                        //}

                                    } else {

                                        //
                                        // This is the first location we've received, so set our
                                        // current and previous to this location.
                                        //
                                        mCurrentLocation = temp;
                                        mPreviousLocation = temp;
                                    }
                                }

                                mFirstPosition = false;
                                //}
                            }
                        }
                    },
                    Looper.myLooper());
        } catch (SecurityException se) {
            int break_here = 1;
        }
    }

    public float getDistance() {

        float distance;

        synchronized (this) {
            distance = mDistanceMeters * 3.28f;
        }

        return distance;
    }

    public Location getCurrentLocation() {
        Location temp;

        synchronized (this) {
            temp = mCurrentLocation;
        }

        return temp;
    }
}
