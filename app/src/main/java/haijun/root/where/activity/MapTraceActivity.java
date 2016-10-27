package haijun.root.where.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.text.InputType;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.LocationMode;
import com.baidu.trace.OnEntityListener;
import com.baidu.trace.OnGeoFenceListener;
import com.baidu.trace.OnStartTraceListener;
import com.baidu.trace.OnStopTraceListener;
import com.baidu.trace.OnTrackListener;
import com.baidu.trace.Trace;
import com.baidu.trace.TraceLocation;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import haijun.root.where.R;
import haijun.root.where.application.MyApplication;
import haijun.root.where.bean.HistoryLocation;
import haijun.root.where.bean.LocationInformation;
import haijun.root.where.service.MonitorService;
import haijun.root.where.util.Contanst;
import haijun.root.where.util.DateDialog;
import haijun.root.where.util.DateUtils;
import haijun.root.where.util.MyUtil;
import haijun.root.where.util.ShowLocationOnMap;
import haijun.root.where.util.TraceUtil;

public class MapTraceActivity extends Activity {
    private static final String TAG = "MapTraceActivity";
    private Trace trace;
    //鹰眼服务ID
    long serviceId  = 127542  ;
    //entity标识
    String entityName = "c1";
    private MapView trace_bmapView;
    private MapStatusUpdate msUpdate = null;
    // 起点图标
    private static BitmapDescriptor bmStart;
    // 终点图标
    private static BitmapDescriptor bmEnd;
    // 起点图标覆盖物
    private static MarkerOptions startMarker = null;
    // 终点图标覆盖物
    private static MarkerOptions endMarker = null;
    // 路线覆盖物
    private static PolylineOptions polyline = null;
    private BaiduMap mBaiduMap;
    // 覆盖物
    protected static OverlayOptions overlay;
    //默认围栏半径
    private int radius =100;
    private int radiusTemp = radius;

    private int fenceId= 0;
    //图标
    private static BitmapDescriptor realtimeBitmap;
    private List<LatLng> latLngList;

    // 围栏去噪精度
    private static int precision = 30;

    // 围栏覆盖物
    protected static OverlayOptions fenceOverlay = null;
    protected static OverlayOptions fenceOverlayTemp = null;
    //private LatLng latLng;
    private int curFence_id;
    private boolean isFenceshowing;
    private Overlay circleOverlay;
    private LinearLayout ll_map_more;
    private MyTraceOnclickListener myTraceOnclickListener;
    private TextView bt_map_tracestate;
    private TextView bt_map_traceshistory;
    private RelativeLayout rl_map_righttrace;
    private RelativeLayout rl_map_righthistory;
    private TextView tv_map_currentdate;

    private int year = 0;
    private int month = 0;
    private int day = 0;

    private int startTime = 0;
    private int endTime = 0;

    private int traceState = Contanst.TRECE_STATE_NOPROCESSED;
    private LinearLayout ll_map_mylocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_trace);

        trace_bmapView = (MapView) findViewById(R.id.trace_bmapView);
        mBaiduMap = trace_bmapView.getMap();

        trace_bmapView.requestDisallowInterceptTouchEvent(true);

        initView();



        startTrace();



    }

    private void initView() {

        LinearLayout ll_map_frlocation = (LinearLayout) findViewById(R.id.ll_map_frlocation);
        LinearLayout ll_map_fence = (LinearLayout) findViewById(R.id.ll_map_fence);
        LinearLayout ll_map_state = (LinearLayout) findViewById(R.id.ll_map_state);
        ll_map_mylocation = (LinearLayout) findViewById(R.id.ll_map_mylocation);
        ll_map_more = (LinearLayout) findViewById(R.id.ll_map_more);

        bt_map_tracestate = (TextView) findViewById(R.id.bt_map_tracestate);
        bt_map_traceshistory = (TextView) findViewById(R.id.bt_map_traceshistory);
        rl_map_righttrace = (RelativeLayout) findViewById(R.id.rl_map_righttrace);
        rl_map_righthistory = (RelativeLayout) findViewById(R.id.rl_map_righthistory);
        tv_map_currentdate = (TextView) findViewById(R.id.tv_map_currentdate);

        LinearLayout iv_map_refresh = (LinearLayout) findViewById(R.id.iv_map_refresh);
        LinearLayout ll_map_date = (LinearLayout) findViewById(R.id.ll_map_date);
        LinearLayout ll_map_processe = (LinearLayout) findViewById(R.id.ll_map_processe);


        tv_map_currentdate.setText("当前日期："+DateUtils.getCurrentDate());


        myTraceOnclickListener = new MyTraceOnclickListener();

        ll_map_frlocation.setOnClickListener(myTraceOnclickListener);
        ll_map_fence.setOnClickListener(myTraceOnclickListener);
        ll_map_state.setOnClickListener(myTraceOnclickListener);
        ll_map_mylocation.setOnClickListener(myTraceOnclickListener);
        ll_map_more.setOnClickListener(myTraceOnclickListener);

        bt_map_tracestate.setOnClickListener(myTraceOnclickListener);
        bt_map_traceshistory.setOnClickListener(myTraceOnclickListener);

        iv_map_refresh.setOnClickListener(myTraceOnclickListener);
        ll_map_date.setOnClickListener(myTraceOnclickListener);
        ll_map_processe.setOnClickListener(myTraceOnclickListener);

    }


    @Override
    public void onResume() {
        super.onResume();
        trace_bmapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        queryEntityLocation();
        Log.i(TAG,"onStart");
    }

    //开始追踪服务
    private  void startTrace(){
        /*//实例化轨迹服务客户端
        client = new LBSTraceClient(this);
        client.setLocationMode(LocationMode.High_Accuracy);
        //位置采集周期
        int gatherInterval = 10;
        //打包周期
        int packInterval = 60;
        //设置位置采集和打包周期
        client.setInterval(gatherInterval, packInterval);

        // 设置协议类型，0为http，1为https
        int protocoType = 1;
        client.setProtocolType(protocoType);

        //轨迹服务类型（0 : 不上传位置数据，也不接收报警信息； 1 : 不上传位置数据，但接收报警信息；2 : 上传位置数据，且接收报警信息）
        int  traceType = 2;
        //实例化轨迹服务
        trace = new Trace(this, serviceId, entityName, traceType);
*/

        //注册屏幕亮度变化的广播
        TraceUtil.registerReveicer(this);


        //实例化开启轨迹服务回调接口
        OnStartTraceListener startTraceListener = new OnStartTraceListener() {
            //开启轨迹服务回调接口（arg0 : 消息编码，arg1 : 消息内容，详情查看类参考）
            @Override
            public void onTraceCallback(int arg0, String arg1) {
                if (arg0==0){
                    Toast.makeText(MapTraceActivity.this, "开启成功", Toast.LENGTH_SHORT).show();
                }
                else if (arg0==10000){
                    Toast.makeText(MapTraceActivity.this, "10000开启服务请求发送失败", Toast.LENGTH_SHORT).show();
                }
                else if (arg0==10001){
                    Toast.makeText(MapTraceActivity.this, "10001参数错误", Toast.LENGTH_SHORT).show();
                }
                else if (arg0==10003){
                    Toast.makeText(MapTraceActivity.this, "10003网络连接失败", Toast.LENGTH_SHORT).show();
                }
                else if (arg0==10004){
                    Toast.makeText(MapTraceActivity.this, "10004网络未开启", Toast.LENGTH_SHORT).show();
                }
                else if (arg0==10005){
                    Toast.makeText(MapTraceActivity.this, "10005服务正在开启", Toast.LENGTH_SHORT).show();
                }
                else if (arg0==10006){
                    Toast.makeText(MapTraceActivity.this, "10006服务已开启", Toast.LENGTH_SHORT).show();
                }
                else if (arg0==10008){
                    Toast.makeText(MapTraceActivity.this, "10006开启缓存", Toast.LENGTH_SHORT).show();
                }
                else if (arg0==10009){
                    Toast.makeText(MapTraceActivity.this, "10009已开启缓存", Toast.LENGTH_SHORT).show();
                }

                Log.i(TAG,"onTraceCallback:"+arg0+":"+arg1);
            }
            //轨迹服务推送接口（用于接收服务端推送消息，arg0 : 消息类型，arg1 : 消息内容，详情查看类参考）
            //报警信息在OnStartTraceListener监听器的OnTracePushCallBack()接口中获取
            @Override
            public void onTracePushCallback(byte arg0, String arg1) {
                Log.i(TAG,"onTracePushCallback:"+arg0+":"+arg1);
            }
        };

        //开启轨迹服务
        //client.startTrace(trace, startTraceListener);

        MyApplication.client.startTrace(MyApplication.trace,startTraceListener);

        if (!MonitorService.isRunning) {
            // 开启监听service
            MonitorService.isCheck = true;
            MonitorService.isRunning = true;
            startMonitorService();
        }
    }

    public void startMonitorService() {
        Intent intent = new Intent(this, MonitorService.class);
        startService(intent);
    }

    //查询实时位置
    private void queryEntityLocation() {
        //检索条件（格式为 : "key1=value1,key2=value2,....."）
        //String columnKey = "car_team=1";
        String columnKey = "car_team=1";
        //返回结果的类型（0 : 返回全部结果，1 : 只返回entityName的列表）
        int returnType = 0;
        //活跃时间，UNIX时间戳（指定该字段时，返回从该时间点之后仍有位置变动的entity的实时点集合）
        int activeTime = (int) (System.currentTimeMillis() / 1000 - 12 * 60 * 60);
        //分页大小
        int pageSize = 1000;
        //分页索引
        int pageIndex = 1;
        //Entity监听器

        OnEntityListener entityListener = new OnEntityListener() {
            // 查询失败回调接口
            @Override
            public void onRequestFailedCallback(String arg0) {
                Log.i(TAG,"entity请求失败回调接口消息 :"+  arg0);

            }

            // 查询entity回调接口，返回查询结果列表
            @Override
            public void onQueryEntityListCallback(String arg0) {
                //json解析
                Gson gson = new Gson();
                LocationInformation locationInformation = gson.fromJson(arg0, LocationInformation.class);

                Log.i(TAG,"onQueryEntityListCallback: " + arg0);
                Log.i(TAG,"onQueryEntityListCallback,entities.length : " + locationInformation.entities.length);
                //经纬度
                String[] location = locationInformation.entities[0].realtime_point.location;
                //Log.i(TAG,"entity回调接口消息 : " + location[0]+","+location[1]);
                double latitude =  Double.parseDouble(location[1]);
                double longitude =  Double.parseDouble(location[0]);

                //在地图上显示
                ShowLocationOnMap.showPointOnMap(latitude,longitude,trace_bmapView);
            }
            //Entity实时定位回调接口
            @Override
            public void onReceiveLocation(TraceLocation traceLocation) {
                super.onReceiveLocation(traceLocation);
                Log.i(TAG,"entity回调接口消息 ,onReceiveLocation: " + traceLocation.getLatitude()+":"+traceLocation.getLongitude());
            }

            @Override
            public void onUpdateEntityCallback(String s) {
                super.onUpdateEntityCallback(s);
                Log.i(TAG,"entity回调接口消息 ,onUpdateEntityCallback: " +s);
            }
        };

        MyApplication.client.queryEntityList(serviceId, entityName, columnKey, returnType, activeTime, pageSize,
                pageIndex, entityListener);
    }

    //查询历史轨迹
    private void queryhisteryLocation(){
        //是否返回精简的结果（0 : 将只返回经纬度，1 : 将返回经纬度及其他属性信息）
        int simpleReturn = 0;
        //开始时间（Unix时间戳）
        int startTime = (int) (System.currentTimeMillis() / 1000 - 12 * 60 * 60);
        //结束时间（Unix时间戳）
        int endTime = (int) (System.currentTimeMillis() / 1000);
        //分页大小
        int pageSize = 1000;
        //分页索引
        int pageIndex = 1;
        //查询历史轨迹
        MyApplication.client.queryHistoryTrack(serviceId, entityName, simpleReturn, startTime, endTime, pageSize, pageIndex, onTrackListener);
    }

    // 绘制历史轨迹
    private OnTrackListener onTrackListener = new OnTrackListener() {
        @Override
        public void onRequestFailedCallback(String s) {
            MyUtil.hideDialog();
        }

        //轨迹中的每个位置点可拥有一系列开发者自定义的描述字段，如汽车的油量、发动机转速等，用以记录行程中的实时状态信息。
        @Override
        public Map onTrackAttrCallback() {
            //开发者须重写OnTrackListener监听器中的onTrackAttrCallback()接口，在回传轨迹点时回传属性数据。
            // 注：SDK根据位置采集周期回调该接口，获取轨迹属性数据。

            return super.onTrackAttrCallback();
        }

        @Override
        public void onQueryHistoryTrackCallback(String s) {
            super.onQueryHistoryTrackCallback(s);
            MyUtil.hideDialog();
            //返回数据格式为point点的数据集合
        /*{
            "status": 0,
                "size": 738,
                "total": 738,
                "entity_name": "mycar",
                "distance": 5674.7545455465,
                "points": [
            {
                "loc_time": 1467620518,
                    "location": [
                113.92966866732,
                        22.579549752609
                ],
                "create_time": "2016-07-04 16:21:59",
                            "radius": 40,
                            "speed": 0,
                            "direction": 0
                    },
                    {
                        "loc_time": 1467620508,
                            "location": [
                        113.92966866732,
                                22.579549752609
                        ],
                */

            dealDataToDrawTrack(s);
            Log.i(TAG, "OnTrackListener回调接口消息 : " + s);
        }
    };

    private void dealDataToDrawTrack(String data) {
        Gson gson = new Gson();
        HistoryLocation historyLocation = gson.fromJson(data, HistoryLocation.class);

        latLngList = new ArrayList<>();
        if (historyLocation != null && Double.parseDouble(historyLocation.status) == 0) {
            if (historyLocation.points!= null) {
                Iterator<HistoryLocation.LocationPoint> iterator = historyLocation.points.iterator();

                while (iterator.hasNext()){
                    HistoryLocation.LocationPoint locationPoint = iterator.next();
                    String[] location = locationPoint.location;
                    LatLng latLng = new LatLng(Double.parseDouble(location[1]),Double.parseDouble(location[0]));
                    latLngList.add(latLng);
                }
            }
            double distance = Double.parseDouble(historyLocation.distance);
            // 绘制历史轨迹
            drawHistoryTrack(latLngList, distance);
        }
    }

    /**
     * 查询纠偏后的历史轨迹
     */
    private void queryProcessedHistoryTrack() {
        // 是否返回精简的结果（0 : 否，1 : 是）
        int simpleReturn = 0;
        // 是否返回纠偏后轨迹（0 : 否，1 : 是）
        int isProcessed = 1;
        //开始时间（Unix时间戳）
        int startTime = (int) (System.currentTimeMillis() / 1000 - 12 * 60 * 60);
        //结束时间（Unix时间戳）
        int endTime = (int) (System.currentTimeMillis() / 1000);
        // 分页大小
        int pageSize = 1000;
        // 分页索引
        int pageIndex = 1;

        MyApplication.client.queryProcessedHistoryTrack(serviceId, entityName, simpleReturn, isProcessed,
                startTime, endTime,
                pageSize,
                pageIndex,
                onTrackListener);
    }

    /**
     * 绘制历史轨迹
     *
     * @param points
     */
    private void drawHistoryTrack(final List<LatLng> points, final double distance) {
        // 绘制新覆盖物前，清空之前的覆盖物
        trace_bmapView.getMap().clear();

        if (points == null || points.size() == 0) {
            Looper.prepare();
            Toast.makeText(MapTraceActivity.this, "当前查询无轨迹点", Toast.LENGTH_SHORT).show();
            Looper.loop();
            resetMarker();
        } else if (points.size() > 1) {

            LatLng llC = points.get(0);
            LatLng llD = points.get(points.size() - 1);
            LatLngBounds bounds = new LatLngBounds.Builder()
                    .include(llC).include(llD).build();

            msUpdate = MapStatusUpdateFactory.newLatLngBounds(bounds);


            bmStart = BitmapDescriptorFactory.fromResource(R.drawable.icon_start);
            bmEnd = BitmapDescriptorFactory.fromResource(R.drawable.icon_end);

            // 添加起点图标
            startMarker = new MarkerOptions()
                    .position(points.get(points.size() - 1)).icon(bmStart)
                    .zIndex(9).draggable(true);
            // 添加终点图标
            endMarker = new MarkerOptions().position(points.get(0))
                    .icon(bmEnd).zIndex(9).draggable(true);

            // 添加路线（轨迹）
            polyline = new PolylineOptions().width(10)
                    .color(Color.RED).points(points);

            addMarker();

            Looper.prepare();
            Toast.makeText(MapTraceActivity.this, "当前轨迹里程为 : " + (int) distance + "米", Toast.LENGTH_SHORT).show();
            Looper.loop();
        }
    }

    private void setGeoFence() {
        showinputDialog();
    }

    // 输入围栏信息对话框
    private void showinputDialog() {
        final EditText circleRadius = new EditText(MapTraceActivity.this);
        circleRadius.setFocusable(true);
        circleRadius.setInputType(InputType.TYPE_CLASS_NUMBER);
        AlertDialog.Builder builder = new AlertDialog.Builder(MapTraceActivity.this);

        builder.setTitle("围栏半径(单位:米)").setView(circleRadius)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        mBaiduMap.setOnMapClickListener(null);
                    }

                });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                mBaiduMap.clear();
                String radiusStr = circleRadius.getText().toString();
                if (!TextUtils.isEmpty(radiusStr)) {
                    radius = Integer.parseInt(radiusStr) > 0 ? Integer.parseInt(radiusStr) : radius;
                }
                Toast.makeText(MapTraceActivity.this, "请点击地图标记围栏圆心", Toast.LENGTH_LONG).show();
            }
        });
        builder.show();
    }

    private void stopTrace() {
        //实例化停止轨迹服务回调接口
        OnStopTraceListener stopTraceListener = new OnStopTraceListener(){
            //轨迹服务停止成功
            @Override
            public void onStopTraceSuccess() {
                Toast.makeText(MapTraceActivity.this, "停止成功", Toast.LENGTH_SHORT).show();
            }
            // 轨迹服务停止失败（arg0 : 错误编码，arg1 : 消息内容，详情查看类参考）
            @Override
            public void onStopTraceFailed(int arg0, String arg1) {
                if (arg0==11000){
                    Toast.makeText(MapTraceActivity.this, "11000停止服务请求发送失败", Toast.LENGTH_SHORT).show();
                }
                else if (arg0==11001){
                    Toast.makeText(MapTraceActivity.this, "11001停止服务失败", Toast.LENGTH_SHORT).show();
                }
                else if (arg0==11002){
                    Toast.makeText(MapTraceActivity.this, "11002服务未开启", Toast.LENGTH_SHORT).show();
                }
                else if (arg0==11003){
                    Toast.makeText(MapTraceActivity.this, "11003服务正在停止", Toast.LENGTH_SHORT).show();
                }
            }
        };
        //停止轨迹服务
        MyApplication.client.stopTrace(trace,stopTraceListener);
    }
    /**
     * 重置覆盖物
     */
    private void resetMarker() {
        startMarker = null;
        endMarker = null;
        polyline = null;
    }

    /**
     * 添加覆盖物
     */
    protected void addMarker() {

        if (null != msUpdate) {
            mBaiduMap.setMapStatus(msUpdate);
            //设置地图当前缩放比例为17
            MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.zoomTo(17);
            mBaiduMap.setMapStatus(mapStatusUpdate);
        }

        if (null != startMarker) {
            mBaiduMap.addOverlay(startMarker);
        }

        if (null != endMarker) {
            mBaiduMap.addOverlay(endMarker);
        }

        if (null != polyline) {
            mBaiduMap.addOverlay(polyline);
        }
    }

    //围栏监听器
    private OnGeoFenceListener onGeoFenceListener = new OnGeoFenceListener() {
        //请求失败回调接口
        @Override
        public void onRequestFailedCallback(String arg0) {
            mBaiduMap.clear();
            if (null != fenceOverlayTemp) {
                fenceOverlay = fenceOverlayTemp;
                fenceOverlayTemp = null;
            }
            radius = radiusTemp;
            // 围栏覆盖物
            addMapOverLay();
            Log.i(TAG,"geoFence请求失败 : " + arg0);
            Looper.prepare();
            Toast.makeText(MapTraceActivity.this,"geoFence请求失败:"+ arg0,Toast.LENGTH_SHORT).show();
            Looper.loop();
        }

        //创建圆形围栏回调接口
        @Override
        public void onCreateCircularFenceCallback(String arg0) {
            Log.i(TAG,"创建圆形围栏回调接口消息 : " + arg0);
            try {
                JSONObject jsonObject = new JSONObject(arg0);
                int status = jsonObject.getInt("status");
                if (status==0){
                    Log.i(TAG,"status==0");
                    //创建成功，
                    fenceId = jsonObject.getInt("fence_id");
                    MyApplication.locationinf.edit().putInt("fence_id",fenceId).commit();
                    fenceOverlayTemp = null;
                    Looper.prepare();
                    Toast.makeText(MapTraceActivity.this,"创建围栏成功",Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
                else {
                    mBaiduMap.clear();
                    fenceOverlay = fenceOverlayTemp;
                    fenceOverlayTemp = null;
                    radius = radiusTemp;
                    // 围栏覆盖物
                    addMapOverLay();
                    Looper.prepare();
                    Toast.makeText(MapTraceActivity.this,"创建圆形围栏失败："+arg0,Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //更新圆形围栏回调接口
        @Override
        public void onUpdateCircularFenceCallback(String arg0) {
            Log.i(TAG,"更新圆形围栏回调接口消息 : " + arg0);

            try {
                JSONObject jsonObject = new JSONObject(arg0);
                int status = jsonObject.getInt("status");
                if (status==0){
                    Log.i(TAG,"status==0");
                    //创建成功，
                    Looper.prepare();
                    Toast.makeText(MapTraceActivity.this,"更新围栏成功",Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
                else {
                    Looper.prepare();
                    Toast.makeText(MapTraceActivity.this,"更新圆形围栏失败："+arg0,Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //删除围栏回调接口
        @Override
        public void onDeleteFenceCallback(String arg0) {
            Log.i(TAG," 删除围栏回调接口消息 : " + arg0);
        }

        //查询围栏列表回调接口
        @Override
        public void onQueryFenceListCallback(String arg0) {
            Log.i(TAG,"查询围栏列表回调接口消息    : " + arg0);

            try {
                JSONObject jsonObject = new JSONObject(arg0);
                int status = jsonObject.getInt("status");
                JSONArray fences = jsonObject.getJSONArray("fences");
                JSONObject jsonObject1 = (JSONObject) fences.get(0);
                int fence_id = jsonObject1.getInt("fence_id");
                Log.i(TAG,"fence_id:"+fence_id);
               //查询成功
                if (status==0){
                    if (fence_id==curFence_id){
                        int radius = jsonObject1.getInt("radius");
                        JSONObject center = jsonObject1.getJSONObject("center");
                        double longitude = center.getDouble("longitude");
                        double latitude = center.getDouble("latitude");

                        LatLng latLng = new LatLng(latitude,longitude);
                        fenceOverlayTemp = fenceOverlay;
                        fenceOverlay = new CircleOptions().fillColor(0x000000FF).center(latLng)
                                .stroke(new Stroke(5, Color.rgb(0xff, 0x00, 0x33)))
                                .radius(radius);

                        // 围栏覆盖物
                        addMapOverLay();
                        isFenceshowing = true;
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //查询历史报警回调接口
        @Override
        public void onQueryHistoryAlarmCallback(String arg0) {
            Log.i(TAG," 查询历史报警回调接口消息 : " + arg0);
            /*返回数据格式  ，具体看http://lbsyun.baidu.com/index.php?title=yingyan/api/fence
            {
                "status": 0,
                "message": "成功",
                "size": 0,
                "monitored_person_alarms": []
            }
             */
            try {
                JSONObject jsonObject = new JSONObject(arg0);
                int status = jsonObject.getInt("status");
                int size = jsonObject.getInt("size");  //返回的结果条数
                if (status==0){
                    for (int i=0;i<size;i++){
                        JSONArray monitored_person_alarms = jsonObject.getJSONArray("monitored_person_alarms");
                        JSONObject jsonObject1 = (JSONObject) monitored_person_alarms.get(i);
                        String monitored_person = jsonObject1.getString("monitored_person");
                        if (monitored_person.equals(entityName)){
                            int alarm_size = jsonObject1.getInt("alarm_size");  //报警列表大小
                            JSONArray alarms = jsonObject1.getJSONArray("alarms");
                            for (int j=0;j<alarm_size;j++){
                                JSONObject jsonObject2 = (JSONObject) alarms.get(0);
                                int action = jsonObject2.getInt("action");
                                String time = jsonObject2.getString("time");
                                Log.i(TAG,"entityName:"+entityName+","+"action"+action+"time"+time);
                            }
                        }
                    }
                    if (size==0){
                        Looper.prepare();
                        Toast.makeText(MapTraceActivity.this,"历史报警记录为空",Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        //查询监控对象状态回调接口
        @Override
        public void onQueryMonitoredStatusCallback(String arg0) {
            Log.i(TAG," 查询监控对象状态回调接口消息    : " + arg0);
           /* 返回数据格式
           {
                "status": 0,
                "message": "成功",
                "size": 1,
                "monitored_person_statuses": [
                    {
                        "monitored_person": "c1",
                        "monitored_status": 1    ===>0：未知状态 1：在围栏内 2：在围栏外
                    }
                ]
            }
            */

            try {
                JSONObject jsonObject = new JSONObject(arg0);
                int status = jsonObject.getInt("status");
                if (status==0){
                    //返回成功
                    int size = jsonObject.getInt("size");
                    for (int i=0;i<size;i++){
                        JSONArray monitored_person_statuses = jsonObject.getJSONArray("monitored_person_statuses");
                        JSONObject jsonObject1 = (JSONObject) monitored_person_statuses.get(i);
                        String monitored_person = jsonObject1.getString("monitored_person");
                        if (monitored_person.equals(entityName)){
                            int monitored_status = jsonObject1.getInt("monitored_status");
                            AlertDialog.Builder builder = new AlertDialog.Builder(MapTraceActivity.this);
                            builder.setTitle("是否在围栏里");
                            builder.setPositiveButton("确定",null);
                            if (monitored_status==0){
                                //0：未知状态
                                Looper.prepare();
                                builder.setMessage("未知状态");
                                builder.show();
                                Looper.loop();
                            }
                            else if (monitored_status==1){
                                //1：在围栏内
                                Looper.prepare();
                                builder.setMessage("在围栏内");
                                builder.show();
                                Looper.loop();
                            }
                            else if (monitored_status==2){
                                //2：在围栏外
                                Looper.prepare();
                                builder.setMessage("在围栏外");
                                builder.show();
                                Looper.loop();
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    };

    class MyTraceOnclickListener implements View.OnClickListener{

        private boolean tag  = true;
        private float v1;
        private float v2;

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                //点击事件，显示当前位置
                case R.id.ll_map_mylocation:
                    mBaiduMap.clear();
                    ShowLocationOnMap.startLocation(MapTraceActivity.this,trace_bmapView);
                    break;
                //点击事件，显示追踪线路
                case R.id.ll_map_frlocation:
                    mBaiduMap.clear();
                    queryEntityLocation();
                    break;
                //更多
                case R.id.ll_map_more:
                    showPopupWindow();
                    break;
                //停止追踪
                /*case R.id.bt_trace_stoptrace:
                    stopTrace();
                    break;
                //纠偏轨迹
                case R.id.bt_trace_processedtrace:
                    mBaiduMap.clear();
                    queryProcessedHistoryTrack();
                    break;*/
                //显示围栏
                case R.id.ll_map_fence:
                    showGeoFence();
                    break;
                //新建围栏
                case R.id.ll_map_newfence:
                    mBaiduMap.setOnMapClickListener(mapClickListener);
                    setGeoFence();
                    break;
                //查询状态
                case R.id.ll_map_state:
                    checkcurrentState();
                    break;
                //轨迹追踪按钮
                case R.id.bt_map_tracestate:
                    bt_map_tracestate.setBackgroundResource(R.drawable.bg_care_switch_lefton);
                    bt_map_traceshistory.setBackgroundResource(R.drawable.bg_care_switch_rightoff);
                    bt_map_tracestate.setTextColor(Color.parseColor("#FFFFFF"));
                    bt_map_traceshistory.setTextColor(Color.parseColor("#999999"));
                    rl_map_righttrace.setVisibility(View.VISIBLE);
                    rl_map_righthistory.setVisibility(View.GONE);
                    ll_map_mylocation.setVisibility(View.VISIBLE);
                    trace_bmapView.getMap().clear();

                    break;
                //历史轨迹按钮
                case R.id.bt_map_traceshistory:
                    bt_map_tracestate.setBackgroundResource(R.drawable.bg_care_switch_leftoff);
                    bt_map_traceshistory.setBackgroundResource(R.drawable.bg_care_switch_righton);
                    bt_map_tracestate.setTextColor(Color.parseColor("#999999"));
                    bt_map_traceshistory.setTextColor(Color.parseColor("#FFFFFF"));
                    rl_map_righttrace.setVisibility(View.GONE);
                    rl_map_righthistory.setVisibility(View.VISIBLE);
                    ll_map_mylocation.setVisibility(View.INVISIBLE);

                    MyUtil.showDialog("正在查询轨迹", MapTraceActivity.this);
                    queryHistoryTrack(1, "need_denoise=1,need_vacuate=1,need_mapmatch=0");


                    break;

                //选择日期
                case R.id.ll_map_date:
                    chooseDate();

                    break;

                //轨迹纠偏
                case R.id.ll_map_processe:
                    String toastInf = "";
                    if (traceState == Contanst.TRECE_STATE_PROCESSED) {
                        //未纠偏
                        toastInf = "正在查询轨迹";
                        queryHistoryTrack(1, "need_denoise=1,need_vacuate=1,need_mapmatch=0");
                    } else if (traceState == Contanst.TRECE_STATE_NOPROCESSED) {
                        toastInf = "正在查询纠偏轨迹";
                        queryHistoryTrack(1, "need_denoise=1,need_vacuate=1,need_mapmatch=1");

                    }
                    traceState = traceState == Contanst.TRECE_STATE_NOPROCESSED ? Contanst.TRECE_STATE_PROCESSED : Contanst.TRECE_STATE_NOPROCESSED;
                    MyUtil.showDialog(toastInf, MapTraceActivity.this);
                    break;

                //刷新
                case R.id.iv_map_refresh:
                    MyUtil.showDialog("正在查询轨迹", MapTraceActivity.this);
                    queryHistoryTrack(1, "need_denoise=1,need_vacuate=1,need_mapmatch=0");
                    break;

                default:
                    break;
            }
        }
    }

    private void chooseDate() {
        // 选择日期
        int[] date = null;
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        if (year == 0 && month == 0 && day == 0) {
            String curDate = DateUtils.getCurrentDate();
            date = DateUtils.getYMDArray(curDate, "-");
        }

        if (date != null) {
            year = date[0];
            month = date[1];
            day = date[2];
        }

        DateDialog dateDiolog = new DateDialog(this, new DateDialog.PriorityListener() {

            public void refreshPriorityUI(String sltYear, String sltMonth,
                                          String sltDay, DateDialog.CallBack back) {

                Log.d("TGA", sltYear + sltMonth + sltDay);
                year = Integer.parseInt(sltYear);
                month = Integer.parseInt(sltMonth);
                day = Integer.parseInt(sltDay);
                String st = year + "年" + month + "月" + day + "日0时0分0秒";
                String et = year + "年" + month + "月" + day + "日23时59分59秒";

                startTime = Integer.parseInt(DateUtils.getTimeToStamp(st));
                endTime = Integer.parseInt(DateUtils.getTimeToStamp(et));

                back.execute();
            }

        }, new DateDialog.CallBack() {

            public void execute() {
                traceState = Contanst.TRECE_STATE_NOPROCESSED;
                tv_map_currentdate.setText(" 当前日期 : " + year + "-" + month + "-" + day + " ");
                Toast.makeText(MapTraceActivity.this, "正在查询历史轨迹，请稍候", Toast.LENGTH_SHORT).show();
                queryHistoryTrack(1,"need_denoise=1,need_vacuate=1,need_mapmatch=0");
            }
        }, year, month, day, width, height, "选择日期", 1);

        Window window = dateDiolog.getWindow();
        window.setGravity(Gravity.CENTER); // 此处可以设置dialog显示的位置
        dateDiolog.setCancelable(true);
        dateDiolog.show();
    }

    private void queryHistoryTrack(int processed, String processOption) {
        //是否返回精简的结果（0 : 将只返回经纬度，1 : 将返回经纬度及其他属性信息）
        int simpleReturn = 0;
        // 是否返回纠偏后轨迹（0 : 否，1 : 是）
        int isProcessed = processed;
        //分页大小
        int pageSize = 1000;
        //分页索引
        int pageIndex = 1;
        // 开始时间
        if (startTime == 0) {
            startTime = (int) (System.currentTimeMillis() / 1000 - 12 * 60 * 60);
        }
        if (endTime == 0) {
            endTime = (int) (System.currentTimeMillis() / 1000);
        }

        //查询历史轨迹
        MyApplication.client.queryHistoryTrack(serviceId, entityName, simpleReturn, isProcessed, processOption,startTime, endTime, pageSize, pageIndex, onTrackListener);
    }


    private void showPopupWindow() {
        // 一个自定义的布局，作为显示的内容
        View contentView = LayoutInflater.from(MapTraceActivity.this).inflate(
                R.layout.pop_window_more, null);
        // 设置按钮的点击事件

        LinearLayout ll_map_newfence = (LinearLayout) contentView.findViewById(R.id.ll_map_newfence);
        LinearLayout ll_map_histalarm = (LinearLayout) contentView.findViewById(R.id.ll_map_histalarm);

        ll_map_newfence.setOnClickListener(myTraceOnclickListener);
        ll_map_histalarm.setOnClickListener(myTraceOnclickListener);
        final PopupWindow popupWindow = new PopupWindow(contentView,
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);

/*popupWindow.setTouchInterceptor(new View.OnTouchListener() {

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        Log.i("mengdd", "onTouch : ");

        return true;
        // 这里如果返回true的话，touch事件将被拦截
        // 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
    }
});
*/
        /*popupWindow.setBackgroundDrawable(getResources().getDrawable(
                R.drawable.bg_pop));*/
        popupWindow.setBackgroundDrawable(new ColorDrawable(0));

        // 设置好参数之后再show
        popupWindow.showAsDropDown(ll_map_more);

    }

    private void checkhisteryAlarmInfo() {
        //围栏ID
        int fenceId = MyApplication.locationinf.getInt("fence_id", 0);
        //监控对象列表（多个entityName，以英文逗号"," 分割）
        String monitoredPersons = entityName;
        //开始时间（unix时间戳）
        int beginTime = (int) (System.currentTimeMillis() / 1000 - 12 * 60 * 60);
        //结束时间（unix时间戳）
        int endTime = (int) (System.currentTimeMillis() / 1000);

        //查询历史报警信息
        MyApplication.client.queryFenceHistoryAlarmInfo(serviceId, fenceId, monitoredPersons, beginTime, endTime,
                onGeoFenceListener);
    }

    private void checkcurrentState() {
        //围栏ID
        int fenceId = MyApplication.locationinf.getInt("fence_id", 0);
        //监控对象列表（多个entityName，以英文逗号"," 分割）
        String monitoredPersons = entityName;
        //查询实时状态
        MyApplication.client.queryMonitoredStatus(serviceId, fenceId, monitoredPersons, onGeoFenceListener);
    }


    //显示围栏
    private void showGeoFence() {
        if (!isFenceshowing){
            //查询围栏列表 queryFenceList(long serviceId, java.lang.String creator, java.lang.String fenceIds, OnGeoFenceListener listener)
            String creator = entityName;
            curFence_id = MyApplication.locationinf.getInt("fence_id", 0);
            if (curFence_id !=0){
                MyApplication.client.queryFenceList(serviceId,creator, String.valueOf(curFence_id),onGeoFenceListener);
            }
        }
        else {
            //移除围栏
            removeMapOverLay();
        }
    }


    // 添加围栏覆盖物
    private void addMapOverLay(){
        if (null != fenceOverlay) {
            circleOverlay = mBaiduMap.addOverlay(fenceOverlay);
        }
    }

    // 移除围栏覆盖物
    private void removeMapOverLay(){
        if (circleOverlay!=null){
            circleOverlay.remove();
            isFenceshowing= false;
        }
    }

    //设置围栏，地图点击监听器
    protected BaiduMap.OnMapClickListener mapClickListener = new BaiduMap.OnMapClickListener() {

        public void onMapClick(LatLng arg0) {
            // TODO Auto-generated method stub
            mBaiduMap.clear();

            MapStatus mMapStatus = new MapStatus.Builder().target(arg0).zoom(17).build();
            msUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);

            fenceOverlayTemp = fenceOverlay;
            fenceOverlay = new CircleOptions().fillColor(0x000000FF).center(arg0)
                    .stroke(new Stroke(5, Color.rgb(0xff, 0x00, 0x33)))
                    .radius(radius);
            addMapOverLay();
            createOrUpdateDialog(arg0);
        }

        public boolean onMapPoiClick(MapPoi arg0) {
            // TODO Auto-generated method stub
            return false;
        }
    };

    private void createOrUpdateDialog(final LatLng latLng) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapTraceActivity.this);

        builder.setTitle("确定设置围栏?");

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                mBaiduMap.clear();
                // 添加覆盖物
                if (null != fenceOverlayTemp) {
                    fenceOverlay = fenceOverlayTemp;
                }
                radius = radiusTemp;
                // 围栏覆盖物
                if (null != fenceOverlay) {
                    mBaiduMap.addOverlay(fenceOverlay);
                }
                mBaiduMap.setOnMapClickListener(null);
            }
        });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                if (0 == fenceId) {
                    // 创建围栏
                    createFence(latLng);
                } else {
                    // 更新围栏
                    updateFence(latLng);
                }
                mBaiduMap.setOnMapClickListener(null);
            }


        });
        builder.show();
    }

    private void createFence(LatLng latLng) {
        //创建者（entity标识）
        String creator = entityName;
        //围栏名称
        String fenceName = entityName + "_fence";
        //围栏描述
        String fenceDesc = "手机";
        //监控对象列表（多个entityName，以英文逗号"," 分割）
        String monitoredPersons = entityName;
        //观察者列表（多个entityName，以英文逗号"," 分割）
        String observers = entityName;
        //生效时间列表
        String validTimes = "0800,2300";
        //生效周期
        int validCycle = 4;
        //围栏生效日期
        String validDate = "";
        //生效日期列表
        String validDays = "";
        //坐标类型 （1：GPS经纬度，2：国测局经纬度，3：百度经纬度）
        int coordType = 3;
        //围栏圆心（圆心位置, 格式 : "经度,纬度"）
        String center = latLng.longitude+","+latLng.latitude;
        //围栏半径（单位 : 米）
        double radius = 500;
        //报警条件（1：进入时触发提醒，2：离开时触发提醒，3：进入离开均触发提醒）
        int alarmCondition = 3;

        //创建圆形地理围栏
        MyApplication.client.createCircularFence(serviceId, creator, fenceName, fenceDesc, monitoredPersons, observers,validTimes,
                validCycle, validDate, validDays, coordType, center, radius, precision,alarmCondition, onGeoFenceListener);
    }

    private void updateFence(LatLng latLng) {
        // 围栏名称
        String fenceName = entityName + "_fence";
        // 围栏ID
        int fenceId = this.fenceId;
        // 围栏描述
        String fenceDesc = "手机";
        // 监控对象列表（多个entityName，以英文逗号"," 分割）
        String monitoredPersons = entityName;
        // 观察者列表（多个entityName，以英文逗号"," 分割）
        String observers = entityName;
        // 生效时间列表
        String validTimes = "0800,2300";
        // 生效周期
        int validCycle = 4;
        // 围栏生效日期
        String validDate = "";
        // 生效日期列表
        String validDays = "";
        // 坐标类型 （1：GPS经纬度，2：国测局经纬度，3：百度经纬度）
        int coordType = 3;
        // 围栏圆心（圆心位置, 格式 : "经度,纬度"）
        String center = latLng.longitude+","+latLng.latitude;
        // 围栏半径（单位 : 米）
        double radius = this.radius;
        // 报警条件（1：进入时触发提醒，2：离开时触发提醒，3：进入离开均触发提醒）
        int alarmCondition = 3;

        MyApplication.client.updateCircularFence(serviceId, fenceName, fenceId, fenceDesc,
                monitoredPersons,
                observers, validTimes, validCycle, validDate, validDays, coordType, center, radius, precision,alarmCondition,
                onGeoFenceListener);
    }


    @Override
    public void onPause() {
        super.onPause();
        trace_bmapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (MyApplication.client!=null){
            MyApplication.client.onDestroy();
        }
        trace_bmapView.onDestroy();
    }


}
