package com.whh.androidbase.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.tencent.mmkv.MMKV;
import com.whh.androidbase.MyApp;

/**
 * MMKV 是基于 mmap 内存映射的 key-value 组件，底层序列化/反序列化使用 protobuf 实现，性能高，稳定性强。
 *
 * 替代SharedPreferences理由：
 *    1、SharedPreferences效率低；每次写入为全量更新，commit、apply、getXXX()引起ANR问题
 *    2、MMKV增量更新，数据序列化方面选用 protobuf 协议，pb 在性能和空间占用上性能更优，不必担心 crash 导致数据丢失
 *
 * MMKV 原理
 * 内存准备:
 *     通过 mmap 内存映射文件，提供一段可供随时写入的内存块，App 只管往里面写数据，由操作系统负责将内存回写到文件，不必担心 crash 导致数据丢失。
 *     MMAP优势
 *        对文件的读写操作只需要从磁盘到用户主存的一次数据拷贝过程，减少了数据的拷贝次数，提高了文件读写效率。
 *        使用逻辑内存对磁盘文件进行映射，操作内存就相当于操作文件，不需要开启线程，操作MMAP的速度和操作内存的速度一样快；
 *        提供一段可供随时写入的内存块，App 只管往里面写数据，由操作系统如内存不足、进程退出等时候负责将内存回写到文件，不必担心 crash 导致数据丢失。
 * 数据组织:
 *     MMKV基于protobuf协议进行数据存储，存储方式为增量更新，也就是不需要每次修改数据都要重新将所有数据写入文件了。
 *     protobuf 是google开源的一个序列化框架，类似xml，json，最大的特点是基于二进制，比传统的XML表示同样一段内容要短小得多。
 *     MMKV使用crc 校验确保数据有效性
 * 写入优化:
 *    考虑到主要使用场景是频繁地进行写入更新，需要有增量更新的能力。将增量 kv 对象序列化后，append 到内存末尾。
 * 空间增长:
 *    使用 append 实现增量更新带来了一个新的问题，就是不断 append 的话，文件大小会增长得不可控。需要在性能和空间上做个折中
 *
 * MMKV写入读取工具
 * 所有变更立马生效，无需调用 sync、apply
 * 教程：https://github.com/Tencent/MMKV/wiki/android_tutorial、https://zhuanlan.zhihu.com/p/47420264(中文)
 * 支持的数据类型：boolean, int, long, float, double, byte[],
 *     String, Set<String>, Any class that implements Parcelable
 *
 * 源码详解：https://blog.csdn.net/qq_22090073/article/details/103703291
 *
 * 【扩展】谷歌推出的Jetpack DataStore 也是用来替代SharedPreferences，但是应用于kotlin
 */
public class MMKVUtils {

    public static MMKV mmkv;

    /**
     * 初始化
     * @param context
     */
    public static void init(Context context) {
        String rootDir = MMKV.initialize(context); //默认存储路径：data/data/com.whh.androidbase/files/mmkv
        Log.e("whh0527", "initialize rootDir is " + rootDir);
//        //也可以自定义存储路径，比如：
//        //String dir = getFilesDir().getAbsolutePath() + "/mmkv_2";
//        //String rootDir = MMKV.initialize(dir);
//        //Log.i("MMKV", "mmkv root: " + rootDir);
//
        mmkv = MMKV.defaultMMKV();
        //自定义实例路径
//        String relativePath = "data/data/com.whh.androidbase/files/whh-mmkv";
//        mmkv = MMKV.mmkvWithID("whh-mmkv", relativePath);

    }

    /**
     * 存入键值对
     * @param key
     * @param value
     */
    public static void setValue(String key, Object value) {

        if (value instanceof String) {
            mmkv.encode(key, (String) value);
        } else if (value instanceof Integer) {
            mmkv.encode(key, (Integer) value);
        } else if (value instanceof Boolean) {
            mmkv.encode(key, (Boolean) value);
        } else if (value instanceof Float) {
            mmkv.encode(key, (Float) value);
        } else if (value instanceof Long) {
            mmkv.encode(key, (Long) value);
        } else {
            mmkv.encode(key, value.toString());
        }
    }

    /**
     * 读取键值对的值
     * @param key
     * @param defaultValue
     * @return
     */
    public static Object getValue(String key, Object defaultValue) {
        if (defaultValue instanceof String) {
            return mmkv.decodeString(key, (String) defaultValue);
        } else if (defaultValue instanceof Integer) {
            return mmkv.decodeInt(key, (Integer) defaultValue);
        } else if (defaultValue instanceof Boolean) {
            return mmkv.decodeBool(key, (Boolean) defaultValue);
        } else if (defaultValue instanceof Float) {
            return mmkv.decodeFloat(key, (Float) defaultValue);
        } else if (defaultValue instanceof Long) {
            return mmkv.decodeLong(key, (Long) defaultValue);
        }
        return null;
    }

    /**
     * 移除key
     * @param key
     * @return
     */
    public static boolean removeKey(String key){
        mmkv.remove(key);
        if (mmkv.contains(key)) return false;
        return true;
    }

    /**
     * SharedPreferences 迁移
     * @param spSpace 需要迁移的sp空间
     */
    public static void importSP(String spSpace) {
        mmkv = MMKV.mmkvWithID(spSpace);
        SharedPreferences old_data = MyApp.context.getSharedPreferences(spSpace, Context.MODE_PRIVATE);
        mmkv.importFromSharedPreferences(old_data);
        old_data.edit().clear().commit(); //迁移完成后清除sp
    }
}
