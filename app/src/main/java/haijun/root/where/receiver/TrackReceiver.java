package haijun.root.where.receiver;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import haijun.root.where.util.TraceUtil;

public class TrackReceiver extends BroadcastReceiver {

    private static final String TAG = "TrackReceiver";

    @SuppressLint("Wakelock")
    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();

        if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            Log.i(TAG,"onReceive:screen off,acquire wake lock!");
            System.out.println("screen off,acquire wake lock!");
            if (null != TraceUtil.wakeLock && !(TraceUtil.wakeLock.isHeld())) {
                TraceUtil.wakeLock.acquire();
            }
        } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
            Log.i(TAG,"onReceive:screen on,release wake lock!");
            if (null != TraceUtil.wakeLock && TraceUtil.wakeLock.isHeld()) {
                TraceUtil.wakeLock.release();
            }
        } else if ("com.baidu.trace.action.GPS_STATUS".equals(action)) {
            int statusCode = intent.getIntExtra("statusCode", 0);
            String statusMessage = intent.getStringExtra("statusMessage");
            Log.i(TAG,"onReceive:GPS状态码"+statusCode+",statusMessage:"+statusMessage);

            Toast.makeText(context, statusMessage, Toast.LENGTH_SHORT).show();
        }
    }

}
