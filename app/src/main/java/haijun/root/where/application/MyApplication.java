package haijun.root.where.application;

import android.app.Application;
import android.content.SharedPreferences;

import com.baidu.mapapi.SDKInitializer;

/**
 * Created by root on 10/23/16.
 */

public class MyApplication extends Application {
    public static SharedPreferences locationinf;
    @Override
    public void onCreate() {
        super.onCreate();

        locationinf = getSharedPreferences("configure",MODE_PRIVATE);

        SDKInitializer.initialize(this);
    }
}
