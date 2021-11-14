/// @description:
/// @date: 2021/11/14 12:07
/// @version: 1.0
class ResponseInfo {
  bool success;
  int code;
  String message;
  dynamic data;

  ResponseInfo(
      {this.success = true, this.code = 200, this.data, this.message = "请求成功"});

  ResponseInfo.error(
      {this.success = false, this.code = 201, this.message = "请求异常"});
}
