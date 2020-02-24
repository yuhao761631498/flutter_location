import 'dart:async';
import 'package:permission_handler/permission_handler.dart';
import 'package:flutter/services.dart';

class FlutterLocationPlugin {
  static const MethodChannel _channel = const MethodChannel('location_plugin');

  static Future<String> get platformLocation async {
    String location = 'Locating...';
    //只有当用户同时点选了拒绝开启权限和不再提醒后才会true
    bool isSHow = await PermissionHandler().shouldShowRequestPermissionRationale(PermissionGroup.location);
    // 申请结果  权限检测
    PermissionStatus permission = await PermissionHandler().checkPermissionStatus(PermissionGroup.location);

    if (permission != PermissionStatus.granted) {
      //权限没允许
      //如果弹框不在出现了，那就跳转到设置页。
      //如果弹框还能出现，那就不用管了，申请权限就行了
      if (!isSHow) {
        await PermissionHandler().openAppSettings();
      } else {
        await PermissionHandler().requestPermissions([PermissionGroup.location]);
        //此时要在检测一遍，如果允许了就请求天气。
        //没允许就就提示。
        PermissionStatus pp = await PermissionHandler().checkPermissionStatus(PermissionGroup.location);
        if (pp == PermissionStatus.granted) {
          try {
            location = await _channel.invokeMethod('getPlatformLocation');
          } on PlatformException catch (e) {
            location = "Failed to get location";
          }
        } else {
          // 参数1：提示消息// 参数2：提示消息多久后自动隐藏// 参数3：位置
//          Toast.show("请允许存储权限，并重试！", context, duration: Toast.LENGTH_SHORT, gravity: Toast.CENTER);
          return "no location permission";
        }
      }
    } else {
      //权限允许了
      try {
        location = await _channel.invokeMethod('getPlatformLocation');
      } on PlatformException catch (e) {
        location = "Failed to get location";
      }
      print("_location" + location);
    }
    return location;
  }
}
