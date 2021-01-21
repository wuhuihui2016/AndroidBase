# Android 开源框架
一、LeakCanary 工作机制
       LeakCanary.install() 会返回一个预定义的 RefWatcher，同时也会启用一个 ActivityRefWatcher，用于自动监控调用Activity.onDestroy() 之后泄露的 activity。
        1.RefWatcher.watch() 创建一个 KeyedWeakReference 到要被监控的对象。
        2.然后在后台线程检查引用是否被清除，如果没有，调用GC。
        3.如果引用还是未被清除，把 heap 内存 dump 到 APP 对应的文件系统中的一个 .hprof 文件中。
        4.在另外一个进程中的 HeapAnalyzerService 有一个 HeapAnalyzer 使用HAHA 解析这个文件。
        5.得益于唯一的 reference key, HeapAnalyzer 找到 KeyedWeakReference，定位内存泄露。
        6.HeapAnalyzer 计算 到 GC roots 的最短强引用路径，并确定是否是泄露。如果是的话，建立导致泄露的引用链。
        7.引用链传递到 APP 进程中的 DisplayLeakService， 并以通知的形式展示出来。

二、腾讯 Bugly
  腾讯公司为移动开发者开放的服务之一，面向移动开发者提供专业的 Crash 监控、崩溃分析等质量跟踪服务。Bugly 能帮助移动互联网开发者更及时地发现掌控异常，更全面的了解定位异常，更高效的修复解决异常。
  针对移动应用，腾讯 Bugly 提供了专业的 Crash、Android ANR ( application not response)、iOS 卡顿监控和解决方案。移动开发者 ( Android / iOS ) 可以通过监控，快速发现用户在使用过程中出现的 Crash (崩溃)、Android ANR 和 iOS 卡顿，并根据上报的信息快速定位和解决问题。

三、Glide
  项目依赖Glide，在app build.gradle 中配置 compile'com.github.bumptech.glide:glide:3.7.0'
  使用glide3.7版本，更高版本或出现异常：Error:Failed to resolve: com.android.support:support-annotations:27.0.2

  Glide缓存机制
    内存存缓存的 读存都在Engine类中完成。内存缓存使用弱引用和LruCache结合完成的,弱引用来缓存的是正在使用中的图片。图片封装类Resources内部有个计数器判断是该图片否正在使用。
  Glide内存缓存的流程
    读：是先从lruCache取，取不到再从弱引用中取；
    存：内存缓存取不到，从网络拉取回来先放在弱引用里，渲染图片，图片对象Resources使用计数加一；
    渲染完图片，图片对象Resources使用计数减一，如果计数为0，图片缓存从弱引用中删除，放入lruCache缓存。

三、Fresco
  项目依赖Glide，在app build.gradle 中配置：implementation'com.facebook.fresco:fresco:1.9.0'
  用法参考文章：  https://blog.csdn.net/yw59792649/article/details/78921025





