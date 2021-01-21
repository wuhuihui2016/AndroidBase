package com.whh.androidbase.activity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e("whh0114", "postDelayed......");
            }
        }, 800000L);
    }

    @Override
    public void onClick(View view) {
        finish();
    }

}