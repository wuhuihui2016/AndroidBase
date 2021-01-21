# Android 机制原理
一、PackageManagerService ：
    APP安装有关的service，WindowManagerService APP调用窗口相关的service，
    ActivityManagerService 系统的引导服务，支持应用进程的启动、切换、调度、四大组件的启动和管理。

二、binder机制
    Android系统中进程间通讯（IPC）的一种方式，也是Android系统中最重要的特性之一。
    Android中的四大组件Activity，Service，Broadcast，ContentProvider，
    不同的App等都运行在不同的进程中，它是这些进程间通讯的桥梁。正如其名“粘合剂”一样，
    它把系统中各个组件粘合到了一起，是各个组件的桥梁。

三、launcher的实现
    Manifest 配置launcher，PackageManager、ActivityManager对应包的管理和应用进程的管理。

四、Android 版本特性
    6.0需要代码请求权限checkPermissions，7.0应用间文件共享限制，系统广播删除，
    8.0通知渠道、悬浮窗、透明窗口不允许屏幕旋转，9.0明文流量的网络请求（Https加密）
    Android SDK兼容：minSdkVersion必须，targetSdkVersion针对某版本，maxSdkVersion非必需。

五、动态广播和静态广播
    两者对比见图：Android/Image/静态广播和动态广播对比.png
    动态广播最好在Activity 的 onResume()注册、onPause()注销，动态广播，有注册就必然得有注销，否则会导致内存泄露，
    重复注册、重复注销也不允许

六、Bitmap
    ①Config：表示图片像素类型
    ②三种压缩格式：Bitmap.CompressFormat.JPEG、Bitmap.CompressFormat.PNG、Bitmap.CompressFormat.WEBP
    ③BitmapFactory提供了四类加载方法：decodeFile、decodeResource、decodeStream、decodeByteArray。
     巨图加载：BitmapRegionDecoder，可以按照区域进行加载。高效加载：核心其实也很简单，主要是采样压缩、缓存策略、异步加载等
    ④内存优化：缓存LRU、缩放、Config、Compress选择、内存管理、缓存方式等等方面入手。、内存管理、内存优化、缩放、config、compress
    图片优化：异步加载，压缩处理bitmapFactory.options，设置内存大小，缓存于内存、SD卡，没有内存再从网络取。
    如何处理大图：BitmapFactory.Options，把inJustDecodeBounds这个属性设为true，计算inSampleSize。

    【引申】开源框架：ImageLoader、Glide（google）、Fresco（FaceBook）、Picasso（Square）
    Picasso包体积小、清晰，但功能有局限不能加载gif、只能缓存全尺寸；
    Glide功能全面，擅长大型图片流，体积较大；
    Fresco内存优化，减少oom，体积更大。

七、APP 启动流程
   点击桌面图标，launcher进程启动主Activity以Binder方式发送给AMS服务，交付给ActivityManagerService
   处理Intent和flag信息，通过prepareMainLooper()方法loop处理消息

八、插件化、热修复、组件化
   解除代码耦合，插件支持热插拔，静默升级，从根本上解决65k属性和方法的bug，进行自定义classLoader。
   插件化和热修复都是动态加载技术，使用场景不同，热修复为解决线上问题或者小功能更新，插件化解决应用上的大问题。
   组件化：为了解耦，把复杂系统拆分成多个组件，分离组件边界和责任，便于独立升级和维护。

九、Framework 工作原理
   Android 系统对Linux、kernel、lib库等封装，提供WMS、AMS、binder机制，handler-message机制等方式，供APP使用。
   Framework 就是提供APP生存环境。

十、Android 动画分类：视图，属性，帧，gif。

十一、Android 进程
   1、一般大体分为前台进程，后台进程，可见进程，服务进程，空进程这五大进程。其中空进程优先级最低，调用startService()让service
     所在进程成为前台进程，service的onDestory()里重新启动自己可避免后台进程被杀死。
   2、一个应用允许多个进程，在清单文件配置的service为一个进程，Android:process就可以配置；
   3、多进程会引起的异常：静态成员和单例模式会失效，线程同步机制完全失效，SharedPreferences可靠性下降，Application会多次创建。

十二、CPU
   是一个有多功能的优秀领导者，它的优点在于调度、管理、协调能力强，计算能力则位于其次，而GPU相当于一个能接受CPU调度的
   “拥有强大计算能力”的员工，GPU提供了多核并行计算的基础结构，且核心数非常多，可支撑大量数据的并行操作，拥有更高的访存速度，更高的浮点运算能力。

十三、Application的生命周期
    参考文章：Android中Application的用途及生命周期_YY小爬虫_新浪博客
    ①onCreate0 在创建应用程序时创建；
    ②onTerminate()  在模拟环境下执行。当终止应用程序对象时调用，不保证一定被调用，当程序是被内核终止以便为其他应用程序释放资源，那么将不会提醒，
     并且不调用应用程序的对象的onTerminate方法而直接终止进程；
    ③onLowMemory() 低内存时执行。好的应用程序一般会在这个方法里面释放一些不必要的资源来应付当后台程序已经终止，前台应用程序内存还不够时的情况；
    ④onConfigurationChanged(Configuration newConfig) 配置改变时触发这个方法。
    ⑤onTrimMemory(int level) 程序在进行内存清理时执行​。

十四、简单的音频/视频格式
    PCM：脉冲编码调制，由二进制数字信号对光源进行通断调制产生，没有压缩的编码方式。
    WAV：无损音频文件格式，PCM是无损WAV文件中音频数据的一种编码方式，但是WAV还可以用其它编码。
    AVI：音视频交错，调用方便，图像质量好，压缩标准可选。
    WMV：可扩充的媒体类型，本地或网络回放，流优先级化。
    3GP：3G流媒体，配合3G网络高速传输而开发。
    FLV：文件小，加载速度快，用于网络观看视频。
    MP4：音视频压缩编码标准。

十五、Serializable 序列化接口，开销大，建议使用，java方法；
     Parcelelable 使用麻烦，效率高，多用于内存，Android方法。

十六、Service启动方式和生命周期
   ①startService()：开启，调用者退出后Service仍在；
         生命周期：onCreate()--onStartCommand()--onDestory()
   通过startService启动后，service会一直无限期运行下去，只有外部调用了stopService()或stopSelf()方法时，该Service才会停止运行并销毁。
        ②bindService()：开启，调用者退出后Service随即退出。
        生命周期：onCreate()--onBind()--onUnBind()--onDestory()
        ①+② 的生命周期：onCreate()--onStartCommand()--onBind()--onUnBind()--onDestory()

十七、JNI和NDK
   JNI是Java调用Native 语言的一种特性，属于Java，Java本地接口，使Java与本地其他类型语言交互（C++）
   实现步骤：在Java中声明Native方法，编译该文件得到.class文件，通过javah命令导出JNI头文件（.h文件），使用Java需要交互的本地代码实现子啊Java中声明的Native方法，编译so文件，通过Java执行Java程序，最终实现Java调用本地代码

   NDK（Native Develop Kit）：Android开发工具包，属于Android。
   作用：快速开发C、C++动态库，并自动将so文件和应用打包成APK，即可通过NDK在Android中使用JNI与本地代码（C、C++）交互（Android开发需要本地代码C、C++实现）
   特点：运行效率高，代码安全性高，功能拓展性好，易于代码复用和移植。
   使用步骤：①配置NDK环境；②创建Android项目，并于NDK进行关联；③在Android项目中声明所需调用的Native方法；④使用该Native方法；⑤通过NDK build命令编译产生so文件；⑥编译AS工程，实现调用本地代码。

   JNI和NDK的关系：JNI实现目的，NDK是Android实现JNI的手段，即在AS开发环境中通过NDK从而实现JNI功能。

十八、进程和线程的区别
   1、线程是进程的子集，一个进程可以有很多线程，每条线程并行执行不同的任务。
   2、不同的进程使用不同的内存空间，而所有的线程共享一片相同的内存空间。别把它和栈内存搞混，每个线程都拥有单独的栈内存用来存储本地数据。
   3、进程是cpu资源分配的最小单位，线程是cpu调度的最小单位、是进程中运行的多个子任务。
   4、进程之间不能共享资源，而线程共享所在进程的地址空间和其它资源。
   5、一个进程内可拥有多个线程，进程可开启进程，也可开启线程。
   6、一个线程只能属于一个进程，线程可直接使用同进程的资源,线程依赖于进程而存在。

十九、Android 签名打包v1和v2的区别
   v1签名是对jar进行签名（签名校验速度慢、完整性保障不够），V2签名是对整个apk签名（Android 7.0 以及以上版本才支持）：官方介绍就是：v2签名是在整个APK文件的二进制内容上计算和验证的，v1是在归档文件中解压缩文件内容。
   一定可行的方案： 只使用 v1 方案       
   不一定可行的方案：同时使用 v1 和 v2 方案       
   对 7.0 以下一定不行的方案：只使用 v2 方案
   1、如果要支持 Android 7.0 以下版本，那么尽量同时选择两种签名方式，但是一旦遇到签名问题，可以只使用 v1 签名方案   
   2、如果需要对签名后的信息做处理修改，那就使用v1签名方案   
   3、如果最后遇到各种不同的问题，可以不勾选v1和v2，直接打包签名

二十、jar文件与aar文件区别？
   jar文件：只包含class文件和清单文件，不包含资源文件，比如图片等所有的 res下的资源文件；
   aar文件(二进制文件库)：class以及res下的所有的资源文件全部包含。

二十一、JSON的优缺点：
    优点：
    （1）数据格式比较简单，易于读写，格式都是压缩的，占用带宽小；
    （2）易于解析，客户端JavaScript可以简单的通过eval_r()进行JSON数据的读取；
    （3）支持多种语言，包括ActionScript, C, C#, ColdFusion, Java, JavaScript, Perl, PHP, Python, Ruby等服务器端语言，便于服务器端的解析；
    （4）在PHP世界，已经有PHP-JSON和JSON-PHP出现了，偏于PHP序列化后的程序直接调用，PHP服务器端的对象、数组等能直接生成JSON格式，便于客户端的访问提取；
    （5）因为JSON格式能直接为服务器端代码使用，大大简化了服务器端和客户端的代码开发量，且完成任务不变，并且易于维护。
    缺点：
    （1）没有XML格式这么推广的深入人心和喜用广泛，没有XML那么通用性；
    （2）JSON格式目前在Web Service中推广还属于初级阶段。

二十二、LRU算法原理
    为减少流量消耗，可采用缓存策略。常用的缓存算法是LRU(Least Recently Used)：
    核心思想：当缓存满时, 会优先淘汰那些近期最少使用的缓存对象。主要是两种方式：
    LruCache(内存缓存)：LruCache类是一个线程安全的泛型类：内部采用一个LinkedHashMap以强引用的方式存储外界的缓存对象，并提供get和put方法来完成缓存的获取和添加操作，当缓存满时会移除较早使用的缓存对象，再添加新的缓存对象。
    DiskLruCache(磁盘缓存)： 通过将缓存对象写入文件系统从而实现缓存效果。

二十三、装箱、拆箱什么含义？
    装箱就是自动将基本数据类型转换为包装器类型，拆箱就是自动将包装器类型转换为基本数据类型。

二十四、什么是反射，有什么作用和应用？
    含义：在运行状态中，对于任意一个类都能知道它的所有属性和方法，对于任何一个对象都能够调用它的任何一个方法和属性。
    功能：动态性，体现在：在运行时判断任意一个类所具有的属性和方法； 在运行时判断任意一个对象所属的类；
         在运行时构造任意一个类的对象；在运行时调用任意一个对象的方法；生成动态代理。

二十五、final、finally、finalize()分别表示什么含义？
    final关键字表示不可更改，具体体现在：
    final修饰的变量必须要初始化，且赋初值后不能再重新赋值
    final修饰的方法不能被子类重写
    final修饰的类不能被继承
    finally：和try、catch成套使用进行异常处理，无论是否捕获或处理异常，finally块里的语句都会被执行，在以下4种特殊情况下，finally块才不会被执行：
    在finally语句块中发生了异常
    在前面的代码中用了System.exit()退出程序
    程序所在的线程死亡
    关闭CPU
    finalize()：是Object中的方法，当垃圾回收器将回收对象从内存中清除出去之前会调用finalize()，但此时并不代表该回收对象一定会“死亡”，还有机会“逃脱”

二十六、ArrayList和LinkedList的区别
    ArrayList的底层结构是数组，可用索引实现快速查找；是动态数组，相比于数组容量可实现动态增长
    LinkedList底层结构是链表，增删速度快；是一个双向循环链表，也可以被当作堆栈、队列或双端队列

二十七、AIDL(Android Interface definition language)
    Android中IPC（Inter-Process Communication）方式中的一种。AIDL的作用跨进程通信，是让你可以在自己的APP里绑定一个其他APP的service，使得你的APP可以和其他APP交互。
    建立AIDL服务要比建立普通的服务复杂一些，具体步骤如下：
     （1）在Eclipse Android工程的Java包目录中建立一个扩展名为aidl的文件。该文件的语法类似于Java代码，但会稍有不同。
     （2）如果aidl文件的内容是正确的，ADT会自动生成一个Java接口文件（*.java）。
     （3）建立一个服务类（Service的子类）。
     （4）实现由aidl文件生成的Java接口。
     （5）在AndroidManifest.xml文件中配置AIDL服务，尤其要注意的是，<action>标签中android:name的属性值就是客户端要引用该服务的ID，也就是Intent类的参数值。
    实现AIDL接口的说明：
     （1）AIDL接口只支持方法，不能声明静态成员；
     （2）不会有返回给调用方的异常。

二十八、LruCache原理
    LruCache是个泛型类，内部采用LinkedHashMap来实现缓存机制，它提供get方法和put方法来获取缓存和添加缓存，
    其最重要的方法trimToSize是用来移除最少使用的缓存和使用最久的缓存，并添加最新的缓存到队列中。

二十九、冷启动和热启动
    1、什么是冷启动和热启动
    冷启动：在启动应用前，系统中没有该应用的任何进程信息
    热启动：在启动应用时，在已有的进程上启动应用（用户使用返回键退出应用，然后马上又重新启动应用）

    2、冷启动和热启动的区别
    冷启动：创建Application后再创建和初始化MainActivity
    热启动：创建和初始化MainActivity即可

    3、冷启动时间的计算
    这个时间值从应用启动（创建进程）开始计算，到完成视图的第一次绘制为止

    4、冷启动流程
    Zygote进程中fork创建出一个新的进程
    创建和初始化Application类、创建MainActivity
    inflate布局、当onCreate/onStart/onResume方法都走完
    contentView的measure/layout/draw显示在界面上
    总结：点击App->IPC->Process.start->ActivityThread->Application生命周期->Activity生命周期->ViewRootImpl测量布局绘制显示在界面上

    5、冷启动优化
    减少第一个界面onCreate()方法的工作量
    不要让Application参与业务的操作
    不要在Application进行耗时操作
    不要以静态变量的方式在Application中保存数据
    减少布局的复杂性和深度
    不要在mainThread中加载资源
    通过懒加载方式初始化第三方SDK

三十、Serializeble和Parcelable
    Serializeble：是java的序列化方式，Serializeble在序列化的时候会产生大量的临时对象，从而引起频繁的GC
    Parcelable：是Android的序列化方式，且性能比Serializeble高，Parcelable不能使用在要将数据存储在硬盘上的情况

三十一、进程保活
    1、进程的优先级
    空进程、后台进程、服务进程、可见进程、前台进程

    2、Android进程回收策略
    Low memory Killer（定时执行）：通过一些比较复杂的评分机制，对进程进行打分，然后将分数高的进程判定为bad进程，杀死并释放内存
    OOM_ODJ：判别进程的优先级

    3、Android保活方案
    利用系统广播拉活
    利用系统Service机制拉活
    利用Native进程拉活
    利用JobScheduler机制拉活
    利用账号同步机制拉活

三十二、Kotlin
    Kotlin是一种基于JVM的编程语言，对Java的一种拓展，比Java更简洁，Kotlin支持函数式编程,Kotlin类和Java类可以相互调用

    2、Kotlin环境搭建
    直接在Plugin中下载Kotlin插件即可，系统会自动配置到Kotlin环境


