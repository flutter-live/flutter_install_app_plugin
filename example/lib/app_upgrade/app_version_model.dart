class AppVersionModel {
  bool isNeed = false;
  String? isForce = "false";
  String updateContent = "";
  String packageUrl = "";

  AppVersionModel.fromJson(Map<String, dynamic> map) {
    isNeed = map["isNeed"];
    isForce = map["isForce"];
    updateContent = map["updateContent"];
    packageUrl = map["packageUrl"];
  }
}
