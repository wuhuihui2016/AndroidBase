package com.whh.androidbase.utils;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

@RequiresApi(api = Build.VERSION_CODES.O)
public class FileUtils {

    private final static String copy_from = "/sdcard/Pictures/android-studio-ide-4.1.exe";
    private final static String copy_to = "/sdcard/Download/android-studio-ide-4.1.exe";
    public final static File srcFile = new File(copy_from); //文件大小：896.1M
    public final static File destFile = new File(copy_to);
    private static long startTime;
    private static int bufferSizeKB = 4;//also tested for 16, 32, 64, 128, 256 and 1024
    private static int bufferSize = bufferSizeKB * 1024;

    /**
     * 普通IO流拷贝文件
     */
    public static void copyFile() {
        Log.e("whh0608", "copyFile...!");
        try {
            startTime = System.currentTimeMillis();
            InputStream input = new FileInputStream(srcFile);
            OutputStream output = new FileOutputStream(destFile);
            byte[] buf = new byte[bufferSize];
            int bytesRead;
            while ((bytesRead = input.read(buf)) != -1) {
                output.write(buf, 0, bytesRead);
            }
            input.close();
            output.close();
        } catch (Exception e) {
            Log.e("whh0608", "copyFile Exception is " + e.getMessage());
        } finally {
            Log.e("whh0608", "copyFile is finished! "
                    + (System.currentTimeMillis() - startTime) + " ms!");
        }
    }

    /**
     * FileChannel拷贝文件
     */
    public static void copyFile2() {
        Log.e("whh0608", "copyFile2...!");
        try {
            startTime = System.currentTimeMillis();
            //创建文件的输入输出流
            FileInputStream in = new FileInputStream(srcFile);
            FileOutputStream out = new FileOutputStream(destFile);

            //通道
            FileChannel inchan = in.getChannel();
            FileChannel outchan = out.getChannel();

            //复制文件
            ByteBuffer buffer = ByteBuffer.allocate(bufferSize);

            int len = 0;

            while ((len = inchan.read(buffer)) != -1) {
                //切换模式
                buffer.flip();
                //读缓冲区数据,写入到文件中
                outchan.write(buffer);
                //清空
                buffer.clear();
            }
            //关闭资源
            outchan.close();
            inchan.close();
            out.close();
            in.close();
        } catch (Exception e) {
            Log.e("whh0608", "copyFile2 Exception is " + e.getMessage());
        } finally {
            Log.e("whh0608", "copyFile2 is finished! "
                    + (System.currentTimeMillis() - startTime) + " ms!");
        }
    }


    /**
     * 零拷贝文件 FileChannel.transferFrom
     * 对比copyFile2方法的优化
     */
    public static void copyFile3() {
        try {
            Log.e("whh0608", "copyFile3...!");
            startTime = System.currentTimeMillis();
            FileChannel inputChannel = new FileInputStream(srcFile).getChannel();
            FileChannel outputChannel = new FileOutputStream(destFile).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
            inputChannel.close();
            outputChannel.close();
        } catch (Exception e) {
            Log.e("whh0608", "copyFile3 Exception is " + e.getMessage());
        } finally {
            Log.e("whh0608", "copyFile3 is finished! "
                    + (System.currentTimeMillis() - startTime) + " ms!");
        }
    }

    /**
     * 零拷贝文件 FileChannel.transferTo
     * 对比copyFile2方法的优化
     */
    public static void copyFile4() {
        try {
            Log.e("whh0608", "copyFile4...!");
            startTime = System.currentTimeMillis();
            FileChannel inputChannel = new FileInputStream(srcFile).getChannel();
            FileChannel outputChannel = new FileOutputStream(destFile).getChannel();
            inputChannel.transferTo(0, inputChannel.size(), outputChannel);
            inputChannel.close();
            outputChannel.close();
        } catch (Exception e) {
            Log.e("whh0608", "copyFile4 Exception is " + e.getMessage());
        } finally {
            Log.e("whh0608", "copyFile4 is finished! "
                    + (System.currentTimeMillis() - startTime) + " ms!");
        }
    }
    //TODO　FileChannel方法使用解析
    //如果需要在FileChannel的某个特定位置进行数据的读/写操作。
        //可以通过调用position()方法获取FileChannel的当前位置，如果该方法返回-1，则文件已结束。
        //也可以通过调用position(long pos)方法设置FileChannel的当前位置
    //channel.size()方法将返回该实例所关联文件的大小。
    //channel.truncate()截取一个文件。截取文件时，文件将中指定长度后面的部分将被删除。如：channel.truncate(1024); //截取文件的前1024个字节
    //channel.force()方法有一个boolean类型的参数，指明是否同时将文件元数据（权限信息等）写到磁盘上。参数为true则同时将文件数据和元数据强制写到磁盘上
    //channel.force(false); //将数据刷出到磁盘，但不包括元数据
    //channel.transferFrom(); 与 channel.transferTo(); 在方法参数上有差异，但作用一致，速度相当，都是实现文件的零拷贝
    //TODO　FileChannel方法使用解析END

    //TODO 结论：各种拷贝文件的方法在速率比较，FileChannel零拷贝方法最快；因此在做大文件拷贝时使用FileChannel零拷贝

    /**
     * 删除已存在的目标文件
     */
    public static void delDestFile() {
        if (destFile.exists()) {
            if (destFile.delete()) {
                Log.e("whh0608", "delete destFile is finished!");
            }
        }
    }
}