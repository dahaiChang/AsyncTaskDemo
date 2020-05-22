package com.cyh.asynctaskdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * 1. 网络上请求数据: 申请网络权限 读写存储权限
 * 2. 布局我们的layout
 * 3. 下载之前我们要做什么?  UI
 * 4. 下载中我们要做什么?   数据
 * 5. 下载后我们要做什么?  UI
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //读写权限
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    //请求状态码
    private static int REQUEST_PERMISSION_CODE = 1;

    public static final int INIT_PROGRESS = 0;
    private ProgressBar progressBar;
    private Button button;
    private TextView textView;

    private static final String APK_URL = "http://download.sj.qq.com/upload/connAssitantDownload/upload/MobileAssistant_1.apk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();

        //权限申请
        checkCustomPermission();
    }

    private void checkCustomPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                Log.i("MainActivity", "申请的权限为：" + permissions[i] + ",申请结果：" + grantResults[i]);
            }
        }
    }



    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2020-05-21 11:22:00 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {
        progressBar = (ProgressBar)findViewById( R.id.progressBar );
        button = (Button)findViewById( R.id.button );
        textView = (TextView)findViewById( R.id.textView );

        button.setOnClickListener( this );
    }

    /**
     * Handle button click events<br />
     * <br />
     * Auto-created on 2020-05-21 11:22:00 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    @Override
    public void onClick(View v) {
        if ( v == button ) {
            // Handle clicks for button
            //1.使用默认开始下载
//            new MyAsyncTask().execute(APK_URL);
            //2.使用封装类下载
            //下载地址准备
            String filePath = Environment.getExternalStorageDirectory() + File.separator + "test.apk";
            DownloadHelper.download(APK_URL, filePath, new DownloadHelper.OnDownloadListener.SimpleOnDownloadListener() {
                @Override
                public void onStart() {
                    super.onStart();
                    textView.setText("下载中");
                    button.setText("下载中");
                }
                @Override
                public void onProgress(int progresss) {
                    super.onProgress(progresss);
                    progressBar.setProgress(progresss);
                }
                @Override
                public void onSuccess(int code, File file) {
                    textView.setText("下载完成");
                    button.setText("下载完成");
                }
                @Override
                public void onFail(int code, File file, String message) {
                    textView.setText(message);
                    button.setText(message);
                }
            });
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    /**
     * String  入参
     * Integer 进度
     * Boolean 返回值
     */
    public class  MyAsyncTask extends AsyncTask<String , Integer, Boolean>{

        private String filePath;
        /**
         * 在异步任务之前，在主线程中
         * // 可操作UI  类似淘米,之前的准备工作
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            textView.setText(R.string.startdown);
            button.setText(R.string.startdown);
            progressBar.setProgress(INIT_PROGRESS);
        }

        /**
         * 在另外一个线程中处理事件
         * @param strings  入参  煮米
         * @return  Boolean 结果
         */
        @Override
        protected Boolean doInBackground(String... strings) {//可变参数 String... strings  代表String 数组，可能有1个或者多个String，String长度不确定。
            //先对传入参数进行判空
            if (strings != null && strings.length > 0){
                String apkUrl = strings[0];

                try {
                    //构造URL
                    URL url = new URL(apkUrl);
                    //构造连接，并打开
                    URLConnection urlConnection = url.openConnection();
                    InputStream inputStream = urlConnection.getInputStream();

                    //获取下载内容总长度
                    int contentLength = urlConnection.getContentLength();

                    //下载地址准备
                    filePath = Environment.getExternalStorageDirectory() + File.separator + "test.apk";
                    //对下载地址进行处理
                    File apkFile = new File(filePath);
                    if (apkFile.exists()){
                        boolean result = apkFile.delete();
                        if (!result){
                            return false;
                        }
                    }

                    //已下载的大小
                    int downLoadSize = 0;
                    //byte数组 挖土车
                    byte[] bytes = new byte[1024];
                    //
                    int length;
                    //创建一个输入管道
                    OutputStream outputStream = new FileOutputStream(filePath);
                    //不断的一车一车挖土，走到挖不到为止
                    while ((length = inputStream.read(bytes))!= -1){
                        //挖到的 放到我们的文件管道里
                        outputStream.write(bytes,0,length);
                        //累加文件的大小
                        downLoadSize += length;
                        //发送进度
                        publishProgress(downLoadSize*100/contentLength);
                    }

                    //关闭流，否则容易内存泄露
                    inputStream.close();
                    outputStream.close();

                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }else {
                return false;
            }
            return true;
        }


        /**
         * 也是在主线程中 ，执行结果 处理
         * @param aBoolean
         */
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            button.setText(aBoolean ? "下载完成" : "下载失败");
            textView.setText(aBoolean ? "下载完成"+ filePath : "下载失败");
        }

        /**
         * 收到进度，然后处理： 也是在UI线程中。
         * @param values
         */
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            //对values进行判空处理
            if (values != null && values.length > 0){
                progressBar.setProgress(values[0]);
            }
        }
    }
}
