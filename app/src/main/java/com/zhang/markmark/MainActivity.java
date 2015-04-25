package com.zhang.markmark;

import android.graphics.Bitmap;
import android.location.Location;
import android.os.Environment;
import android.os.PersistableBundle;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;

import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends ActionBarActivity
        implements LocationSource, AMapLocationListener, AMap.OnMapScreenShotListener {

    private MapView mapView;
    private AMap aMap;
    private LocationManagerProxy mLocationManagerProxy;
    private OnLocationChangedListener mListener;

    private boolean isStart = false;//开始标记
    private boolean isEnd = false;//结束标记

    private AMapLocation location = null;

    private Button btnStart;
    private Button btnEnd;
    private TextView txtStart;
    private TextView txtEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        init();

        btnStart = (Button) findViewById(R.id.btn_start);
        btnEnd = (Button) findViewById(R.id.btn_end);
        txtStart = (TextView) findViewById(R.id.txtStart);
        txtEnd = (TextView) findViewById(R.id.txtEnd);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isStart = true;
                v.setEnabled(false);
                if (!location.equals(null)) {
                    txtStart.setText(new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss").format(new Date())
                            + "\n" + location.getAddress());
                    isStart = false;
                }
            }
        });

        btnEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEnd = true;
                v.setEnabled(false);
                if (!location.equals(null)) {
                    txtEnd.setText(new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss").format(new Date())
                            + "\n" + location.getAddress());
                    isEnd = false;
                }

            }
        });

    }

    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
            setUpMap();
        }
    }

    private void setUpMap() {

        aMap.getUiSettings().setCompassEnabled(true);

        aMap.setLocationSource(this);
        aMap.getUiSettings().setMyLocationButtonEnabled(true);
        aMap.setMyLocationEnabled(true);
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 定位成功后回调函数
     *
     * @param aMapLocation
     */
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null && aMapLocation.getAMapException().getErrorCode() == 0) {
            //获取位置信息
            if (mListener != null && aMapLocation != null) {
                if (aMapLocation.getAMapException().getErrorCode() == 0) {
                    mListener.onLocationChanged(aMapLocation);
                    Toast.makeText(this, aMapLocation.getAddress(), Toast.LENGTH_LONG).show();
                    location = aMapLocation;
                    if (isStart) {
                        txtStart.setText(new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss").format(new Date())
                                + "\n" + location.getAddress());
                        isStart = false;
                    }
                    if (isEnd){
                        txtEnd.setText(new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss").format(new Date())
                                + "\n" + location.getAddress());
                    }
                }
            }
        }
    }

    /**
     * 此方法已经废弃
     *
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    /**
     * 激活定位
     *
     * @param listener
     */
    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mLocationManagerProxy == null) {
            mLocationManagerProxy = LocationManagerProxy.getInstance(this);
            mLocationManagerProxy.requestLocationData(
                    LocationProviderProxy.AMapNetwork,
                    60 * 1000,
                    10,
                    this
            );
        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationManagerProxy != null) {
            mLocationManagerProxy.removeUpdates(this);
            mLocationManagerProxy.destroy();
        }
        mLocationManagerProxy = null;
    }

    @Override
    public void onMapScreenShot(Bitmap bitmap) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        try{
            FileOutputStream fos = new FileOutputStream(
                    Environment.getExternalStorageDirectory()+
                            "/test_"+sdf.format(new Date())+".png");
            boolean b = bitmap.compress(Bitmap.CompressFormat.PNG,100,fos);
            try{
                fos.flush();
            }catch (IOException e){
                e.printStackTrace();
            }
            if (b){
                Toast.makeText(this,"截屏成功",Toast.LENGTH_LONG).show();
            }else {
                Toast.makeText(this,"截屏失败",Toast.LENGTH_LONG).show();
            }
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }

    }
}
