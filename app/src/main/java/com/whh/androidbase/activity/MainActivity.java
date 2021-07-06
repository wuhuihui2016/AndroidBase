package com.whh.androidbase.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.whh.androidbase.MyApp;
import com.whh.androidbase.R;
import com.whh.androidbase.utils.FileUtils;
import com.whh.test.TestVersion;
import com.whh.testjar.MainMethod;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView tv_gradle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //获取当前gradle配置的信息
        tv_gradle = (TextView) findViewById(R.id.tv_gradle);
        tv_gradle.setText(TestVersion.getVersion() + " [CURFLAVOR]" + MyApp.CURFLAVOR
                + "\n" + MainMethod.getTitile());
    }

    /**
     * 多任务页面跳转
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this, TestANRActivity.class);
        //在近期任务列表显示单个APP的多个Activity：参考文章链接：https://www.jianshu.com/p/7f6f98da691e
        //方法一：单个APP跳转页面后，在最新任务中增加显示跳转的页面，用以下配置，在跳转的页面(TestANRActivity)关闭时finishAndRemoveTask();
        //【方法步骤详见<DOC\Android\Android 实战技巧.md#十七>】
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT); //此标志用于将文档打开到一个基于此意图的新任务中；
        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK); //此标志用于创建新任务并将活动导入其中
        startActivity(intent);
    }

    /**
     * 文件拷问方法
     *
     * @param view
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void copy(View view) {

        FileUtils.delDestFile(); //拷贝前删除已存在的目标文件
        FileUtils.copyFile(); //普通IO流拷贝文件
        //TODO　copyFile is finished! 40345 ms!

        FileUtils.delDestFile();
        FileUtils.copyFile2(); //FileChannel.read拷贝文件
        //TODO　copyFile2 is finished! 39838 ms!

        FileUtils.delDestFile();
        FileUtils.copyFile3(); //FileChannel.transferFrom拷贝文件
        //TODO　copyFile3 is finished! 10947 ms!

        FileUtils.delDestFile();
        FileUtils.copyFile4(); //FileChannel.transferTo拷贝文件
        //TODO　copyFile4 is finished! 8951 ms!

    }

    /**
     * handler 使用
     *
     * @param view
     */

    private Handler handler = new Handler();

    public void handler(View view) {
        if (handler != null) {
            Log.e("whh0609", "00000,,,start post");
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.e("whh0609", "11111,,,postDelayed 10000");
                }
            }, 1000 * 10);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.e("whh0609", "11111,,,postDelayed 5000");
                }
            }, 1000 * 5);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.e("whh0609", "11111,,,postDelayed 2000 in Thread: " +
                            Thread.currentThread().getName()); //在mainThread
                }
            }, 1000 * 2);
            handler.postAtTime(new Runnable() {
                @Override
                public void run() {
                    Log.e("whh0609", "11111,,,postAtTime 2000"); //貌似和postDelayed 2000没区别啊
                }
            }, 1000 * 2);
        }

        new Thread() {
            @Override
            public void run() {
                super.run();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("whh0609", "22222,,,postDelayed 2000 in Thread: " +
                                Thread.currentThread().getName()); //在mainThread
                    }
                }, 2000);
            }
        }.start();
        //TODO　执行顺序：2-5-10　
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            //Looper.myLooper().quitSafely(); // IllegalStateException:Main thread not allowed to quit.
            handler = null;
        }
    }
}