package com.whh.androidbase.utils;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

/**
 * HandlerThread
 * 用来启动具有一个内部循环的新线程的一个便利类
 *
 * 本质上是一个线程类，它继承了Thread。
 *    HandlerThread有自己的内部Looper对象，可以进行loopr循环。通过获取HandlerThread的looper对象传递给Handler对象，可以在handleMessage()方法中执行异步任务。
 *    创建HandlerThread后必须先调用HandlerThread.start()方法，Thread会先调用run方法，创建Looper对象。
 *    当有耗时任务进入队列时，则不需要开启新线程，在原有的线程中执行耗时任务即可，否则线程阻塞。
 *    它在Android中的一个具体的使用场景是IntentService。
 *    由于HanlderThread的run()方法是一个无限循环，因此当明确不需要再使用HandlerThread时，可以通过它的quit或者quitSafely方法来终止线程的执行。
 *
 * HanlderThread 的优缺点
 *    HandlerThread 优点是异步不会堵塞，减少对性能的消耗。
 *    HandlerThread 缺点是不能同时继续进行多任务处理，要等待进行处理，处理效率较低。
 *    HandlerThread 与线程池不同，HandlerThread是一个串队列，背后只有一个线程。
 *
 * 模拟线程下载
 * 1、activiy 初始化变量 uiHandler，作为 myHandlerThread 的 uiHandler；
 * 2、activiy 启动 myHandlerThread，调用 myHandlerThread 内部方法 onLooperPrepared；
 *      在 onLooperPrepared 中，初始化 workHandler，并通知 uiHandler 开始执行任务；
 * 3、uiHandler 接收到通知，发送下载任务消息给 myHandlerThread：遍历任务队列，将任务队列加入到 MessageQueue；
 *     每个任务的执行，给 workHandler 传输下载连接和 STAR 标志；
 * 4、myHandlerThread 的 workHandler 接收到通知后，开始下载任务，并将下载个数+1，当下载个数和所需下载个数一致时，执行 STOP，quitSafely。同样下载出错的时也在这里处理
 */
public class MyHandlerThread extends HandlerThread implements Handler.Callback {

    private Handler workHandler; //处理下载逻辑的Handler
    private Handler uiHandler; //处理UI刷新的Handler

    public final static int READY = 0X110;
    public final static int STAR = 0X111;
    public final static int STOP = 0X112;
    public final static int ERROR = 0X113;

    private int max;
    private int index;

    public MyHandlerThread() {
        super("download-thread");
    }

    @Override
    protected void onLooperPrepared() {
        Log.i("whh0720", "download-thread onLooperPrepared...");
        workHandler = new Handler(getLooper(), this);
        if (uiHandler == null) {
            throw new NullPointerException("uiHandler is null");
        }
        uiHandler.sendEmptyMessage(READY); //通知主线程Looper已经准备完毕，可以开始下载了
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case STAR: //开始下载
                Log.e("whh0720", "MyHandlerThread.STAR...");
                index++; //记录当前下载到了第几个
                String url = msg.getData().getString("url");
                Log.i("whh0720", "准备下载：" + url);
                try {
                    sleep(2000); //模拟下载
                    if (index == 2) {  //下载到第二个的时候模拟下载失败
                        Log.i("whh0720", "下载出错：" + url);
                        setErrorMessage(url);
                        Log.i("whh0720", "开始下载下一个地址");
                    } else {
                        Log.i("whh0720", "当前地址下载完成：" + url);
                    }
                } catch (InterruptedException e) {
                    setErrorMessage(url);
                    e.printStackTrace();
                }
                if (index == max) { //全部下载完毕
                    workHandler.sendEmptyMessage(STOP);
                }
                break;
            case STOP: //全部下载完成
                //退出Looper循环
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    quitSafely();
                } else {
                    quit();
                }
                Log.i("whh0720", "全部下载完成");
                break;
        }
        return false;
    }

    //发送下载错误消息到主线程用于刷新UI
    private void setErrorMessage(String url) {
        Message errorMsg = new Message();
        errorMsg.what = ERROR;
        Bundle bundle = new Bundle();
        bundle.putString("errorUrl", url);
        errorMsg.setData(bundle);
        uiHandler.sendMessage(errorMsg);
    }

    public void setUiHandler(Handler uiHandler) {
        this.uiHandler = uiHandler;
    }

    public Handler getWorkHandler() {
        return workHandler;
    }

    public void setMax(int max) {
        this.max = max;
    }
}
