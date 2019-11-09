package wzzb.ybzb.com.wzzb;

import android.app.Activity;
import android.content.Context;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpUtils {
    public static final MediaType MIXED = MediaType.parse("multipart/mixed");
    public static final MediaType ALTERNATIVE = MediaType.parse("multipart/alternative");
    public static final MediaType DIGEST = MediaType.parse("multipart/digest");
    public static final MediaType PARALLEL = MediaType.parse("multipart/parallel");
    public static final MediaType FORM = MediaType.parse("multipart/form-data");

    public interface GetEntityCallBack{
        // 2:定义一个或多个抽象方法
        void getEntity(Object obj);
    }

    // 声明一个全局变量
    public static  GetEntityCallBack callBack;
    //3:再本类中定义一个接口实例(只是定义，而不是创建)：目的是接受调用者传过来的接口
    public static  void setGetEntityCallBack(GetEntityCallBack callBack1){
        // GetEntityCallBack变成全局变量
        callBack=callBack1;
    }
    public static Activity context;
    // Type类型封装的  也可以把type改成Class 下面的参数也是class
    public static void post(final Activity con , String path, HashMap hashMap, final NetResponse netResponse){
        context = con;
        OkHttpClient client = new OkHttpClient();

        Request.Builder requestBuilder = new Request.Builder();


        requestBuilder.url(path);

        final Request request = requestBuilder.build();

        okhttp3.Call call =client.newCall(request);
        // 接口回调 这个属于异步方法
        call.enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        netResponse.resFailed();
                    }
                });

            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                final String string = response.body().string();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String json = string;
                        if(json == null){
                            netResponse.resFailed();
                        }else{
                            netResponse.resSuccess(json);
                        }
                    }
                });
            }
        });
    }
        // post请求 异步方法
    public static void post(final Context context, final String path, HashMap hashMap, Callback callback){

        OkHttpClient client = new OkHttpClient();
        //构建一个请求体 add参数1 key 参数2 value 发送字段
        RequestBody requestBody = new FormBody.Builder()
                .add("room_number", hashMap.get("room_number").toString())
                .add("mac_address", hashMap.get("mac_address").toString())
                .add("ip", hashMap.get("ip").toString())
                .add("last_login_time", hashMap.get("last_login_time").toString())
                .add("screen", hashMap.get("screen").toString())
                .add("rate", hashMap.get("rate").toString())
                .add("memory", hashMap.get("memory").toString())
                .add("storage", hashMap.get("storage").toString())
                .add("net_status", hashMap.get("net_status").toString())
                .add("cpu", hashMap.get("cpu").toString())
                .add("status", hashMap.get("status").toString())
                .add("wifi", hashMap.get("wifi").toString())
                .build();
        final Request request = new Request.Builder()
                .url(path)
                .post(requestBody)
                .build();
        okhttp3.Call call = client.newCall(request);
        call.enqueue(callback);
    }
    // Get请求 异步方法
    public static void get( final Context context,final String path, Callback callback){
        //OkHttpClient client = new OkHttpClient();
        //.retryOnConnectionFailure(false)
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(50, TimeUnit.SECONDS)
                .readTimeout(50,TimeUnit.SECONDS)
                .writeTimeout(50,TimeUnit.SECONDS)
                .build();

        final Request request = new Request.Builder()
                .url(path)
                .get()
                .build();
        okhttp3.Call call = client.newCall(request);
        call.enqueue(callback);
    }
    // Get请求 异步方法
    public static void get(final String path, Callback callback){
        OkHttpClient client = new OkHttpClient();

        final Request request = new Request.Builder()
                .url(path)
                .get()
                .build();
        okhttp3.Call call = client.newCall(request);
        call.enqueue(callback);
    }

    public static abstract class NetResponse{
        public abstract void resSuccess(String json);
        public abstract void resFailed();
    }
    //同

    public void getSyncResponse(String apkPath) {

        //创建cliect对象
        OkHttpClient cliect=new OkHttpClient();
        //创建一个网络请求
        Request request=new Request.Builder()
                .url(apkPath)
                .build();
//创建网络请求的一个操作类
        Call call=cliect.newCall(request);
        try {
        //返回Response
        //同步请求execute()
            Response re = call.execute();
            if(listener != null){
                listener.success(re);
            }
        } catch (IOException e) {
            if(listener != null){
                listener.failed(e);
            }
            e.printStackTrace();

        }
    }
    public static void getSyncUpLoad(String url) {

        //创建cliect对象
        OkHttpClient cliect=new OkHttpClient();
        //创建一个网络请求
        Request request=new Request.Builder()
                .url(url)
                .build();
//创建网络请求的一个操作类
        Call call=cliect.newCall(request);
        try {
            //返回Response
            //同步请求execute()
            Response re = call.execute();

        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    GetSyncResponseListener listener;
    public void setGetSyncResponseListener(GetSyncResponseListener listener){
        this.listener = listener;
    }
    public interface GetSyncResponseListener{
        public void failed(IOException e);
        public void success(Response response);
    }
}
