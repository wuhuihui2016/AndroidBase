package com.whh.androidbase.activity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.whh.androidbase.R;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 测试ANR
 */
public class TestANRActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Log.e("whh0114", "postDelayed......");
//            }
//        }, 800000L);
    }

    @Override
    public void onClick(View view) {
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        finishAndRemoveTask();
        //如果在APP中关闭，以下情况无需考虑，该任务的页面关闭后会回到上一个页面
        //如果在近期任务列表中打开后关闭了某个任务的activity，会回到桌面，可以按需要在关闭页面时条跳转到其他页面
//        startActivity(new Intent(this, MainActivity.class));
    }
}