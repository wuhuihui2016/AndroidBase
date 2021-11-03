# Android 开源框架
一、LeakCanary 工作机制
       LeakCanary.install() 会返回一个预定义的 RefWatcher，同时也会启用一个 ActivityRefWatcher，用于自动监控调用Activity.onDestroy() 之后泄露的 activity。
        1.RefWatcher.watch() 创建一个 KeyedWeakReference 到要被监控的对象。
        2.然后在后台线程检查引用是否被清除，如果没有，调用GC。
        3.如果引用还是未被清除，把 heap 内存 dump 到 APP 对应的文件系统中的一个 .hprof 文件中。
        4.在另外一个进程中的 HeapAnalyzerService 有一个 HeapAnalyzer 使用HAHA 可达性分析来解析这个文件。
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
    内存存缓存的读存都在Engine类中完成。内存缓存使用弱引用和LruCache结合完成的,弱引用来缓存的是正在使用中的图片。图片封装类Resources内部有个计数器判断是该图片否正在使用。
  Glide内存缓存的流程
    读：是先从lruCache取，取不到再从弱引用中取；
    存：内存缓存取不到，从网络拉取回来先放在弱引用里，渲染图片，图片对象Resources使用计数加一；
    渲染完图片，图片对象Resources使用计数减一，如果计数为0，图片缓存从弱引用中删除，放入lruCache缓存。

   Glide原理
    with方法：with有很多重载方法，接收参数包括Context、Actitity、FragmentActivity、Fragment等，with方法内部主要处理生命周期相关，
        并返回RequestManager，我们重点分析生命周期：对于ApplicationContext，直接声明ApplicationLifecycle，并把RequestManager加入到Lifecycle的listener中，
        其实ApplicationLifeCycle什么都没做，甚至没有存储listeners。对于Activity和Fragment，Glide自动创建了一个Fragment，并把Fragment加入到了Activity中，
        并把RequestManager加入到Fragment控制的LifeCycle中，因为Fragment的生命周期和Activity是同步的，如果Activity被销毁了，Fragment是可以监听到的，
        这样Glide就可以捕获这个事件并停止图片加载了
    load方法：该方法返回一个DrawableTypeRequest对象，父类是GenericRequestBuilder，里面包括Glide绝大多数API，比如说placeholder、error、diskCacheStrategy等
    into方法：主要处理流程都在into中，我们重点分析缓存策略：首先，生成缓存Key的参数有10多个（包括id、width、height等）。
        在内存缓存策略中，我们通过弱引用来缓存正在使用的图片，通过LruCache(Least Recently Used)来缓存不在使用的图片。
        在硬盘缓存策略中，缓存分为两级，首先我们通过key值访问转换后的结果(RESULT)，如果不存在，再通过简化的key值访问原始图片(SOURCE)

  【引申】开源框架：ImageLoader、Glide（google）、Fresco（FaceBook）、Picasso（Square）
      Picasso包体积小、加载效率低、清晰，但功能有局限不能加载gif、只能缓存全尺寸（100K）；
      Glide功能全面，擅长大型图片流，支持生命周期管理、自动裁剪图片、三级缓存、Gif图加载、体积适中（500K）
           多种图片格式的缓存，适用于更多的内容表现形式（如Gif、WebP、缩略图、Video）
           生命周期集成（根据Activity或者Fragment的生命周期管理图片加载请求）
           高效处理Bitmap（bitmap的复用和主动回收，减少系统回收压力）
           高效的缓存策略，灵活（Picasso只会缓存原始尺寸的图片，Glide缓存的是多种规格），加载速度快且内存开销小（默认Bitmap格式的不同，使得内存开销是Picasso的一半）
      Fresco内存优化，减少oom，体积更大。
      
  提问：[https://www.jianshu.com/p/cf06a3f8e4d3]
     1、图片加载总体流程？
        1.封装参数：从指定来源，到输出结果，中间可能经历很多流程，所以第一件事就是封装参数，这些参数会贯穿整个过程；
        2.解析路径：图片的来源有多种，格式也不尽相同，需要规范化；
        3.读取缓存：为了减少计算，通常都会做缓存；同样的请求，从缓存中取图片（Bitmap）即可；
        4.查找文件/下载文件：如果是本地的文件，直接解码即可；如果是网络图片，需要先下载；
        5.解码：这一步是整个过程中最复杂的步骤之一，有不少细节；
        6.变换：解码出Bitmap之后，可能还需要做一些变换处理（圆角，滤镜等）；
        7.缓存：得到最终bitmap之后，可以缓存起来，以便下次请求时直接取结果；
        8.显示：显示结果，可能需要做些动画（淡入动画，crossFade等）。
     2、缓存机制？
        Glide的缓存机制，主要分为2种缓存，一种是内存缓存，一种是磁盘缓存。
        使用内存缓存的原因：防止应用重复将图片读入到内存，造成内存资源浪费。setMemoryCache
        使用磁盘缓存的原因：防止应用重复的从网络或者其他地方下载和读取数据。setDiskCache
          GlideApp.with(context)
          .load(url)
          .diskCacheStrategy(DiskCacheStrategy.ALL)
          .dontAnimate()
          .centerCrop()
          .into(imageView);
        /*默认的策略是DiskCacheStrategy.AUTOMATIC
        DiskCacheStrategy有五个常量：
        DiskCacheStrategy.ALL 使用DATA和RESOURCE缓存远程数据，仅使用RESOURCE来缓存本地数据。
        DiskCacheStrategy.NONE 不使用磁盘缓存
        DiskCacheStrategy.DATA 在资源解码前就将原始数据写入磁盘缓存
        DiskCacheStrategy.RESOURCE 在资源解码后将数据写入磁盘缓存，即经过缩放等转换后的图片资源。
        DiskCacheStrategy.AUTOMATIC 根据原始图片数据和资源编码策略来自动选择磁盘缓存策略。*/
        正是因为有着这两种缓存的结合，才构成了Glide极佳的缓存效果。
        GlideApp.with(context)
        .load(url)
        .skipMemoryCache(true)//跳过缓存，默认为false 
        .dontAnimate()
        .centerCrop()
        .into(imageView);
        //磁盘缓存清理（子线程）
        GlideApp.get(context).clearDiskCache();
        //内存缓存清理（主线程）
        GlideApp.get(context).clearMemory();
     3、Glide内存缓存加载的流程
       1.首先去获取活动缓存，如果加载到则直接返回，没有则进入下一步
       2.接着去获取LRU缓存，在获取时会将其从LRU中删除并添加到活动缓存中
       3.下次加载就可以直接加载活动缓存了
       4.当图片引用为0时，会从活动缓存中清除并添加到LRU缓存中
       5.之所以要设计两种内存缓存的原因是为了防止加载中的图片被LRU回收
      4、Glide做了哪些内存优化？
        尺寸优化；图片格式优化；内存复用优化
      5、Glide如何管理生命周期?
        Glide.with(this)绑定了Activity的生命周期。在Activity内新建了一个无UI的Fragment，这个Fragment持有一个Lifecycle，
        通过Lifecycle在Fragment关键生命周期通知RequestManager进行相关从操作。
        在生命周期onStart时继续加载，onStop时暂停加载，onDestory时停止加载任务和清除操作。
      6、Glide怎么做大图加载？
         通过BitmapRegionDecoder来实现，滑动时内存抖动，卡顿现象比较明显，不能用于线上；
         通过SubsamplingScaleImageView来实现，将大图切片，再判断是否可见，如果可见则加入内存中，否则回收，
         减少了内存占用与抖动 同时根据不同的缩放比例选择合适的采样率，进一步减少内存占用，同时在子线程进行decodeRegion操作，
         解码成功后回调至主线程，减少UI卡顿。
         
四、Fresco
  项目依赖Glide，在app build.gradle 中配置：implementation'com.facebook.fresco:fresco:1.9.0'
  用法参考文章：  https://blog.csdn.net/yw59792649/article/details/78921025
  最大的优势在于5.0以下(最低2.3)的bitmap加载。在5.0以下系统，Fresco将图片放到一个特别的内存区域(Ashmem区)
  大大减少OOM（在更底层的Native层对OOM进行处理，图片将不再占用App的内存），适用于需要高性能加载大量图片的场景

五、Eventbus【https://www.jianshu.com/p/5ad5ea7180a2】
  EventBus是一种用于Android的事件发布-订阅总线，由GreenRobot开发。
  Gihub地址 :https://github.com/greenrobot/EventBus。
  EventBus能够简化各组件间的通信，让我们的代码书写变得简单，能有效的分离事件发送方和接收方(也就是解耦的意思)，能避免复杂和容易出错的依赖性和生命周期问题。EventBus可以代替Android传统的Intent，Handler，Broadcast或接口函数，在Fragment、Activity、Service线程之间传递数据，执行方法。
  特点：代码简洁，是一种发布订阅设计模式(观察者设计模式)。

  三大角色：
  Event：事件，可以是任意类型，EventBus会根据事件类型进行全局的通知。
  Subscriber：事件订阅者，在EventBus 3.0之前我们必须定义以onEvent开头的那几个方法，分别是onEvent、onEventMainThread、onEventBackgroundThread和onEventAsync，而在3.0之后事件处理的方法名可以随意取，不过需要加上注解@subscribe，并且指定线程模型，默认是POSTING。
  Publisher：事件的发布者，可以在任意线程里发布事件。一般情况下，使用EventBus.getDefault()就可以得到一个EventBus对象，然后再调用post(Object)方法即可。

  四个线程模型
  ①POSTING (默认) 表示事件处理函数的线程跟发布事件的线程在同一个线程。
  ②MAIN 表示事件处理函数的线程在主线程(UI)线程，因此在这里不能进行耗时操作。
  ③BACKGROUND 表示事件处理函数的线程在后台线程，因此不能进行UI操作。如果发布事件的线程是主线程(UI线程)，那么事件处理函数将会开启一个后台线程，如果果发布事件的线程是在后台线程，那么事件处理函数就使用该线程。
  ④ASYNC 表示无论事件发布的线程是哪一个，事件处理函数始终会新建一个子线程运行，同样不能进行UI操作。
  
六、BlockCanary(https://blog.csdn.net/cpcpcp123/article/details/106983922)
   implementation 'com.github.markzhai:blockcanary-android:1.5.0'
   Application注册：BlockCanary.install(this, new AppBlockContext()).start();
   原理：借助Handler Looper不断轮询消息的机制，queue.next方法获取消息队列中的消息，然后计算出调用dispatchMessage方法的前后时间值（T1,T2），
         T2减去T1的时间差来判断是否超过之前设定好的阈值，如果超过了就dump出收集的信息，来定位UI卡顿的原因。
         在Looper的loop方法中，有一个Printer，它在每个Message处理的前后被调用，而如果主线程卡住了，
         就是 dispatchMessage里卡住了，里面实现的功能就是不断地从 MessageQueue 里面取出 Message 对象，并加以执行。
         在 dispatchMessage 的前后，分别有两个 log 的输出事件，而 dispatchMessage 就是线程上的一次消息处理。如果两次消息处理事件，
         都超过了 16.67ms(60fps,16ms/帧), 那就一定发生了卡顿，这也是 BlockCanary 的基础原理。
         
七、retrofit
   实现原理：动态代码+注解+建造者+适配器模式

   Retrofit用到的动态代理，并不能算是严格的代理模式。它只是利用了代理模式中invoke这一中转过程，来解析接口中的注解声明，
   然后通过这些注解声明来创建一个请求类，最终再通过该请求类来发起请求。
   也就是说，Retrofit所关注的重点在于如何创建invoke方法所返回的实例，
   而普通的代理模式则在于控制接口实现类的访问。
   
八、Matrix-ApkChecker 瘦身
  https://www.jianshu.com/p/3d5e6ebb7ae3
  https://www.pianshen.com/article/44211914547/





