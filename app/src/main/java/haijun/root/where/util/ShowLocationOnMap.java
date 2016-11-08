package haijun.root.where.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import haijun.root.where.R;
import haijun.root.where.activity.MapTraceActivity;
import haijun.root.where.service.LocationService;


/**
 * Created by root on 10/25/16.
 */

public class ShowLocationOnMap {
    static MapView mMapView;
    private static final String TAG = "ShowLocationOnMap";
    private static LocationService locationService;
    private static Overlay overlay = null;

    private static Context context;

    /**
     * 图标
     */


    public static void showPointOnMap(double latitude,double longitude,MapView mMapView,Context context,int who) {
        if (mMapView!=null){
            ShowLocationOnMap.mMapView = mMapView;
        }
        BaiduMap map = ShowLocationOnMap.mMapView.getMap();
        LatLng latLng = new LatLng(latitude, longitude);

    /*    //在地图上显示所在位置
        map.setMyLocationEnabled(true);  //设置为位置显示
        MyLocationData.Builder localbuild = new MyLocationData.Builder();
        localbuild.latitude(latitude);
        localbuild.longitude(longitude);
        MyLocationData myLocationData = localbuild.build();
        map.setMyLocationData(myLocationData);

        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLngZoom(latLng, 17); //17为地图的显示比例，比例范围是3-19
        map.animateMapStatus(msu);
*/
        if (null != overlay) {
            overlay.remove();
        }

        MapStatus mMapStatus = new MapStatus.Builder().target(latLng).zoom(17).build();
        MapStatusUpdate msUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);

        Bitmap bitmap;
        if (who==1){
            //自己
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.location_me);
        }
        else {
            //好友
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.location_on);
        }
        Bitmap bitmap1 = zoomImg(bitmap, (int)MyUtil.dp2px(context,30), (int)MyUtil.dp2px(context,30));
        BitmapDescriptor realtimeBitmap = BitmapDescriptorFactory.fromBitmap(bitmap1);
        OverlayOptions overlayOptions = new MarkerOptions().position(latLng).icon(realtimeBitmap).zIndex(9).draggable(true);


        List<Map<Integer,OverlayOptions>> overlaymapList = new ArrayList<>();
        Map<Integer,OverlayOptions> myOver= new HashMap<>();
        myOver.put(0,overlayOptions);
        overlaymapList.add(myOver);

        if (null != msUpdate) {
            map.setMapStatus(msUpdate);
        }

        // 实时点覆盖物
        if (null != overlayOptions) {
            for (int i=0;i<overlaymapList.size();i++){
                map.addOverlay(overlaymapList.get(i).get(i));
            }

        }
    }

    public static Bitmap zoomImg(Bitmap bm, int newWidth ,int newHeight){
        // 获得图片的宽高
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return newbm;
    }


    public static void startLocation(Context context,MapView mMapView) {
        ShowLocationOnMap.context = context;
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
                showPointOnMap(latitude,longitude,null,ShowLocationOnMap.context,1);
            }
        }
    };




}
