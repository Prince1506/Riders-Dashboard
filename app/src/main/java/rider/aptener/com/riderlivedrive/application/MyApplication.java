package rider.aptener.com.riderlivedrive.application;

import android.app.Application;

import rider.aptener.com.riderlivedrive.utils.PermissionUtils;

/**
 * Created by ist on 2/11/17.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        PermissionUtils.initialize(getApplicationContext());
    }
}
