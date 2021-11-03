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

    广播的类型主要分为5类：
    普通广播（Normal Broadcast）：开发者自身定义 intent的广播（最常用），sendBroadcast(intent);
    系统广播（System Broadcast）：涉及到手机的基本操作（如开机、网络状态变化、拍照等等），都会发出相应的广播，每个广播都有特定的Intent - Filter（包括具体的action）
    有序广播（Ordered Broadcast）：发送出去的广播被广播接收者按照先后顺序接收，按照Priority属性值从大-小排序；Priority属性相同者，动态注册的广播优先；sendOrderedBroadcast(intent);

      BroadcastReceiver与LocalBroadcastReceiver有什么区别？【https://www.guozzz.com/187/】
           【BroadcastReceiver】是针对应用间、应用与系统间、应用内部进行通信的一种方式 。是跨应用广播，利用Binder机制实现，
                支持动态和静态两种方式注册方式。(现在也不推荐使用静态注册了，8.0之后限制了绝大部分广播只能使用动态注册 )
           【LocalBroadcastReceiver】仅在自己的应用内发送接收广播，也就是只有自己的应用能收到，数据更加安全广播只在这个程序里，
                而且效率更高。 是应用内广播，利用Handler实现，利用了IntentFilter的match功能，提供消息的发布与接收功能，实现应用内通信，仅支持动态注册。
           BroadcastReceiver 静态注册：
           (1)在清单文件中，通过标签声明；
           (2)在Android3.1开始，对于接收系统广播的BroadcastReceiver，App进程退出后，无法接收到广播；对于自定义的广播，可以通过重写flag的值，使得即使App进程退出，仍然可以接收到广播。
           (3)静态注册的广播是由PackageManagerService负责。

           BroadcastReceiver 动态注册：
           (1)在代码中注册，程序运行的时候才能进行；2.跟随组件的生命周期；3.动态注册的广播是由AMS(ActivityManagerService)负责的。
           注意：对于动态注册，最好在Activity的onResume()中注册，在onPause()中注销。在系统内存不足时，onStop()、onDestory() 可能不会执行App就被销毁，onPause()在App销毁前一定会被执行，保证广播在App销毁前注销。

           LocalBroadcastReceiver 使用
           1.调用LocalBroadcastManager.getInstance(this)来获得实例，在发送和注册的时候采用，LocalBroadcastManager的sendBroadcast方法和registerReceiver方法。
           2.使用了单例模式，并且将外部传入的Context转换成了Application的Context，避免造成内存泄露。
           3.在构造方法中创建了Handler，实质是通过Handler进行发送和接受消息的。
           4.创建Handler时，传入了主线程的Looper，说明这个Handler是在主线程创建的，即广播接收者是在主线程接收消息的，所以不能在onReceiver（）中做耗时操作。
           注意：对于LocalBroadcastManager发送的广播，只能通过LocalBroadcastManager动态注册，不能静态注册。

           广播细分为三种: 普通广播(调用sendBroadcast()发送)、有序广播、本地广播

           有序广播：
           广播接收者会按照priority优先级从大到小进行排序
           优先级相同的广播，动态注册的广播优先处理
           广播接收者还能对广播进行截断和修改

           本地广播的优点?
           LocalBroadcast是APP内部维护的一套广播机制，有很高的安全性和高效性。所以如果有APP内部发送、接收广播的需要应该使用LocalBroadcast。
           发送的广播不会离开我们的应用，不会泄露关键数据。
           其他程序无法将广播发送到我们程序内部，不会有安全漏洞。
           本地广播的注意事项：
            本地广播无法通过静态注册来接收，相比起系统全局广播更加高效
            在广播中启动activity的话，需要为intent加入FLAG_ACTIVITY_NEW_TASK的标记，不然会报错，因为需要一个栈来存放新打开的activity。
            广播中弹出AlertDialog的话，需要设置对话框的类型为:TYPE_SYSTEM_ALERT不然是无法弹出的。
            LocalBroadcastManager所发送的广播action，只能与注册到LocalBroadcastManager中BroadcastReceiver产生互动。


六、Bitmap
    ①Config：表示图片像素类型
    ②三种压缩格式：Bitmap.CompressFormat.JPEG、Bitmap.CompressFormat.PNG、Bitmap.CompressFormat.WEBP
    ③BitmapFactory提供了四类加载方法：decodeFile、decodeResource、decodeStream、decodeByteArray。
     巨图加载：BitmapRegionDecoder，可以按照区域进行加载。高效加载：核心其实也很简单，主要是采样压缩、缓存策略、异步加载等
    ④内存优化：缓存LRU、缩放、Config、Compress选择、内存管理、缓存方式等等方面入手。、内存管理、内存优化、缩放、config、compress
    图片优化：异步加载，压缩处理bitmapFactory.options，设置内存大小，缓存于内存、SD卡，没有内存再从网络取。
    如何处理大图：BitmapFactory.Options，把inJustDecodeBounds这个属性设为true，计算inSampleSize。

七、APP 启动流程
   点击桌面图标，launcher进程启动主Activity以Binder方式发送给AMS服务，交付给ActivityManagerService
   处理Intent和flag信息，通过prepareMainLooper()方法loop处理消息

①点击桌面App图标，Launcher进程采用Binder IPC向system_server进程发起startActivity请求；
②system_server进程接收到请求后，向zygote进程发送创建进程的请求；
③Zygote进程fork出新的子进程，即App进程；
④App进程，通过Binder IPC向sytem_server进程发起attachApplication请求；
⑤system_server进程在收到请求后，进行一系列准备工作后，再通过binder IPC向App进程发送scheduleLaunchActivity请求；
⑥App进程的binder线程（ApplicationThread）在收到请求后，通过handler向主线程发送LAUNCH_ACTIVITY消息；
⑦主线程在收到Message后，通过发射机制创建目标Activity，并回调Activity.onCreate()等方法。
⑧到此，App便正式启动，开始进入Activity生命周期，执行完onCreate/onStart/onResume方法，UI渲染结束后便可以看到App的主界面。

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

   【扩展】多进程通信【https://www.jianshu.com/p/84a12977dc26】
        1、Android中支持的多进程通信方式：
          系统实现。
          AIDL（Android Interface Definition Language，Android接口定义语言）：大部分应用程序不应该使用AIDL去创建一个绑定服务，
              因为它需要多线程能力，并可能导致一个更复杂的实现。功能强大，支持进程间一对多的实时并发通信，并可实现 RPC (远程过程调用)。
          Messenger：支持一对多的串行实时通信， AIDL 的简化版本。利用Handler实现。（适用于多进程、单线程，不需要考虑线程安全），其底层基于AIDL。【方法简易、可实战】
          Bundle：四大组件的进程通信方式，只能传输 Bundle 支持的数据类型。
          ContentProvider：强大的数据源访问支持，主要支持 CRUD 操作，一对多的进程间数据共享，例如我们的应用访问系统的通讯录数据。
          BroadcastReceiver：即广播，但只能单向通信，接收者只能被动的接收消息。
          文件共享：在非高并发情况下共享简单的数据。
          Socket：通过网络传输数据。
        2、多进程引发的问题：
         不能内存共享，如：静态成员和单例模式失效
         线程同步机制失效
         SharedPreferences 可靠性降低，在主进程中往sharePreference存的值，在子进程不能立即可见。
              【https://blog.csdn.net/u013394527/article/details/80775899】
              解决方法：使用SharedPreferences时，SharedPreferences 在MODE_PRIVATE MODE_PUBLIC 之外其实还可以设置多进程的Flag，即 MODE_MULTI_PROCESS。
              SharedPreferences myPrefs = context.getSharedPreferences(MY_FILE_NAME, Context.MODE_MULTI_PROCESS | Context.MODE_PRIVATE);
              一旦我们设置了这个Flag，每次调用Context.getSharedPreferences 的时候系统会重新从SP文件中读入数据，因此我们在使用的时候每次读取和存入都要使用Context.getSharedPreferences 重新获取SP实例。即使是这样，由于SP本质上并不是多进程安全的，所以还是无法保证数据的同步，因此该方法我们并没有使用，我们也不推荐使用。
         Application 被多次创建
         文件共享问题，可能造成资源的竞争访问，导致诸如数据库损坏、数据丢失等
         调试麻烦

      Messenger使用方法：【https://www.cnblogs.com/ldq2016/p/8417692.html】
      服务实现一个Handler，由其接收来自客户端的每个调用的回调。
      Handler用于创建Messenger对象（对Handler的引用）。
      Messenger创建一个IBinder，服务通过onBind()使其返回客户端。
      客户端使用IBinder将Messenger（引用服务的Handler）实例化，然后使用后者将Message对象发送给服务。
      服务在其Handler中（具体地讲，是在handleMessage()方法中）接收每个Message。

十二、CPU
   是一个有多功能的优秀领导者，它的优点在于调度、管理、协调能力强，计算能力则位于其次，而GPU相当于一个能接受CPU调度的
   “拥有强大计算能力”的员工，GPU提供了多核并行计算的基础结构，且核心数非常多，可支撑大量数据的并行操作，拥有更高的访存速度，更高的浮点运算能力。

十三、Application的生命周期
    参考文章：Android中Application的用途及生命周期_YY小爬虫_新浪博客
    ①onCreate() 在创建应用程序时创建；
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

十五、Serializable 序列化接口，开销大，建议使用，java方法，在序列化的时候会产生大量的临时变量，从而引起频繁的GC；
        注意：Intent 传递 Serializable 对象时，被传递的 Serializable 对象里面的自定义成员对象也要实现Serializable接口，
             否则出现 java.lang.RuntimeException: Parcelable encountered IOException writing serializable object 异常。
     Parcelelable 使用麻烦，效率高，多用于内存，Android方法，性能比Serializeble高，Parcelable不能使用在要将数据存储在硬盘上的情况。
    【选型】
       1）在使用内存的时候，Parcelable比Serializable性能高，所以推荐使用Parcelable。
       2）Serializable在序列化的时候会产生大量的临时变量，从而引起频繁的GC。
       3）Parcelable不能使用在要将数据存储在磁盘上的情况，因为Parcelable不能很好的保证数据的持续性在外界有变化的情况下。尽管Serializable效率低点，但此时还是建议使用Serializable 。
       4）Serializable是Java中的序列化接口，其使用起来简单但是开销很大，序列化和反序列化过程需要大量I\O操作。
         而Parcelable是Android中序列化方式，更适合用在Android平台，使用起来稍复杂。效率很高，这也是Android推荐的序列化方式，因此在Android平台上首选Parcelable。
    【引申提问】Android Intent传递对象为什么要序列化？
       a.永久性保存对象，保存对象的字节序列到本地文件中
       b.对象可以在网络中传输
       c.对象可以在IPC之间传递（进程间通信）

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
    LruCache(内存缓存)：LruCache类是一个线程安全的泛型类：内部采用一个LinkedHashMap以强引用的方式存储外界的缓存对象，
       并提供get和put方法来完成缓存的获取和添加操作，当缓存满时会移除较早使用的缓存对象，再添加新的缓存对象。
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
    【扩展】用List list = new ArrayList(Arrays.asList(array)); 替换 List list = Arrays.asList(array);
    替换原因：asList(array) 抛出异常：Exception in thread "main" java.lang.UnsupportedOperationException，
        由于asList产生的集合并没有重写add,remove等方法，所以它会调用父类AbstractList的方法，而父类的方法中抛出的却是异常信息

   ArrayList可以实现快速随机访问
   ArrayList新增元素
        方法有两种，一种是直接将元素加到数组的末尾，另外一种是添加元素到任意位置。
        在添加元素之前，都会先确认容量大小，如果容量够大，就不用进行扩容；
        如果容量不够大，就会按照原来数组的1.5倍大小进行扩容，在扩容之后需要将数组复制到新分配的内存地址。
        添加元素到任意位置，会导致在该位置后的所有元素都需要重新排列，而将元素添加到数组的末尾，
        在没有发生扩容的前提下，是不会有元素复制排序过程的。
   ArrayList删除元素
        ArrayList在每一次有效的删除元素操作之后，都要进行数组的重组，并且删除的元素位置越靠前，数组重组的开销就越大。

   LinkedList基于双向链表数据结构实现，由于LinkedList存储数据的内存地址是不连续的，而是通过指针来定位不连续地址，
   因此，LinkedList不支持随机快速访问，LinkedList也就不能实现RandomAccess接口。
   LinkedList 新增元素有多种，添加至末尾或任意位置，如果添加到位置，只会改变前后元素的前后指针，指针将会指向添加的新元素，因此LinkedList新增元素快。

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


二十八、冷启动和热启动
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


二十九、进程保活
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

三十、Kotlin
    Kotlin是一种基于JVM的编程语言，对Java的一种拓展，比Java更简洁，Kotlin支持函数式编程,Kotlin类和Java类可以相互调用

    2、Kotlin环境搭建
    直接在Plugin中下载Kotlin插件即可，系统会自动配置到Kotlin环境

三十一、SurfaceView双缓冲机制
    缓冲：在我们的界面中图形都是在画布上绘制出来的，所以这个绘制的过程就叫缓冲,而画布也就可以称作缓冲区。

    缓冲的种类：
        无缓冲：不使用画布的情况下直接在窗口上进行绘图就叫做无缓冲绘图。
        单缓冲：用了一个画布，将所有的内容先绘制到画布上，再整体绘制到窗口，这个就叫做单缓冲绘图。
        双缓冲：用了两个画布，一个进行临时的绘图，一个进行最终的绘图，这样的就叫做双缓冲绘图。

    surfaceView缓冲和View缓冲的区别：
    surfaceView：通过 surfaceHolder.lockCanvas 锁定画布，实现下一张图片的绘制，再通过另外的线程刷新界面，绘制图片。
    View：直接在ondraw里绘制图片，刷新界面。
    ps：其实view也能实现双缓冲，我们可以在另一个ondraw里绘制下一张bitmap，或者另开一个线程去处理绘图以外的操作，从而实现view的双缓冲。

    surfaceView为什么比view好用？
    View是在UI主线程中进行绘制的，绘制时会阻塞主线程，如果onTouch处理的事件比较多的话会导致界面卡顿。
    而surfaceView是另开了一个线程绘制的，再加上双缓冲机制，所以要比view高效并且界面不会卡顿。
    （1）View底层没有双缓冲机制，SurfaceView有；
    （2）view主要适用于主动更新，而SurfaceView适用与被动的更新，如频繁的刷新
    （3）view会在主线程中去更新UI，而SurfaceView则在子线程中刷新；
    
    TextureView
    与SurfaceView相比，TextureView并没有创建一个单独的Surface用来绘制，这使得它可以像一般的View一样执行一些变换操作，设置透明度等。另外，Textureview必须在硬件加速开启的窗口中。
    在android 7.0上系统surfaceview的性能比TextureView更有优势，支持对象的内容位置和包含的应用内容同步更新，平移、缩放不会产生黑边。 在7.0以下系统如果使用场景有动画效果，可以选择性使用TextureView

三十二、单例模式实现方式
    1、饿汉式(线程安全，调用效率高，但是不能延时加载)
        public class ImageLoader{
             private static ImageLoader instance = new ImageLoader;
             private ImageLoader(){}
             public static ImageLoader getInstance(){
                  return instance;
              }
        }
        程序跑起来就被创建对象，如果程序始终没有使用这个单例对象，就会造成不必要的资源浪费，因此不推荐这种实现方式。
    2、懒汉式(线程安全，调用效率不高，但是能延时加载)
        public class SingletonDemo2 {

            //类初始化时，不初始化这个对象(延时加载，真正用的时候再创建)
            private static SingletonDemo2 instance;

            //构造器私有化
            private SingletonDemo2(){}

            //方法同步，调用效率低
            public static synchronized SingletonDemo2 getInstance(){
                if(instance==null){
                    instance=new SingletonDemo2();
                }
                return instance;
            }
        }
    3、Double CheckLock实现单例：DCL也就是双重锁判断机制（由于JVM底层模型原因，偶尔会出问题，不建议使用）
        public class SingletonDemo5 {
                private volatile static SingletonDemo5 SingletonDemo5;

                private SingletonDemo5() {
                }

                public static SingletonDemo5 newInstance() {
                    if (SingletonDemo5 == null) {
                        synchronized (SingletonDemo5.class) {
                            if (SingletonDemo5 == null) {
                                SingletonDemo5 = new SingletonDemo5();
                            }
                        }
                    }
                    return SingletonDemo5;
                }
            }
    4、静态内部类实现模式（线程安全，调用效率高，可以延时加载）
        public class SingletonDemo3 {

            private static class SingletonClassInstance{
                private static final SingletonDemo3 instance=new SingletonDemo3();
            }

            private SingletonDemo3(){}

            public static SingletonDemo3 getInstance(){
                return SingletonClassInstance.instance;
            }

        }
    5、枚举类（线程安全，调用效率高，不能延时加载，可以天然的防止反射和反序列化调用）
        public enum SingletonDemo4 {

            //枚举元素本身就是单例
            INSTANCE;

            //添加自己需要的操作
            public void singletonOperation(){
            }
        }
    【结语】单例对象 占用资源少，不需要延时加载，枚举好于饿汉；单例对象占用资源多，需要延时加载，静态内部类好于懒汉式。

三十三、LRU
    基本概念：LRU是Least Recently Used的缩写，最近最少使用算法。

    Java 实现LRUCache
      1、基于LRU的基本概念，为了达到按最近最少使用排序。能够选择HashMap的子类LinkedHashMap来作为LRUCache的存储容器。
      2、LinkedHashMap的原理：
      a、 对于LinkedHashMap而言，它继承与HashMap、底层使用哈希表与双向链表来保存全部元素。其基本操作与父类HashMap相似，它通过重写父类相关的方法。来实现自己的链接列表特性。
    HashMap是单链表。LinkedHashMap是双向链表
      b、存储：LinkedHashMap并未重写父类HashMap的put方法，而是重写了父类HashMap的put方法调用的子方法void recordAccess(HashMap m)。void addEntry(int hash, K key, V value, int bucketIndex) 和void createEntry(int hash, K key, V value, int bucketIndex)，提供了自己特有的双向链接列表的实现。
      c、读取：LinkedHashMap重写了父类HashMap的get方法，实际在调用父类getEntry()方法取得查找的元素后，再推断当排序模式accessOrder为true时。记录訪问顺序，将最新訪问的元素加入到双向链表的表头，并从原来的位置删除。因为的链表的添加、删除操作是常量级的，故并不会带来性能的损失。

    LRUCache的简单实现
    package com.knowledgeStudy.lrucache;
    import java.util.ArrayList;
    import java.util.Collection;
    import java.util.LinkedHashMap;
    import java.util.Map;
    /**
     * 固定大小 的LRUCache<br>
     * 线程安全
     **/
    public class LRUCache<K, V> {
        private static final float factor = 0.75f;//扩容因子
        private Map<K, V> map; //数据存储容器
        private int cacheSize;//缓存大小
        public LRUCache(int cacheSize) {
            this.cacheSize = cacheSize;
            int capacity = (int) Math.ceil(cacheSize / factor) + 1;
            map = new LinkedHashMap<K, V>(capacity, factor, true) {
                private static final long serialVersionUID = 1L;
                /**
                 * 重写LinkedHashMap的removeEldestEntry()固定table中链表的长度
                 **/
                @Override
                protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                    boolean todel = size() > LRUCache.this.cacheSize;
                    return todel;
                }
            };
        }
        /**
         * 依据key获取value
         *
         * @param key
         * @return value
         **/
        public synchronized V get(K key) {
            return map.get(key);
        }
        /**
         * put一个key-value
         *
         * @param key
         *            value
         **/
        public synchronized void put(K key, V value) {
            map.put(key, value);
        }
        /**
         * 依据key来删除一个缓存
         *
         * @param key
         **/
        public synchronized void remove(K key) {
            map.remove(key);
        }
        /**
         * 清空缓存
         **/
        public synchronized void clear() {
            map.clear();
        }
        /**
         * 已经使用缓存的大小
         **/
        public synchronized int cacheSize() {
            return map.size();
        }
        /**
         * 获取缓存中全部的键值对
         **/
        public synchronized Collection<Map.Entry<K, V>> getAll() {
            return new ArrayList<Map.Entry<K, V>>(map.entrySet());
        }
    }

三十六、Android 系统中的启动流程【https://www.jianshu.com/p/8185b57bd070】

三十七、ContentProvider【https://www.jianshu.com/p/11dffee54414】
    ContentProvider 是一种内容共享型组件，其主要职责是向其他组件以及其他应用提供数据。
    当 ContentProvider 所在的进程启动时，ContentProvider 会同时启动并被发布到 AMS 中。
    注意：ContentProvider 的 onCreate 要先于 Application 的 onCreate 执行

    是否是单例?
    一般来说，ContentProvider 都应该是单例的，ContentProvider 是否单例取决于注册时的 android:multiprcess 属性，
    如果该属性为 false ，则是单例的，这也是默认值。当 android:multiprocess 为 true 时为多例，
    这时候在每个调用者的进程中都存在一个 ContentProvider 对象。

    发布过程
    应用进程 & 系统进程
    当 ContentProvider 所在进程启动时，在 ActivityThread 的 main 函数中，调用 attach 方法，attach 方法中会调用 AMS 的 attachApplication 方法，
    最终通过 IPC 在 AMS 中调用 ActivityThread 中 ApplicationThread 的 bindApplication 方法，通过消息机制调用 ActivityThread 的 handleBindApplication 方法
    ContentProvider的初始化过程:Application#onBaseContextAttach() -> ContentProvider#onCreate() -> Application#onCreate()

    handleBindApplication 方法中有如下代码

        // 初始化 Application，并在初始化时调用 Application 的 attachBaseContext(Context context) 方法为 Application 附加上 Context，
        所以在 attachBase 方法中调用 getApplication 方法得到的是空
        Application app = data.info.makeApplication(data.restrictedBackupMode, null);

        // 加载 ContentProvider，并调用其 onCreate 方法
        if (!data.restrictedBackupMode) {
            List<ProviderInfo> providers = data.providers;
            // prividers 是根据新启动的应用进程的信息，由系统进程在系统启动时注册的 ContentProvider 中筛选出来的，启动 Application 时从系统进程传递过来
            if (providers != null) {
                installContentProviders(app, providers); // 初始化 ContentProvider，其中会调用 onCreate 方法
                mH.sendEmptyMessageDelayed(H.ENABLE_JIT, 10*1000);
            }
        }

        // 调用 Application 的 onCreate 方法，由此可见，ContentProvider 的 onCreate 方法早于 Application 的 onCreate 方法
        mInstrumentation.callApplicationOnCreate(app);
    installContentProviders 方法中遍历 providers 集合，分别调用 installProvider 方法的到 ContentProviderHolder 对象， installProvider 中使用 ClassLoader 初始化 ContentProvider 对象，然后调用 ContentProvider 的 attachInfo 方法初始化其信息，其中调用了 onCreate 方法。然后根据注册清单中注册的 ContentProvider 的信息，构造 ContentProviderHolder 对象，ContentProviderHolder 是可序列化的 ContentProvider，接着将IcontentProvider 放入已发布的 IContentProvider 的集合， 再将 ContentProviderHolder 对象放入集合中，通过 AMS 的 publishContentProviders 方法远程发布 ContentProviderHolder 集合到系统进程。这就完成了当前应用中 ContentProvider 的发布。

    ContentProviderHolder 中还有 IContentProvider 的引用，IContentProvider 继承了 IInterface 接口，可在 Binder 中传输，其子类是 ContentProviderNative 也是抽象类，其实现类为 ContentProvider 中的一个内部类 Transport ，在 Transport 实现的方法中调用了 ContentProvider 中的方法，所以 Transport 为 AIDL 中的 Stub 类，将 Transport 的引用放入 ContentProviderHolder 中，在系统进程可以得到 Transport 的代理 Proxy，在系统进程中通过 IPC 就可以调用 Transport 的方法，从而调用 ContentProvider 的方法。

    [工作过程]
    1、在使用 ContentProvider 时，我们会通过 ContentResolver 来进行操作
    2、Context 对象的 getContenResolver 方法，返回一个 ApplicationContentResolver 类对象，该类继承自 ContentResolver ，
       在 ContentImpl 构造方法中通 ApplicationContentResolver 类的构造方法初始化 ApplicationContentResolver 对象。
    3、以 query 方法为例，ApplicationContentResolver 的 query 方法，会调用 ContentResolver 的 query 方法，
       query 方法中调用 acquireProvider 方法来获取 IContentProvider 对象，IContentnProvider 继承了 IInterface 接口，
       ApplicationContentResolver 在 ContentResolver 中是抽象方法，最终会调用 ApplicationContentResolver 的 acquireProvider 方法，
       在调用 query 方法时会将 Uri 构造成 String 并以参数的形式传递;
    4、acquireProvider 方法中，会调用当前应用 ActivityThread 的 acquireProvider 方法来获取能处理当前 Uri 的 IContentProvider 对象，
       acquireProvider 方法中，会根据要操作的 Uri 来查找是否当前发布的 ContentProvider 中是否有对应的，如果有则直接返回，如果没有则通过进程间通信通知
       AMS 的 getContentProvider 方法启动需要的 ContentProvider 所在的进程，进程启动之后 ContentProvider 也会发布，得到对应的 ContentProvider 之后 AMS 会将其返回，acquireProvider 方法中也将得到的 IContentProvider 返回到 ApplicationContentResolver 的 query 方法中。
    5、ApplicationContentResolver 的 acquireProvider 方法中得到 IContentProvider 之后，通过 AMS 来访问 ContentProvider ，
       这里的 IContentProvider 是 AIDL 中的 Proxy，通过 IPC 调用 ContentProvider.Transport 对象的方法，Transport 的 query 方法，
       调用我们自定义的 ContentProvider 的 query 方法，并将结果返回到 acquireProvider 方法，在返回到客户端调用的 query 方法，完成工作。

三十八、分而治之算法(Devide and Conquer)
    1、分而治之是什么
    分而治之是算法设计中的一种方法
    它将一个问题分成多个和原问题相似的小问题，递归解决小问题，再将结果合并以解决原来的问题
    2、应用场景
    场景一：归并排序
    分：把数组从中间一分为二
    解：递归地对两个子数组进行归并排序
    合：合并有序子数组
    场景二：快速排序
    分：选基准，按基准把数组分成两个子数组
    解：递归地对两个子数组进行快速排序
    合：对两个子数组进行合并
    
ForkJoin是由JDK1.7后提供多线并发处理框架，基本思想是分而治之。使用ForkJoin将相同的计算任务通过多线程的进行执行。从而能提高数据的计算速度。
在google的中的大数据处理框架mapreduce就通过类似ForkJoin的思想。通过多线程提高大数据的处理。    
