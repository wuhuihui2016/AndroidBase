package com.whh.androidbase.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.whh.androidbase.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 各种集合类的示例
 */
public class TestCollectionsActivity extends AppCompatActivity {

    private TextView tv_txt;
    private List<Integer> nums = new ArrayList<>();
    private List<Integer> newNums = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_txt = (TextView) findViewById(R.id.tv_txt);
        initNums();

        //排序：Collections支持三种排序
        //A.sort()默认排序(从小到大)、B.reverse()倒序(从大到小)、C.shuffle()随机排序
    }

    private void initNums() {
        nums.add(1);
        nums.add(2);
        nums.add(3);
        nums.add(4);
        nums.add(5);
        nums.add(2);
        nums.add(3);
        nums.add(6);
        nums.add(5);
        nums.add(7);

        removeDupl(); //去重
    }

    /**
     * 去重
     */
    private void removeDupl() {

        List newList = new  ArrayList();
        String items = "";
        long startTime = System.currentTimeMillis();

        //方法一 利用HashSet去重
        Set set = new HashSet(nums);
        newList.addAll(set);
        for (int i = 0; i < newList.size(); i++) {
            items += newList.get(i) + "、";
        }
        //newList = new ArrayList<>(new HashSet<Integer>(nums));//去重后的集合
        Log.e("whh0318", "removeDupl HashSet items" + items + ", spend time: " + (System.currentTimeMillis() - startTime)); // 1

        //方法二 通过List的contains()方法去重
        newList.clear();
        startTime = System.currentTimeMillis();
        for (int i = 0; i < nums.size(); i++) {
            if (!newList.contains(nums.get(i))) {
                // 将未包含的元素添加进newList集合中
                newList.add(nums.get(i));
            }
        }
        items = "";
        for (int i = 0; i < newList.size(); i++) {
            items += newList.get(i) + "、";
        }
        Log.e("whh0318", "removeDupl contains() items" + items + ", spend time: " + (System.currentTimeMillis() - startTime)); //0

        //方法三 循环List进行去重
        newList.clear();
        newList.addAll(nums);
        startTime = System.currentTimeMillis();
        for (int i = 0; i < newList.size() - 1; i++) {
            for (int j = newList.size() - 1; j > i; j--) {
                // 进行比较
                if (newList.get(j).equals(newList.get(i))) {
                    newList.remove(j);
                }
            }
        }
        items = "";
        for (int i = 0; i < newList.size(); i++) {
            items += newList.get(i) + "、";
        }
        Log.e("whh0318", "removeDupl equals items" + items + ", spend time: " + (System.currentTimeMillis() - startTime)); //0
    }

}