package haijun.root.where.util;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;

import haijun.root.where.receiver.TrackReceiver;

/**
 * Created by root on 10/27/16.
 */

public class TraceUtil {
    private static boolean isRegister = false;

    protected static PowerManager pm = null;

    public static PowerManager.WakeLock wakeLock = null;

    private static TrackReceiver trackReceiver = new TrackReceiver();

    public static void registerReveicer(Context context){
        if (!isRegister) {
            if (null == pm) {
                pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            }
            if (null == wakeLock) {
                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "track upload");
            }
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction("com.baidu.trace.action.GPS_STATUS");
            context.registerReceiver(trackReceiver, filter);
            isRegister = true;
        }
    }
}
