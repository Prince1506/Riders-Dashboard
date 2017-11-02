package rider.aptener.com.riderlivedrive.services;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;


/**
 * Created by ist on 2/11/17.
 */

public class LocationService extends Service implements LocationListener {
    private static final long MIN_TIME_INTERVAL_FOR_GPS_LOCATION = 1000;
    private static final float MIN_DISTANCE_INTERVAL_FOR_GPS_LOCATION = 5.0f;
    private static String TAG = LocationService.class.getSimpleName();
    private static Location mCurrentLocation;
    private LocationManager locationManager;
    private Location gpsLocation, networkLocation;

    public static Location getmCurrentLocation() {
        return mCurrentLocation;
    }

    public static void setmCurrentLocation(Location mCurrentLocation) {
        LocationService.mCurrentLocation = mCurrentLocation;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            Log.i(TAG, "onCreate...");
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_INTERVAL_FOR_GPS_LOCATION, MIN_DISTANCE_INTERVAL_FOR_GPS_LOCATION, this);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_INTERVAL_FOR_GPS_LOCATION, MIN_DISTANCE_INTERVAL_FOR_GPS_LOCATION, this);
//        Criteria criteria = new Criteria();
//        criteria.setAccuracy(Criteria.ACCURACY_FINE);
//		criteria.setSpeedAccuracy(Criteria.ACCURACY_HIGH);
//		criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
//		criteria.setBearingAccuracy(Criteria.ACCURACY_HIGH);
//		criteria.setSpeedRequired(true);
//		locationManager.getBestProvider(criteria, true);
            mCurrentLocation = getBestLocation();
            Log.d(TAG, "mCurrentLocation : " + mCurrentLocation);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Location getBestLocation() {
        Log.d(TAG, "getBestLocation...");
        try {
            Location location_gps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location location_network = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//		 If both are available, get the most recent
            if (location_gps != null && location_network != null) {
                return (location_gps.getAccuracy() < location_network.getAccuracy()) ?
                        location_gps : location_network;
            } else if (location_gps == null && location_network == null) {
                return null;
            } else {
                return (location_gps == null) ? location_network : location_gps;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
//		return location_gps;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged...provider : " + location.getProvider());
        try {
            if (LocationManager.GPS_PROVIDER.equals(location.getProvider())) {
                gpsLocation = location;
            } else if
                    (LocationManager.NETWORK_PROVIDER.equals(location.getProvider())) {
                networkLocation = location;
            }
            if (gpsLocation != null && networkLocation != null) {
//            if (Math.abs(gpsLocation.getTime() - networkLocation.getTime()) >
//                    MIN_TIME_INTERVAL_FOR_GPS_LOCATION) {
//                if (gpsLocation.getTime() > networkLocation.getTime()) {
//                    mCurrentLocation = gpsLocation;
//                } else {
//                    mCurrentLocation = networkLocation;
//                }
//            } else {
                Log.d(TAG, "gpsAccuracy():" + gpsLocation.getAccuracy() + " and networkLAccuracy():" + networkLocation.getAccuracy());
                if (gpsLocation.getAccuracy() < networkLocation.getAccuracy()) {
                    mCurrentLocation = gpsLocation;
                } else {
                    mCurrentLocation = networkLocation;
                }
                //}
            } else if (gpsLocation != null) {
                Log.d(TAG, "gpsLocation");
                mCurrentLocation = gpsLocation;
            } else if (networkLocation != null) {
                Log.d(TAG, "networkLocation");
                mCurrentLocation = networkLocation;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//		mCurrentLocation = gpsLocation;
    }

    @Override
    public void onProviderDisabled(String provider) {
        System.out.println("onProviderDisabled, provider : " + provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        System.out.println("onProviderEnabled, provider : " + provider);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        System.out.println("onStatusChanged, provider : " + provider);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy...");
        locationManager.removeUpdates(this);
    }

//	@Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        super.onStartCommand(intent, flags, startId);
//                /* Register this SensorEventListener with Android sensor service */
//        return START_STICKY;
//    }

}
