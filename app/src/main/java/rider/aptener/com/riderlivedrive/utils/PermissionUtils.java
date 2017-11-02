package rider.aptener.com.riderlivedrive.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

/**
 * Created by root on 22/5/17.
 */

public class PermissionUtils {
    private static PermissionUtils _instance;
    private Context mContext;

    public PermissionUtils(Context mContext) {
        this.mContext = mContext;
    }

    public static synchronized void initialize(Context context) {
        if (_instance != null) {
            return;
            //throw new IllegalStateException("Extra call to initialize analytics trackers");
        }

        _instance = new PermissionUtils(context);
    }

    public static synchronized PermissionUtils getInstance() {
        if (_instance == null) {
            throw new IllegalStateException("Call initialize() before getInstance()");
        }
        return _instance;
    }

    ///check write storage permission
    public boolean checkWriteStoragePermissionGranted() {
        boolean permission = checkEachPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return permission;
    }

    //check phone state permission
    public boolean checkPhoneStatePermissionGranted() {
        boolean permission = checkEachPermission(Manifest.permission.READ_PHONE_STATE);
        return permission;
    }

    //check location permission
    public boolean checkCoarseLocationPermissionGranted() {
        try {
            boolean permissionCourseLocation = checkEachPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            boolean permissionFineLocation = checkEachPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            return (permissionCourseLocation || permissionFineLocation);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //check call log permission
    public boolean checkReadCallLogPermissionGranted() {
        boolean permission = checkEachPermission(Manifest.permission.READ_CALL_LOG);
        return permission;
    }

    //check all permission
    public boolean checkAllPermissionGranted() {
        return (checkWriteStoragePermissionGranted() && checkPhoneStatePermissionGranted() && checkCoarseLocationPermissionGranted());
    }

    //check any permission is denied
    public boolean checkAllPermissionDenied() {
        return (!checkWriteStoragePermissionGranted() || !checkPhoneStatePermissionGranted() || !checkCoarseLocationPermissionGranted());
    }

    public boolean checkEachPermission(String permission) {
        boolean permissioinStatus = false;
        try {
            if (permission != null) {
                int permissioin = ActivityCompat.checkSelfPermission(mContext, permission);
                if (permissioin == PackageManager.PERMISSION_GRANTED) {
                    permissioinStatus = true;
                } else {
                    permissioinStatus = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return permissioinStatus;
    }

    public boolean checkAudioPermission() {
        boolean permission = checkEachPermission(Manifest.permission.RECORD_AUDIO);
        return permission;
    }

    //check Wpt all permission
}
