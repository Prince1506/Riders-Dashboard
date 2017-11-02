package rider.aptener.com.riderlivedrive.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Date;

import rider.aptener.com.riderlivedrive.R;
import rider.aptener.com.riderlivedrive.activities.LiveDriveActivity;
import rider.aptener.com.riderlivedrive.constants.RyderConstants;
import rider.aptener.com.riderlivedrive.services.LocationService;
import rider.aptener.com.riderlivedrive.timers.StopWatch;
import rider.aptener.com.riderlivedrive.utils.LiveDriveUtils;
import rider.aptener.com.riderlivedrive.utils.PermissionUtils;
import rider.aptener.com.riderlivedrive.utils.ToastUtil;

import static rider.aptener.com.riderlivedrive.constants.RyderConstants.MSG_START_TIMER;
import static rider.aptener.com.riderlivedrive.constants.RyderConstants.MSG_UPDATE_TIMER;

/**
 * Created by ist on 2/11/17.
 */

public class LiveDriveFragment extends android.support.v4.app.Fragment {
    static LiveDriveFragment _instance;
    public boolean isLiveDriveRunning;
    public boolean isStopTestClicked = false;
    String TAG = LiveDriveFragment.class.getSimpleName();
    private View rootView;
    private LinearLayout mosMapLayout, llStartStopMOSDrive;
    private TextView tvSpeed, tvDistance, tvTime, tvmosDriveTime;
    private HorizontalScrollView hsvSpeedParamsBottom;
    private ImageView imgBtnStopNewMOSDrive;
    private SupportMapFragment mapFrag;
    private GoogleMap map;
    private boolean isFirstMarker;
    private Intent locService;
    private LocationManager gpsLocationManager;
    private PowerManager.WakeLock mWakeLock;
    private StopWatch stopWatch;
    private long startedOn;
    private LiveDriveActivity driveActivity;
    private Double longitude, latitude;
    private Dialog stopTestDialog;
    private Location location;
    private long currentTime;
    private Location prevLocation;
    private long prevTime;
    private long hours;
    private long minutes;
    private long seconds;
    private String driveTime;
    private String totalTime;
    private float speed;
    private float distance;
    @SuppressLint("HandlerLeak")
    Handler stopWatchHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_START_TIMER:
                    try {
                        if (stopWatch != null) {
                            stopWatch.start();
                            stopWatchHandler.sendEmptyMessage(MSG_UPDATE_TIMER);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "stopWatch started");
                    break;

                case MSG_UPDATE_TIMER:
                    try {
                        if (!PermissionUtils.getInstance().checkCoarseLocationPermissionGranted()) {
                            ((LiveDriveActivity) getContext()).requestPermissions();
                        }
                        getCurrentLocation();
                        if (location != null) {
                            Log.d("drive-test", "latlngs : " + location.getLatitude() + ", " + location.getLongitude());
                        }
                        if (stopWatch != null) {
                            hours = stopWatch.getElapsedTimeHour();
                            minutes = stopWatch.getElapsedTimeMin();
                            seconds = stopWatch.getElapsedTimeSecs();
                        }
                        driveTime = LiveDriveUtils.getInstance(getContext()).getFormatedTime(hours, minutes, seconds);
                        totalTime = driveTime;

                        Log.d("drive-test", "stopWatch:" + driveTime);
                        if (driveTime != null) {
                            tvTime.setText("" + driveTime);
                        } else {
                            tvTime.setText(RyderConstants.DASH);
                        }

                        calculateDistance();

                        if (speed >= 0) {
                            tvSpeed.setText(String.format("%.2f", speed * 3.6f));
                        }


                        if (distance > 0) {
                            tvDistance.setText(String.format("%.2f", (distance / 1000)));
                        } else {
                            tvDistance.setText(String.format("%.2f", 0.00));
                        }
                        setMarkerOnMap();
                        if (stopWatchHandler != null)
                            stopWatchHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIMER, RyderConstants.REFRESH_RATE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break; // though the  is still running

                case RyderConstants.MSG_STOP_TIMER:
                    try {
                        if (stopWatchHandler != null)
                            stopWatchHandler.removeMessages(MSG_UPDATE_TIMER); // no more
                        if (stopWatch != null) {
                            stopWatch.stop();// stop timer
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                default:
                    break;
            }
        }
    };

    public static LiveDriveFragment getInstance() {
        if (_instance == null) {
            _instance = new LiveDriveFragment();
        }
        return _instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PermissionUtils.getInstance().checkCoarseLocationPermissionGranted();
        driveActivity = (LiveDriveActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.drive_test, container, false);
        }
        initView(rootView);
        isFirstMarker = true;
        loadMapFragment();
        stopWatch = new StopWatch();
        registerServices();
        startCapturing();
        return rootView;

    }

    private void initView(View rootView) {
        mosMapLayout = rootView.findViewById(R.id.mosMapLayout);
        tvSpeed = rootView.findViewById(R.id.tvSpeedMOSDrive);
        tvDistance = rootView.findViewById(R.id.tvDistanceMOSDrive);
        tvTime = rootView.findViewById(R.id.tvTimerMOSDrive);
        hsvSpeedParamsBottom = rootView.findViewById(R.id.hsvSpeedParamsBottom);
        imgBtnStopNewMOSDrive = rootView.findViewById(R.id.imgBtnStopNewMOSDrive);
        llStartStopMOSDrive = rootView.findViewById(R.id.llStartStopMOSDrive);
    }

    private void loadMapFragment() {
        try {
            mapFrag = getMapFragment();

            if (mapFrag != null) {
                map = mapFrag.getMap();
            } else {
                ToastUtil.showLongToast(getContext(), ToastUtil.TOAST_UNABLE_TO_INITIALISE_MAP_FRAG);
                return;
            }
            if (map == null) {
                ToastUtil.showLongToast(getContext(), ToastUtil.TOAST_UNABLE_TO_INITIALISE_MAP);
                return;
            }
            map.setMyLocationEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(false);
//        map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
//        map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(false);
            map.getUiSettings().setCompassEnabled(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private SupportMapFragment getMapFragment() {
        try {
            FragmentManager fm = null;

            Log.d(TAG, "sdk: " + Build.VERSION.SDK_INT);
            Log.d(TAG, "release: " + Build.VERSION.RELEASE);

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                Log.d(TAG, "using getFragmentManager");
                fm = getFragmentManager();
            } else {
                Log.d(TAG, "using getChildFragmentManager");
                fm = getChildFragmentManager();
            }

            return (SupportMapFragment) fm.findFragmentById(R.id.mosMapFragment);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void registerServices() {
        try {
            // initialise and start location service
            locService = new Intent(getActivity(), LocationService.class);
            getActivity().startService(locService);
            gpsLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            // initialise and acquire power lock for managing screen lock
            PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "WakeLock");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startCapturing() {
        try {
            startedOn = new Date().getTime();
            driveActivity.startedOn = startedOn;
            stopWatchHandler.sendEmptyMessage(MSG_START_TIMER);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getCurrentLocation() {
        try {
            location = LocationService.getmCurrentLocation();
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setMarkerOnMap() {
        try {
            if (latitude != null && longitude != null) {
                int marker = R.drawable.marker_blue_dot;
                if (isFirstMarker) {
                    isFirstMarker = false;
//                setCurrentLocationToMap();
                    marker = R.drawable.pin_green_start;
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 16));
                } else {
                    marker = R.drawable.marker_blue_dot;
                }
//            String selectedParam = getMarkerByNwType(networkType);
//            int marker = getMarkerForSignalParams(selectedParam);
                map.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).icon(BitmapDescriptorFactory.fromResource(marker)).anchor(0.5f, 0.5f));
                map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void endApplication() {
        ((LiveDriveActivity) getContext()).finish();
    }

    private void calculateDistance() {
        try {
            latitude = null;
            longitude = null;

            Location location = LocationService.getmCurrentLocation();
            currentTime = new Date().getTime();

            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                //Log.d("gps", "Location:" + location + " " + latitude + " " + longitude);

                if (prevLocation != null) {
                    Log.d("drive-test", "distance before  " + location.distanceTo(prevLocation));
//                    if (!((currentTime - prevTime) > RyderConstants.TEN_SECONDS && location.distanceTo(prevLocation) > RyderConstants.ONE_KILOMETER)) {
                    distance += location.distanceTo(prevLocation);
                    Log.d(TAG, "totalDistance" + distance);
                    long totalTime = LiveDriveUtils.getInstance(getContext()).getTotalTime(hours, minutes, seconds);
                    if (totalTime != 0) {
                        speed = distance / totalTime;
                    }
                    Log.d("drive-test", "speed" + speed);
//                    }
                }
                prevLocation = location;
                prevTime = currentTime;
            } else {
                latitude = null;
                longitude = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("drive-test", "onDestroyView");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("drive-test", "onDestroy");
        _instance = null;
        stopWatchHandler.sendEmptyMessage(RyderConstants.MSG_STOP_TIMER);
    }
}
