package com.github.zxq.httpclient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Created by Administrator on 2016/1/8.
 * 异步线程
 *
 * @author ZhangXiangQian
 */
public class BaseZXQTask extends AsyncTask<Void, Void, String>

{
    private ProgressDialog mDialog;
    private HttpParams taskParams;
    private Activity context;
    private String errMessage;
    private int responseCode = 200;
    private String path;


    public BaseZXQTask(Activity context, String url, HttpParams taskParams) {
        this.taskParams = taskParams;
        this.context = context;
        this.path = url;
        mDialog = new ProgressDialog(context);
        mDialog.setTitle(taskParams.title);
        mDialog.setMessage(taskParams.msg);
        mDialog.setCancelable(taskParams.isCancel);
    }


    @Override
    protected void onPostExecute(String rtn) {
        super.onPostExecute(rtn);
        if (mDialog != null && !context.isFinishing() && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        if (200 == responseCode) {
            taskParams.getCallback().onResultSuccess(rtn);
        } else {
            taskParams.getCallback().onResultFailure(responseCode, errMessage);
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mDialog != null && !context.isFinishing() && taskParams.isShowDialog) {
            mDialog.show();
        }
    }

    @Override
    protected String doInBackground(Void... params) {
        HttpURLConnection conn = null;
        InputStream is = null;
        ByteArrayOutputStream bos = null;
        URL url;
        try {
            Log.i("httpZXQTask", "path:" + path);
            url = new URL(path);
            bos = new ByteArrayOutputStream();
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(taskParams.getHttpType());
            conn.setReadTimeout((int) taskParams.getReadTimeout());
            conn.setConnectTimeout((int) taskParams.getRequestTimeout());
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            //添加头文件数据
            if (taskParams.getRequestProperty() != null && taskParams.getRequestProperty().size() != 0) {
                Iterator<String> it = taskParams.getRequestProperty().keySet().iterator();
                while (it.hasNext()) {
                    String key = it.next();
                    String value = taskParams.getRequestProperty().get(key);
                    conn.setRequestProperty(key, value);
                    Log.i("httpZXQTask", "data:" + "KEY:" + key + ";value:" + value + "\n");
                }
            }
            //后台传输数据
            if (taskParams.getRequestData() != null && taskParams.getRequestData().size() != 0) {
                Iterator<String> it = taskParams.getRequestData().keySet().iterator();
                JSONObject jo = new JSONObject();
                while (it.hasNext()) {
                    String key = it.next();
                    String value = taskParams.getRequestData().get(key);
                    jo.put(key, value);
                }
                Log.i("httpZXQTask", "data:" + jo.toString());
                conn.getOutputStream().write(jo.toString().getBytes());
            }
            int code = conn.getResponseCode();
            byte[] b = new byte[1024];
            if (HttpURLConnection.HTTP_OK == code) {
                is = conn.getInputStream();
                int len;
                while ((len = is.read(b)) != -1) {
                    bos.write(b, 0, len);
                }
                String result = bos.toString();
                return result;
            } else {
                is = conn.getErrorStream();
                int len;
                while ((len = is.read(b)) != -1) {
                    bos.write(b, 0, len);
                }
                responseCode = code;
                errMessage = bos.toString();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            responseCode = 400;
            errMessage = "错误请求";
        } catch (IOException e) {
            e.printStackTrace();
            responseCode = 400;
            errMessage = "IO流异常";
        } catch (JSONException e) {
            e.printStackTrace();
            responseCode = 400;
            errMessage = "JSON解析异常";
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(bos != null){
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static class Builder {
        private HttpParams P;

        private Activity context;

        public Builder(Activity context) {
            this.context = context;
            P = new HttpParams();
        }

        public Builder setTitle(String title) {
            P.setTitle(title);
            return this;
        }

        public Builder setTitle(int resId) {
            return setTitle(context.getResources().getString(resId));
        }

        public Builder setMessage(String msg) {
            P.setMsg(msg);
            return this;
        }


        public Builder setMessage(int msgId) {
            return setMessage(context.getResources().getString(msgId));
        }


        public Builder setIsCancel(boolean isCancel) {
            P.setCancel(isCancel);
            return this;
        }

        public Builder setIsShowDialog(boolean isShowDialog) {
            P.setShowDialog(isShowDialog);
            return this;
        }

        public Builder addRequestProperty(String key, String value) {
            if (P.getRequestProperty() == null) {
                Map<String, String> data = new HashMap<>();
                data.put(key, value);
                P.setRequestProperty(data);
            } else {
                P.getRequestProperty().put(key, value);
            }
            return this;
        }

        public Builder addReuqestData(String key, String value) {
            if (P.getRequestData() == null) {
                Map<String, String> data = new HashMap<>();
                data.put(key, value);
                P.setRequestData(data);
            } else {
                P.getRequestData().put(key, value);
            }
            return this;
        }

        public Builder setHttpType(String type) {
            P.setHttpType(type);
            return this;
        }

        public Builder setCallback(HttpResult callback) {
            P.setCallback(callback);
            return this;
        }

        public Builder setRequestTimeout(long requestTimeout) {
            P.setRequestTimeout(requestTimeout);
            return this;
        }

        public Builder setReadTimeout(long readTimeout) {
            P.setReadTimeout(readTimeout);
            return this;
        }

        public void request(String url) {
            new BaseZXQTask(context, url, P).execute();
        }
    }


    public static class HttpParams {

        private boolean isCancel = true;
        private boolean isShowDialog = false;
        private String msg;
        private String title;
        private HttpResult mHttpResult;
        // Http类型 GET？Post?
        private String httpType;
        //requestProperty 请求头
        private Map<String, String> RequestProperty;
        //后台传输数据
        private Map<String, String> RequestData;
        //请求超时
        private long requestTimeout;
        //读取超时
        private long readTimeout;
        //回调
        private HttpResult callback;

        public void setCancel(boolean cancel) {
            isCancel = cancel;
        }

        public void setShowDialog(boolean showDialog) {
            isShowDialog = showDialog;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setmHttpResult(HttpResult mHttpResult) {
            this.mHttpResult = mHttpResult;
        }

        public void setHttpType(String httpType) {
            this.httpType = httpType;
        }

        public String getHttpType() {
            return httpType;
        }

        public Map<String, String> getRequestProperty() {
            return RequestProperty;
        }

        public void setRequestProperty(Map<String, String> requestProperty) {
            RequestProperty = requestProperty;
        }

        public long getRequestTimeout() {
            return requestTimeout;
        }

        public void setRequestTimeout(long requestTimeout) {
            this.requestTimeout = requestTimeout;
        }

        public long getReadTimeout() {
            return readTimeout;
        }

        public void setReadTimeout(long readTimeout) {
            this.readTimeout = readTimeout;
        }

        public Map<String, String> getRequestData() {
            return RequestData;
        }

        public void setRequestData(Map<String, String> requestData) {
            RequestData = requestData;
        }

        public HttpResult getCallback() {
            return callback;
        }

        public void setCallback(HttpResult callback) {
            this.callback = callback;
        }
    }

    public interface HttpResult {
        public void onResultSuccess(String s);

        public void onResultFailure(int code, String msg);
    }

}
