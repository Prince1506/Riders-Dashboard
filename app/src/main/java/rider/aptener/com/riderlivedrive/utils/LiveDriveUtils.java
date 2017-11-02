package rider.aptener.com.riderlivedrive.utils;

import android.content.Context;

import rider.aptener.com.riderlivedrive.constants.RyderConstants;

/**
 * Created by ist on 2/11/17.
 */

public class LiveDriveUtils {
    private static LiveDriveUtils _instance;
    private Context context;

    public LiveDriveUtils(Context context) {
        this.context = context;
    }

    public static LiveDriveUtils getInstance(Context context) {
        if (_instance == null) {
            _instance = new LiveDriveUtils(context);
        }
        return _instance;
    }

    public String getFormatedHrMin(long hours, long minutes) {
        String hr = "";
        String min = "";
        if (hours < 10) {
            hr = RyderConstants.ZERO + hours;
        } else {
            hr = hours + "";
        }
        if (minutes < 10) {
            min = RyderConstants.ZERO + minutes;
        } else {
            min = minutes + "";
        }

        return hr + ":" + min;
    }

    public String getFormatedTime(long hours, long minutes, long seconds) {
        String hr = "";
        String min = "";
        String sec = "";
        if (hours < 10) {
            hr = RyderConstants.ZERO + hours;
        } else {
            hr = hours + "";
        }
        if (minutes < 10) {
            min = RyderConstants.ZERO + minutes;
        } else {
            min = minutes + "";
        }
        if (seconds < 10) {
            sec = RyderConstants.ZERO + seconds;
        } else {
            sec = seconds + "";
        }
        return hr + ":" + min + ":" + sec;
    }


    public long getTotalTime(long hours, long minutes, long seconds) {
        long totalTime = hours * 60l * 60l;
        totalTime += minutes * 60;
        totalTime += seconds;
        return totalTime;
    }

}
