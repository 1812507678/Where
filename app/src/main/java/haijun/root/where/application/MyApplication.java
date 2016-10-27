package haijun.root.where.application;

import android.app.Application;
import android.content.SharedPreferences;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.LocationMode;
import com.baidu.trace.Trace;

/**
 * Created by root on 10/23/16.
 */

public class MyApplication extends Application {
    /**
     * 轨迹服务
     */
    public static Trace trace = null;

    /**
     * 轨迹服务客户端
     */
    public static LBSTraceClient client = null;

    /**
     * 鹰眼服务ID，开发者创建的鹰眼服务对应的服务ID
     */
    public static int serviceId = 127542 ;

    /**
     * entity标识
     */
    private String entityName = "c1";

    /**
     * 轨迹服务类型（0 : 不建立socket长连接， 1 : 建立socket长连接但不上传位置数据，2 : 建立socket长连接并上传位置数据）
     */
    private int traceType = 2;

    public static SharedPreferences locationinf;

    @Override
    public void onCreate() {
        super.onCreate();

        locationinf = getSharedPreferences("configure",MODE_PRIVATE);

        //百度地图初始化
        SDKInitializer.initialize(this);
        // 初始化轨迹服务客户端
        client = new LBSTraceClient(this);
        // 初始化轨迹服务
        trace = new Trace(this, serviceId, entityName, traceType);
        // 设置定位模式
        client.setLocationMode(LocationMode.High_Accuracy);
    }

}
