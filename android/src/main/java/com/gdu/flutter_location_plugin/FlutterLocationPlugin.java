package com.gdu.flutter_location_plugin;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import static android.content.Context.LOCATION_SERVICE;

/** FlutterLocationPlugin */
@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class FlutterLocationPlugin  implements MethodCallHandler{
  private static final String CHANNEL = "location_plugin";
  private Result result;
  private final int RETURNLOCATION = 0;
  private final int LOCATIONFAIELD = 1;
  private static Registrar registrar;

  /** Plugin registration. */
  public static void registerWith(Registrar registrar) {
    FlutterLocationPlugin.registrar=registrar;
    final MethodChannel channel = new MethodChannel(registrar.messenger(), CHANNEL);
    channel.setMethodCallHandler(new FlutterLocationPlugin());
  }



  @Override
  public void onMethodCall(MethodCall call, Result result) {
    if (call.method.equals("getPlatformLocation")) {
      getLocation();
      this.result = result;
//      result.success("Android " + android.os.Build.VERSION.RELEASE);
    } else {
      result.notImplemented();
    }
  }

  @TargetApi(Build.VERSION_CODES.GINGERBREAD)
  private void getLocation() {
    LocationManager locationManager = (LocationManager) registrar.context().getSystemService(LOCATION_SERVICE);
    if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
      Log.e("yuhao", "isProviderEnabled: ");
      locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
          Log.e("yuhao", "onLocationChanged: ");
          getAddress(location);
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
      }, null);
    }
  }


  // 获取地址信息
  private void getAddress(final Location location) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        String sAddress = "";
        try {
          if (location != null) {
            Geocoder gc = new Geocoder(registrar.context());
            List<Address> addresses = gc.getFromLocation(location.getLatitude(),
                    location.getLongitude(), 1);
            if (addresses.size() > 0) {
              Address address = addresses.get(0);
              if (!TextUtils.isEmpty(address.getLocality())) {
                sAddress = address.getLocality();
              } else {
                sAddress = "定位失败";
              }
              Log.e("gzq", "sAddress：" + sAddress);
            }
          }
          Log.e("yuhao", "sAddress=" + sAddress);

          handler.obtainMessage(RETURNLOCATION, sAddress + "," + location.getLatitude() + "," + location.getLongitude()).sendToTarget();
        } catch (Exception e) {
//                    result.error("UNAVAILABLE", "UnKnow Address.", null);
          handler.sendEmptyMessage(LOCATIONFAIELD);
          Log.e("yuhao", "e=" + e.getMessage());
          e.printStackTrace();
        }
      }
    }).start();
  }


  private Handler handler = new Handler(new Handler.Callback() {
    @Override
    public boolean handleMessage(Message msg) {
      switch (msg.what) {
        case RETURNLOCATION:
          result.success((String) msg.obj);
          break;

        case LOCATIONFAIELD:
          result.error("UNAVAILABLE", "UnKnow Address.", null);
          break;
      }
      return false;
    }
  });
}
