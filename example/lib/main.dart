import 'package:flutter/material.dart';
import 'package:flutter_install_app_plugin/flutter_install_app_plugin.dart';
import 'package:package_info_plus/package_info_plus.dart';

import 'app_upgrade/app_upgrade.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      theme: ThemeData(
          primaryColor: Colors.lightBlueAccent,
          bottomAppBarTheme: BottomAppBarTheme(color: Colors.blue)),
      //不显示 debug 标签
      debugShowCheckedModeBanner: false,
      home: const IndexPage(),
    );
  }
}

class IndexPage extends StatefulWidget {
  const IndexPage({Key? key}) : super(key: key);

  @override
  _IndexPageState createState() => _IndexPageState();
}

class _IndexPageState extends State<IndexPage> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Plugin example app'),
      ),
      body: Center(
        child: Column(
          children: <Widget>[
            FlatButton(
                child: Text("打开appstore"),
                onPressed: () {
                  FlutterInstallAppPlugin.gotoAppStore(
                      "https://apps.apple.com/cn/app/id1472328992");
                }),
            FlatButton(
                child: Text("点击安装"),
                onPressed: () {
                  FlutterInstallAppPlugin.gotoAppStore(
                      "https://apps.apple.com/cn/app/id1472328992");
                }),
            FlatButton(
                child: Text("Android 点击安装"),
                onPressed: () async {
                  //获取当前App的版本信息
                  PackageInfo packageInfo = await PackageInfo.fromPlatform();

                  String appName = packageInfo.appName;
                  String packageName = packageInfo.packageName;
                  String version = packageInfo.version;
                  String buildNumber = packageInfo.buildNumber;
                  print("appName $appName");
                  print("packageName $packageName");
                  print("version $version");
                  print("buildNumber $buildNumber");

                  checkAppVersion(context, showToast: true);
                }),
          ],
        ),
      ),
    );
  }
}
