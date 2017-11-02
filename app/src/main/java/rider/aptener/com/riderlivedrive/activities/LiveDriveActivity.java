package rider.aptener.com.riderlivedrive.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import rider.aptener.com.riderlivedrive.R;
import rider.aptener.com.riderlivedrive.constants.RyderConstants;
import rider.aptener.com.riderlivedrive.fragments.LiveDriveFragment;
import rider.aptener.com.riderlivedrive.utils.PermissionUtils;
import rider.aptener.com.riderlivedrive.utils.ToastUtil;

public class LiveDriveActivity extends AppCompatActivity {

    public static TextView tvBack, tvHeadingName, tvMoreHeading;
    public static boolean isActivityFinish = false;
    private static boolean isReturnedFromSettings;
    public ImageView imgLogo;
    public long startedOn;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    AlertDialog mAlertDialog;
    private String TAG = LiveDriveActivity.class.getSimpleName();
    private Context mContext;
    private LinearLayout layoutActionBar, layoutAppLogo;
    private PowerManager.WakeLock mWakeLock;
    private ScreenReceiver mReceiver;

    public static void replaceDriveFragment(Context context, Fragment fragment) {
        int CONTAINER_ID = R.id.fragment_live_drive;
        FragmentTransaction fragmentTransaction = ((LiveDriveActivity) context).fragmentManager.beginTransaction();
        fragmentTransaction.replace(CONTAINER_ID, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commitAllowingStateLoss();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = LiveDriveActivity.this;
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        initWidget();

        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "Service");
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);
        if (!PermissionUtils.getInstance().checkCoarseLocationPermissionGranted()) {
            requestPermissions();
        }
        replaceDriveFragment(mContext, LiveDriveFragment.getInstance());
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermissions() {
        Log.d("marsh", "inside requestPermissions");
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, RyderConstants.REQUEST_LOCATION_CODE);
        } else {
            Log.d("marsh", "inside requestPermissions else");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, RyderConstants.REQUEST_LOCATION_CODE);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case RyderConstants.REQUEST_LOCATION_CODE: {
                try {
                    for (int i = 0; i < permissions.length; i++) {
                        String permission = permissions[i];
                        if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                            // user rejected the permission
                            boolean showRationale = shouldShowRequestPermissionRationale(permission);
                            if (!showRationale) {
                                try {
                                    showPermissionPopup();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                            } else {
                                finish();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } catch (Error e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "Inside onStart");
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause()");
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Inside onResume");
        if (isReturnedFromSettings) {
            isReturnedFromSettings = false;
            if (!PermissionUtils.getInstance().checkCoarseLocationPermissionGranted()) {
                showPermissionPopup();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "Inside onStop");
    }


//	public static boolean replaceDriveFragment(Context context, FragmentManager fragmentManager, Fragment fragment) {
//		try{
//			int CONTAINER_ID = R.id.fragment_mosvoice;
//			FragmentTransaction fragmentTransaction = ((MOSVoiceActivity) context).fragmentManager.beginTransaction();
//			fragmentTransaction.replace(CONTAINER_ID, fragment);
//			fragmentTransaction.addToBackStack(null);
//			fragmentTransaction.commitAllowingStateLoss();
//		}catch(Exception e){
//			e.printStackTrace();
//			return false;
//		}
//		return true;
//
//	}

    @Override
    protected void onRestart() {
        Log.d(TAG, "onRestart()");
        super.onRestart();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void initWidget() {
        layoutActionBar = (LinearLayout) findViewById(R.id.ll_action_bar);
        LinearLayout actionBar = (LinearLayout) getLayoutInflater().inflate(R.layout.layout_actionbar, null);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutActionBar.addView(actionBar, layoutParams);
        imgLogo = (ImageView) findViewById(R.id.img_netvelocity_icon);
        imgLogo.setImageResource(R.drawable.rydr_logo);
        tvMoreHeading = (TextView) actionBar.findViewById(R.id.text_more);
        tvMoreHeading.setVisibility(View.GONE);
        tvBack = (TextView) actionBar.getChildAt(0);
        tvHeadingName = (TextView) actionBar.getChildAt(1);
        tvBack.setVisibility(View.VISIBLE);
        tvHeadingName.setVisibility(View.VISIBLE);
        imgLogo.setVisibility(View.GONE);
        tvHeadingName.setText(RyderConstants.DRIVE_TEST_TITLE);

        layoutAppLogo = ((LinearLayout) actionBar.getChildAt(2));
        layoutAppLogo.setGravity(Gravity.RIGHT);
        tvBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                tvBack.setClickable(true);
                onBackPressed();

            }
        });
    }

    private void turnScreenOn() {
        int flags = WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;
        getWindow().addFlags(flags);
    }

    private void acquireWakeLock() {
        try {
            mWakeLock.acquire();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void releaseWakeLock() {
        try {
            mWakeLock.release();
        } catch (Exception e) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
        isActivityFinish = false;

    }

    public void showPermissionPopup() {
        try {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle(" ");
            alertDialog.setIcon(R.drawable.rydr_logo);
            alertDialog.setMessage(getResources().getString(R.string.permission_request_msg));
            alertDialog.setCancelable(false);
            alertDialog.setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    isReturnedFromSettings = true;
                    Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                    myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                    myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (mContext != null && !(((LiveDriveActivity) mContext).isDestroyed())) {
                        startActivity(myAppSettings);
                    }
                }
            });
            alertDialog.setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    isReturnedFromSettings = false;
                    finish();
                }
            });
            mAlertDialog = alertDialog.create();
            if (mAlertDialog != null && !mAlertDialog.isShowing()) {
                mAlertDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class ScreenReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                if (LiveDriveFragment.getInstance().isLiveDriveRunning) {
                    turnScreenOn();
                    acquireWakeLock();
                }
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                if (LiveDriveFragment.getInstance().isLiveDriveRunning) {
                    releaseWakeLock();
                    ToastUtil.showLongToast(context, ToastUtil.MOBILE_SHOULD_NOT_BE_LOCKED);
                }
            }
        }

    }
}