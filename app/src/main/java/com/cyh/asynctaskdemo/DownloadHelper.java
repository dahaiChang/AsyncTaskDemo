package com.cyh.asynctaskdemo;

import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * AsyncTask封装
 *   1. download方法  url localPath listener
 * 2. listener: start, success fail progress.
 * 3. 用asynctask封装的
 */
public class DownloadHelper {
    public static void download(String url,String localPath,OnDownloadListener listener){
        MyAsyncTask myAsyncTask = new MyAsyncTask(url,localPath,listener);
        myAsyncTask.execute();
    }

    public interface OnDownloadListener{
        void onStart();
        void onSuccess(int code, File file);
        void onFail(int code, File file,String message);
        void onProgress(int progresss);

        abstract class SimpleOnDownloadListener implements OnDownloadListener{
            @Override
            public void onStart() {

            }

            @Override
            public void onProgress(int progresss) {

            }
        }
    }

    /**
     * String  入参
     * Integer 进度
     * Boolean 返回值
     */
    public static class  MyAsyncTask extends AsyncTask<String , Integer, Boolean> {

        String mUrl;
        String mLocalPath;
        OnDownloadListener mListener;

        public MyAsyncTask(String mUrl, String mLocalPath, OnDownloadListener mListener) {
            this.mUrl = mUrl;
            this.mLocalPath = mLocalPath;
            this.mListener = mListener;
        }

        /**
         * 在异步任务之前，在主线程中
         * // 可操作UI  类似淘米,之前的准备工作
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mListener != null) {
                mListener.onStart();
            }
        }

        /**
         * 在另外一个线程中处理事件
         *
         * @param strings 入参  煮米
         * @return Boolean 结果
         */
        @Override
        protected Boolean doInBackground(String... strings) {//可变参数 String... strings  代表String 数组，可能有1个或者多个String，String长度不确定。
            String apkUrl = mUrl;

            try {
                //构造URL
                URL url = new URL(apkUrl);
                //构造连接，并打开
                URLConnection urlConnection = url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();

                //获取下载内容总长度
                int contentLength = urlConnection.getContentLength();

                //对下载地址进行处理
                File apkFile = new File(mLocalPath);
                if (apkFile.exists()) {
                    boolean result = apkFile.delete();
                    if (!result) {
                        if (mListener != null){
                            mListener.onFail(-1,apkFile,"文件删除失败");
                        }
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
                OutputStream outputStream = new FileOutputStream(mLocalPath);
                //不断的一车一车挖土，走到挖不到为止
                while ((length = inputStream.read(bytes)) != -1) {
                    //挖到的 放到我们的文件管道里
                    outputStream.write(bytes, 0, length);
                    //累加文件的大小
                    downLoadSize += length;
                    //发送进度
                    publishProgress(downLoadSize * 100 / contentLength);
                }

                //关闭流，否则容易内存泄露
                inputStream.close();
                outputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
                if (mListener != null){
                    mListener.onFail(-2,new File(mLocalPath),"文件IO异常");
                }
                return false;
            }

            if (mListener != null){
                mListener.onSuccess(0,new File(mLocalPath));
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
        }

        /**
         * 收到进度，然后处理： 也是在UI线程中。
         * @param values
         */
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            // 收到进度，然后处理： 也是在UI线程中。
            if (values != null && values.length > 0) {
                if(mListener != null){
                    mListener.onProgress(values[0]);
                }
            }
        }
    }
}