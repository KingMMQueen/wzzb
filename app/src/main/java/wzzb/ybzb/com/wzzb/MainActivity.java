package wzzb.ybzb.com.wzzb;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class MainActivity extends Activity{

    public Timer timer=null;
    public TimerTask timerTask=null;
    public TextView tv_daojishi;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frist_adv);
        tv_daojishi=(TextView)findViewById(R.id.tv_daojishi);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //installDefautAPK();
        //startPost(MainActivity.this);
        timerTask=new TimerTask() {
            @Override
            public void run() {
                if(Integer.parseInt(tv_daojishi.getText().toString().trim())<=0){
                    startAPP("com.howfor.player",MainActivity.this);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_daojishi.setText((Integer.parseInt(tv_daojishi.getText().toString().trim())-1)+"");
                    }
                });

            }
        };
        timer=new Timer();
        timer.schedule(timerTask,2000,1000);
        //startAPP("com.iptv.myappmanager",MainActivity.this);
        //startAPP("com.howfor.player",MainActivity.this);
    }

    public void startAPP(String appPackageName, Activity activity){
        try{
            Intent intent = activity.getPackageManager().getLaunchIntentForPackage(appPackageName);
            activity.startActivity(intent);
            overridePendingTransition(Animation.INFINITE, Animation.INFINITE);
            finish();
        }catch(Exception e){
            int a = 0;
            int b = a;
        }
    }
    private void startPost(Context context)
    {
        HashMap<String,String> map=new HashMap<String, String>();
        map.put("room_number",getMac(context));
        map.put("mac_address",getMac(context));
        map.put("ip",getClientIP(MainActivity.this));
        map.put("last_login_time",getDate());
        map.put("screen","1920*1080");
        map.put("rate","100M");
        map.put("memory",getClientIP(MainActivity.this));
        map.put("storage",getLocalVersion(MainActivity.this)+"");
        //map.put("net_status","<5MS");
        map.put("net_status",android.os.Build.SERIAL);
        map.put("cpu",Build.VERSION.RELEASE);
        map.put("status","1");
        map.put("wifi","开启");
        OkHttpUtils.post(MainActivity.this,"http://jxyy.58120.net/api/center/room/add/agent_id/34", map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String  sf=response.body().string();
                JSONObject jsonObject = null;
                String mac=getMac(MainActivity.this);
                try {
                    jsonObject = new JSONObject(sf);
                    int code = jsonObject.getInt("code");
                    final String msg=jsonObject.getString("msg");
                    if(code==0){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,msg, Toast.LENGTH_LONG);
                                finish();
                            }
                        });
                    }else{
                        getDataForMainActivity();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }
    public void getDataForMainActivity(){
        OkHttpUtils.get(MainActivity.this,"http://jxyy.58120.net/api/center/home/index/agent_id/34/mac_address/"+getMac(MainActivity.this), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try{
                    if(response.code()==200) {
                        String res = response.body().string();
                        JSONObject jsonObject = null;
                        jsonObject = new JSONObject(res);
                        final String vipmac=jsonObject.getJSONObject("data").getString("vipmac");
                        if (!vipmac.equals("")){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try{
                                        getRoot(vipmac);
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }

                    }else{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,"接口访问出错！", Toast.LENGTH_LONG);
                                return;
                            }
                        });

                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

    }
    DataOutputStream dataOutputStream;
    BufferedReader errorStream;
    public void getRoot(final String vipmac){
        new Thread(){
            @Override
            public void run() {
                Process process = null;
                try {
                    process = Runtime.getRuntime().exec("su");
                    dataOutputStream = new DataOutputStream(process.getOutputStream());
                    dataOutputStream.flush();
                    dataOutputStream.writeBytes("netcfg eth0 hwaddr "+vipmac+"\n");
                    dataOutputStream.writeBytes("netcfg eth0 up\n");
                    dataOutputStream.writeBytes("exit\n");
                    process.waitFor();
                    errorStream = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    String msg = "";
                    String line;
                    // 读取命令的执行结果
                    while ((line = errorStream.readLine()) != null) {
                        msg += line;
                    }
                    //   TestUtil.uploadTest(context,"5，失败原因："+msg);
                    Log.d("TAG", "install msg is " + msg);
                    // 如果执行结果中包含Failure字样就认为是安装失败，否则就认为安装成功
                    if (msg.contains("Failure")) {

                    }

                } catch (final IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this,e.getCause().getMessage(), Toast.LENGTH_LONG);
                        }
                    });
                    e.printStackTrace();
                } catch (final InterruptedException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this,e.getCause().getMessage(), Toast.LENGTH_LONG);
                        }
                    });
                    e.printStackTrace();

                }
            }
        }.start();


    }
    public static String getMac(Context context){
        String wifi_mac = (String) SPUtils.get(context, "DEVICE_MAC", "");
        String ethernetAddress = getEthernetAddress();
        if(!TextUtils.isEmpty(ethernetAddress)){
            SPUtils.put(context,"DEVICE_MAC",ethernetAddress);
            return ethernetAddress;
        }
        if (TextUtils.isEmpty(wifi_mac)) {
            WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = (null==wifiManager)?null:wifiManager.getConnectionInfo();
            if(null != info){
                wifi_mac = info.getMacAddress();
                SPUtils.put(context,"DEVICE_MAC",wifi_mac);
            }
        }
        return wifi_mac;

    }
    private static String getEthernetAddress() {
        try {
            return loadFileAsString("/sys/class/net/eth0/address").toUpperCase(Locale.ENGLISH).substring(0, 17);
        } catch (IOException e) {
            return null;
        }
    }

    private static String loadFileAsString(String filePath) throws java.io.IOException{
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        char[] buf = new char[1024]; int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }

    public static String getClientIP(Context context) {

        try {
            // 获取本地设备的所有网络接口
            Enumeration<NetworkInterface> enumerationNi = NetworkInterface
                    .getNetworkInterfaces();
            while (enumerationNi.hasMoreElements()) {
                NetworkInterface networkInterface = enumerationNi.nextElement();
                String interfaceName = networkInterface.getDisplayName();
                if(getNetMode(context)==1) {
                    // 如果是有限网卡
                    if (interfaceName.equals("eth0")) {
                        Enumeration<InetAddress> enumIpAddr = networkInterface
                                .getInetAddresses();

                        while (enumIpAddr.hasMoreElements()) {
                            // 返回枚举集合中的下一个IP地址信息
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            // 不是回环地址，并且是ipv4的地址
                            if (!inetAddress.isLoopbackAddress()
                                    && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }

                    }
                }
                if(getNetMode(context)==2) {
                    //  如果是无限网卡
                    if (interfaceName.equals("wlan0")) {
                        Enumeration<InetAddress> enumIpAddr = networkInterface
                                .getInetAddresses();

                        while (enumIpAddr.hasMoreElements()) {
                            // 返回枚举集合中的下一个IP地址信息
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            // 不是回环地址，并且是ipv4的地址
                            if (!inetAddress.isLoopbackAddress()
                                    && inetAddress instanceof Inet4Address) {

                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                }
            }

        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "0.0.0.0";

    }
    /**
     * 判断当前网络有没有联网
     * 并且判断是有线还是无线
     *
     * 0:无网络连接
     * 1：有线网络
     * 2：无线网络
     *
     * @return
     */
    public static int getNetMode(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info == null) {
            return 0;
        }
        boolean iscon = info.isAvailable();
        if (!iscon) {
            return 0;
        }
        if (info.getType() == ConnectivityManager.TYPE_ETHERNET) {
            return 1;
        } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
            return 2;
        } else {
            return 0;
        }
    }
    public static String getDate() {
        SimpleDateFormat formatter   =   new   SimpleDateFormat   ("yyyy-MM-dd HH:mm:ss");
        Date curDate   =   new   Date(System.currentTimeMillis());//获取当前时间     String   str   =   formatter.format(curDate);
        return formatter.format(curDate);
    }
    /* 获取本地软件版本号​
           */
    public static int getLocalVersion(Context ctx) {
        int localVersion = 0;
        try {
            PackageInfo packageInfo = ctx.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0);
            localVersion = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return localVersion;
    }



    boolean canClick;
    private void installDefautAPK() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean avilible = isAvilible(MainActivity.this, "com.jiang.tvlauncher");
                if(!avilible){
                    File fileDir = new File(Environment.getExternalStorageDirectory() + File.separator + "SXSystemDir");
                    fileDir.mkdir();
                    File apkFile = new File(Environment.getExternalStorageDirectory() + "/SXSystemDir/", "com.jiang.tvlauncher.apk");// 设置路径
                    copyFile("com.jiang.tvlauncher.apk", apkFile.getAbsolutePath());
                    if( ApkController.install(apkFile.getAbsolutePath(), MainActivity.this, false)){
                        canClick = true;
                    }else {
                        canClick = false;
                    }

                }else{
                    canClick = true;
                }
            }
        }).start();
    }
    private boolean isAvilible(Context context, String packageName )
    {
        final PackageManager packageManager = context.getPackageManager();
        // 获取所有已安装程序的包信息
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        for ( int i = 0; i < pinfo.size(); i++ )
        {
            if(pinfo.get(i).packageName.equalsIgnoreCase(packageName))
                return true;
        }
        return false;
    }
    private void copyFile(String filename,String destinationPath) {
        AssetManager assetManager = getAssets();
        InputStream in = null;
        OutputStream out = null;
        String newFileName = null;
        try {
            in = assetManager.open(filename);
            newFileName = destinationPath;
            out = new FileOutputStream(newFileName);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.flush();
            out.close();
        } catch (Exception e) {
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode==KeyEvent.KEYCODE_DPAD_CENTER||keyCode==KeyEvent.KEYCODE_ENTER){
            startAPP("com.iptv.myappmanager",MainActivity.this);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(timerTask!=null){
            timerTask.cancel();
            timerTask=null;
        }
        if(timer!=null){
            timer.cancel();
            timer=null;
        }
    }
}
