package haijun.root.where.util;

import android.content.Context;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import haijun.root.where.service.LocationService;


/**
 * Created by root on 10/25/16.
 */

public class ShowLocationOnMap {
    static MapView mMapView;
    private static final String TAG = "ShowLocationOnMap";
    private static LocationService locationService;

    public static void showPointOnMap(double latitude,double longitude,MapView mMapView) {
        if (mMapView!=null){
            ShowLocationOnMap.mMapView = mMapView;
        }
        BaiduMap map = ShowLocationOnMap.mMapView.getMap();
        LatLng latLng = new LatLng(latitude, longitude);

        //在地图上显示所在位置
        map.setMyLocationEnabled(true);  //设置为位置显示
        MyLocationData.Builder localbuild = new MyLocationData.Builder();
        localbuild.latitude(latitude);
        localbuild.longitude(longitude);
        MyLocationData myLocationData = localbuild.build();
        map.setMyLocationData(myLocationData);

        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLngZoom(latLng, 17); //17为地图的显示比例，比例范围是3-19
        map.animateMapStatus(msu);
    }

    public static void startLocation(Context context,MapView mMapView) {
        ShowLocationOnMap.mMapView = mMapView;
        // -----------location config ------------
        locationService = new LocationService(context);
        //获取locationservice实例，建议应用中只初始化1个location实例，然后使用，可以参考其他示例的activity，都是通过此种方式获取locationservice实例的
        locationService.registerListener(mListener);
        //注册监听
        locationService.setLocationOption(locationService.getDefaultLocationClientOption());
        locationService.start();// 定位SDK
    }


    protected void stopLocation() {
        locationService.unregisterListener(mListener); //注销掉监听
        locationService.stop(); //停止定位服务
    }


    /*****
     *
     * 定位结果回调，重写onReceiveLocation方法，可以直接拷贝如下代码到自己工程中修改
     *
     */
    private static BDLocationListener mListener = new BDLocationListener() {

        @Override
        public void onReceiveLocation(BDLocation location) {
            Log.i(TAG,"onReceiveLocation:");
            if (null != location && location.getLocType() != BDLocation.TypeServerError) {
                double latitude =  location.getLatitude();
                double longitude = location.getLongitude();
                Log.i(TAG,"latitude:"+location.getLatitude());
                Log.i(TAG,"longitude:"+location.getLongitude());
                showPointOnMap(latitude,longitude,null);
            }
        }
    };




}
