package wzzb.ybzb.com.wzzb;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;

public class ApkController {
    /**
     * 描述: 安装
     * 修改人: 吴传龙
     * 最后修改时间:2015年3月8日 下午9:07:50
     */
    public static boolean install(String apkPath,Context context,boolean isSlient){//isSlient是否仅仅静默安装 isSlient true 表示直接静默安装
        //先判断该文件是否存在
        File apkFile = new File(apkPath);
        if(!apkFile.exists()){//文件不存在
            return false;
        }
        // 先判断手机是否有root权限
        if(hasRootPerssion()){
        //    TestUtil.uploadTest(context,"3，有root权限");
            // 有root权限，利用静默安装实现
            return installClient(context,apkPath);
        }else{
         //   TestUtil.uploadTest(context,"4，没有root权限");
            if(!isSlient){
                // 没有root权限，利用意图进行安装
                File file = new File(apkPath);
                if(!file.exists())
                    return false;
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setDataAndType(Uri.fromFile(file),"application/vnd.android.package-archive");
                context.startActivity(intent);
                return true;
            }
           return false;
        }
    }

    /**
     * 描述: 卸载
     * 修改人: 吴传龙
     * 最后修改时间:2015年3月8日 下午9:07:50
     */
    public static boolean uninstall(String packageName,Context context){
        if(hasRootPerssion()){
            // 有root权限，利用静默卸载实现
            return clientUninstall(packageName);
        }else{
         /*   Uri packageURI = Uri.parse("package:" + packageName);
            Intent uninstallIntent = new Intent(Intent.ACTION_DELETE,packageURI);
            uninstallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(uninstallIntent);
            return true;*/
         return false;
        }
    }

    /**
     * 判断手机是否有root权限
     */
    public static boolean hasRootPerssion(){
        PrintWriter PrintWriter = null;
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");
            PrintWriter = new PrintWriter(process.getOutputStream());
            PrintWriter.flush();
            PrintWriter.close();
            int value = process.waitFor();
            return returnResult(value);
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            if(process!=null){
                process.destroy();
            }
        }
        return false;
    }

    /**
     * 静默安装
     */
    public static boolean installClient(Context context ,String apkPath) {
        boolean result = false;
        DataOutputStream dataOutputStream = null;
        BufferedReader errorStream = null;
        try {
            // 申请su权限
            Process process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            // 执行pm install命令
            String command = "pm install -r " + apkPath + "\n";
            dataOutputStream.write(command.getBytes(Charset.forName("utf-8")));
            dataOutputStream.flush();
            dataOutputStream.writeBytes("exit\n");
           dataOutputStream.flush();
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
            if (!msg.contains("Failure")) {
                result = true;
            }
        } catch (Exception e) {
            Log.e("TAG", e.getMessage(), e);
        //    TestUtil.uploadTest(context,"6，异常原因："+e.getMessage());
        } finally {
            try {
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
                if (errorStream != null) {
                    errorStream.close();
                }
            } catch (IOException e) {
                Log.e("TAG", e.getMessage(), e);
            }
        }
        return result;
    }

    /**
     * 静默卸载
     */
    private static boolean clientUninstall(String packageName){
        PrintWriter PrintWriter = null;
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");
            PrintWriter = new PrintWriter(process.getOutputStream());
            PrintWriter.println("LD_LIBRARY_PATH=/vendor/lib:/system/lib ");
            PrintWriter.println("pm uninstall "+packageName);
            PrintWriter.flush();
            PrintWriter.close();
            int value = process.waitFor();
            return returnResult(value);
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            if(process!=null){
                process.destroy();
            }
        }
        return false;
    }

    /**
     * 启动app
     * com.exmaple.client/.MainActivity
     * com.exmaple.client/com.exmaple.client.MainActivity
     */
    public static boolean startApp(String packageName,String activityName){
        boolean isSuccess = false;
        String cmd = "am start -n " + packageName + "/" + activityName + " \n";
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(cmd);
            int value = process.waitFor();
            return returnResult(value);
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            if(process!=null){
                process.destroy();
            }
        }
        return isSuccess;
    }


    private static boolean returnResult(int value){
        // 代表成功
        if (value == 0) {
            return true;
        } else if (value == 1) { // 失败
            return false;
        } else { // 未知情况
            return false;
        }
    }
}