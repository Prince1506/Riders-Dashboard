package rider.aptener.com.riderlivedrive.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by ist-128 on 25/5/17.
 */

public class ToastUtil {

    public final static String MOBILE_SHOULD_NOT_BE_LOCKED = "Mobile should not be locked while drive is in progress";
    public final static String TOAST_UNABLE_TO_INITIALISE_MAP_FRAG = "Unable to initialize Google mapFragment";
    public final static String TOAST_UNABLE_TO_INITIALISE_MAP = "Unable to initialize Google map";

    public static void showShortToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showLongToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
}
