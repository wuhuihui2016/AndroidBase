package com.whh.androidbase.activity;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.whh.androidbase.R;

/**
 * 各种AndroidX特性使用
 */
public class TestAndroidXActivity extends AppCompatActivity {

    private  TextView tv_txt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_txt = (TextView) findViewById(R.id.tv_txt);

    }

}