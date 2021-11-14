package com.lzd.flutter_install_app_plugin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import java.io.File;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

/** FlutterInstallAppPlugin */
public class FlutterInstallAppPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
  private MethodChannel channel;
  private File apkFile = null;
  private String appId = null;
  private Activity activity = null;

  private static final String INSTALL_APP_NAME = "flutter_install_app_plugin";
  private static final Integer installRequestCode = 1234;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), INSTALL_APP_NAME);
    channel.setMethodCallHandler(this);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    } else if(call.method.equals(("installApk"))){
      String filePath = call.argument("filePath");
      String appId = call.argument("appId");
      Log.d("android plugin", "installApk $filePath $appId");
      try {
        installApk(filePath, appId);
      } catch (Exception e) {
        e.printStackTrace();
        result.error(e.getClass().getName(), e.getMessage(), null);
      }
      result.success("Success");
    } else {
      result.notImplemented();
    }
  }

  private void installApk(String filePath, String appId) throws Exception {
    if (filePath == null) throw new Exception("fillPath is null!");
    if(this.activity == null) throw new Exception("context is null!");
    File file = new File(filePath);
    if (!file.exists()) throw new Exception("$filePath is not exist! or check permission");
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      if (canRequestPackageInstalls(activity)) install24(activity, file, appId);
      else {
        showSettingPackageInstall(activity);
        this.apkFile = file;
        this.appId = appId;
      }
    } else {
      installBelow24(activity, file);
    }
  }

  /**
   * android24及以上安装需要通过 ContentProvider 获取文件Uri，
   * 需在应用中的AndroidManifest.xml 文件添加 provider 标签，
   * 并新增文件路径配置文件 res/xml/provider_path.xml
   * 在android 6.0 以上如果没有动态申请文件读写权限，会导致文件读取失败，你将会收到一个异常。
   * 插件中不封装申请权限逻辑，是为了使模块功能单一，调用者可以引入独立的权限申请插件
   */
  private void installBelow24(Context context, File file) {
    Intent intent = new Intent(Intent.ACTION_VIEW);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    Uri uri = Uri.fromFile(file);
    intent.setDataAndType(uri, "application/vnd.android.package-archive");
    context.startActivity(intent);
  }

  private void showSettingPackageInstall(Activity activity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      Log.d("SettingPackageInstall", ">= Build.VERSION_CODES.O");
      Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
      intent.setData(Uri.parse("package:" + activity.getPackageName()));
      activity.startActivityForResult(intent, installRequestCode);
    } else {
      throw new RuntimeException("VERSION.SDK_INT < O");
    }
  }

  private boolean canRequestPackageInstalls(Activity activity) {
    return Build.VERSION.SDK_INT <= Build.VERSION_CODES.O || activity.getPackageManager().canRequestPackageInstalls();
  }

  private void install24(Context context, File file, String appId) throws Exception {
    if (context == null) throw new Exception("context is null!");
    if (file == null) throw new Exception("file is null!");
    if (appId == null) throw new Exception("appId is null!");
    Intent intent = new Intent(Intent.ACTION_VIEW);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    Uri uri = FileProvider.getUriForFile(context, appId + ".fileProvider.install", file);
    intent.setDataAndType(uri, "application/vnd.android.package-archive");
    context.startActivity(intent);
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
    activity = binding.getActivity();
    binding.addActivityResultListener(new PluginRegistry.ActivityResultListener() {
      @Override
      public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK && requestCode == installRequestCode){
          try {
            install24(binding.getActivity().getApplicationContext(), apkFile, appId);
          } catch (Exception e) {
            e.printStackTrace();
          }
          return true;
        }else{
          return false;
        }
      }
    });
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    this.onDetachedFromActivity();
  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
    this.onAttachedToActivity(binding);
  }

  @Override
  public void onDetachedFromActivity() {
    activity = null;
  }
}
