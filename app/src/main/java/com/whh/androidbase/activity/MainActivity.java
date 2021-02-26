package com.whh.androidbase.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.whh.androidbase.MyApp;
import com.whh.androidbase.R;
import com.whh.test.TestVersion;
import com.whh.testjar.MainMethod;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tv_gradle = (TextView) findViewById(R.id.tv_gradle);
        tv_gradle.setText(TestVersion.getVersion() + " [CURFLAVOR]"  + MyApp.CURFLAVOR
                + "\n" + MainMethod.getTitile());
    }

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

}