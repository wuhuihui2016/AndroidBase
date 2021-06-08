package com.whh.androidbase;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.whh.androidbase.utils.MMKVUtils;

public class MyApp extends Application {

    public static Context context;

    public static String CURFLAVOR = BuildConfig.FLAVOR; //当前编译的产品

    @Override
    public void onCreate() {
        super.onCreate();

        context = this;
        Log.e("whh0527", "MyApp in onCreate...");

        MMKVUtils.init(this);
        MMKVUtils.setValue("name", "555");
        MMKVUtils.setValue("age", 5);
        MMKVUtils.setValue("isYouth", 1.23);

        Log.e("whh0527", "getValue==>name=" + MMKVUtils.getValue("name", "")
                + "; age=" + MMKVUtils.getValue("age", 0)
                + "; isYouth=" + MMKVUtils.getValue("isYouth", false));

        Log.e("whh0527", "isYouth contains?" + MMKVUtils.mmkv.contains("isYouth"));
        MMKVUtils.removeKey("isYouth");
        Log.e("whh0527", "After remove...isYouth contains?" + MMKVUtils.mmkv.contains("isYouth"));

        testImportSPData();
    }

    /**
     * 测试 Sp 数据迁移到 MMKV
     */
    private void testImportSPData(){
        String spSpace = "mySpData";
        SharedPreferences.Editor editor = getSharedPreferences(spSpace, MODE_PRIVATE).edit();
        editor.putString("nameSp", "whh");
        editor.putInt("ageSp", 18);
        editor.putBoolean("isYouthSp", true);
        editor.commit();

        MMKVUtils.importSP(spSpace);

        Log.e("whh0527", "importSP...getValue==>"
                + "nameSp=" + MMKVUtils.getValue("nameSp", "")
                + "; ageSp=" + MMKVUtils.getValue("ageSp", 0)
                + "; isYouthSp=" + MMKVUtils.getValue("isYouthSp", false));

    }
}
