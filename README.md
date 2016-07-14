# HttpZXQClient
异步网络请求

BaseZXQTask一个类搞定，目前仅支持GET、POST但不包含Https请求,而且向后台传输的数据格式是JSON形式，若是GET，Url地址还需自己拼接，后面会逐步完善。

##### 初始化
```java
   BaseZXQTask.Builder builder = new BaseZXQTask.Builder(this);
```

##### 参数设置
```java
        //网络请求类型 POST/GET
        builder.setHttpType("POST");
        //后台接收的数据，若有多个，就多次调用addReuqestData
        builder.addReuqestData("auth_id", "15711412157");
        builder.addReuqestData("activation_code", "666666");
        //网络请求头文件，若需要可以实现多次调用
        builder.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        builder.addRequestProperty("Accept", "application/json");
        //是否显示对话框
        builder.setIsShowDialog(true);
        //对话框提示信息
        builder.setMessage("正在加载数据");
        //请求超时
        builder.setRequestTimeout(60 * 1000);
        //请求结果
        builder.setCallback(new BaseZXQTask.HttpResult() {
            @Override
            public void onResultSuccess(String s) {
               // 成功 code = 200;
            }

            @Override
            public void onResultFailure(int code, String msg) {
              // 失败 code != 200 及后台返回信息或者异常处理返回信息
            }
        });
        //传入请求地址
        builder.request("http://10.7.7.100:3000/api/activation");
```
