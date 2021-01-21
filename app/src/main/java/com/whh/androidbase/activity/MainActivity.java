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
        startActivity(new Intent(this, TestANRActivity.class));
    }
}