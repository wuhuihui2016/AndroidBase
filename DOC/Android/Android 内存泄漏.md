# Android 内存泄漏
一、内存溢出，内存泄漏
   【内存溢出（OOM）】：程序在申请内存时，没有足够的内存空间使用。
   原因：加载对象过大，相对资源较多，来不及加载。
   解决办法：内存引用上做处理，比如用软引用；图片加载时处理（压缩等）；动态回收内存；优化内存分配，自定义堆内存大小，避免使用Enum，
   减少BitMap的内存占用，内存对象重复使用，避免对象的内存泄漏。

   【内存泄漏（memory leak）】： 程序在申请内存后，无法释放已申请的内存空间，一次泄漏危害可忽略，但推积严重最终会导致OOM；

二、原因分析及解决办法
  1、Handler 或 Runnable 作为非静态内部类  Handler 和 Runnable 作为匿名内部类，都会持有 Activity 的引用，由于 Handler 和 Runnable
    的生命周期比 Activity 长，导致Activity 无法被回收，从而造成内存泄漏。  解决办法：将Handler 和 Runnable 定义为静态内部类，在Activity 的onDestory()方法中调用Handler 的 removeCallbacks 方法来移除 Message。
    还有一种特殊情况，如果 Handler 或者 Runnable 中持有 Context 对象，那么即使使用静态内部类，还是会发生内存泄漏。

    解决办法：
    方法一：通过程序逻辑来进行保护。
      1.在关闭Activity的时候停掉你的后台线程。线程停掉了，就相当于切断了Handler和外部连接的线，Activity自然会在合适的时候被回收。
      2.如果你的Handler是被delay的Message持有了引用，那么使用相应的Handler的removeCallbacks()方法，把消息对象从消息队列移除就行了。
    方法二：将Handler声明为静态类。
      在Java 中，非静态的内部类和匿名内部类都会隐式地持有其外部类的引用，静态的内部类不会持有外部类的引用。
    方法三、使用弱引用 SoftReference<BaseActivity> softAc = new SoftReference<BaseActivity>(activity);

  静态类不持有外部类的对象，所以你的Activity可以随意被回收。由于Handler不再持有外部类对象的引用，导致程序不允许你在Handler中操作Activity中的对象了。所以你需要在Handler中增加一个对Activity的弱引用（WeakReference）。
        WebView泄漏：创建的对象没有在合适的时间销毁，则一直存在内存里耗费内存空间，WebView不建议在xml文件中指明，因为一直存在不能对其销毁，应该在代码中创建WebView，通过addView()的方式加入layout，在Activity 的onDestory()方法中需要销毁，先将加载的内容置为null，webView.destroy();

  2、webView泄露，Dialog泄漏
   解决办法：在onDestory()方法中释放引用
    mWebView.stopLoading();
    // 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
    mWebView.getSettings().setJavaScriptEnabled(false);
    mWebView.clearHistory();
    mWebView.clearView();
    mWebView.removeAllViews();
    mWebView.destroy();

  3、非静态内部类造成的内存泄漏  非静态类会持有外部类的引用，如果这个内部类比外部类的生命周期长，在外部类被销毁时，内部类无法回收，即造成内存泄漏；

  4、外部类中持有非静态内部类的静态对象  保持一致的生命周期，将内部类对象改成非静态；

  //反面例子：非静态内部类创建静态实例造成的内存泄漏
  public class MainActivity extends AppCompatActivity {

    private static TestResource mResource = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(mResource == null){
            mResource = new TestResource();
         }//...
    }

    class TestResource {//...
    }
  }

  5、其他内存泄漏情况：比如BraodcastReceiver 未注销，InputStream 未关闭，再代码中多注意注销或关闭。

  6、通过context.getApplicationContext()使用ApplicationContext作为Context传入，才能避免内存泄漏。如果一定要使用Activity的话，要使用弱引用。
     【引申】
      getApplicationContext() 返回应用的上下文，生命周期是整个应用，应用摧毁它才摧毁
      Activity.this的context 返回当前activity的上下文，属于activity ，activity 摧毁他就摧毁
      getBaseContext()  返回由构造函数指定或setBaseContext()设置的上下文
      this.getApplicationContext（）取的是这个应 用程序的Context，Activity.this取的是这个Activity的Context，这两者的生命周期是不同 的，前者的生命周期是整个应用，后者的生命周期只是它所在的Activity。

      Application是一个长引用，Activity是短引用。Application适用于存储那些需要反复读取的对象，比如用户的用户名和密码，应用程序的当前设置等。Activity适用于当前活动窗体，比如显示一个dialog，或新建一个View，传入的context对象就应该是当前Activity，而非Application。

      如果你打算持有一个长期活动并且需要Context引用的对象，记得使用Application对象。你可以通过调用Context.getApplicationContext() 或者 Activity.getApplication()方法得到它。
      总的来说，要避免Context相关的内存泄露，铭记以下几条：

      •不要对Activity（Activity继承自Context）作长期的引用（一个指向Activity的引用与Activity本身有相同的生命周期）；
      •（如果使用长引用）试着用Application代替Activity；
      •如果你不能控制内部类的生命周期，避免使用非静态内部类，应该用静态内部类，并且对里面的Activity作弱引用。该问题的解决方法是：对于外部类，用WeakReference构造静态内部类，同时要在视图根完成，并且它的WeakReference内部类要有一个实例（WeakReference）。

      【引申END】

  7、需要手动关闭的资源没有关闭
    如IO流、Curcor、集合需要调用close()、clear、remove方法进行关闭，广播需要取消注册等，而且需要在finally块中执行，保证无论如何都会关闭、属性动画也要在不可见时停止运行

  8、注册对象未销毁: 广播，回调监听

  9、Handler、 AsyncTask、new Thread、Timer/TimerTask使用不当（非静态匿名内部类，持有外部类Activity的引用。处理方法：创建一个静态Handler内部类，然后对Handler持有的对象使用弱引用。（弱引用，即在引用对象的同时仍然允许通过垃圾回收来回收该对象。）同时，对于还未处理完的消息，应在onDestroy()中通过removeCallbacksAndMessages(null)移除。使用静态内部类+弱引用）。
    集合造成内存泄漏：如果集合是静态变量，生命周期和app一致，它持有集合中对象的引用，导致这些对象无法被释放，造成内存泄漏。处理方法：释放集合对象，置空集合，以回收空间

  10、上传图片造成OOM问题：在androidmenifest.xml文件中更改application配置，将hardwareAccelerated设置为false，largeHeap设置为true
     android:hardwareAccelerated = "true"的时候会牺牲内存来加快图片的加载速度。
     android:largeHeap = "false"会限制app应用申请的内存大小。

三、如何让避免内存异常问题
  1.数据类型: 不要使用比需求更占用空间的基本数据类型
  2.循环尽量用 foreach ,少用 iterator, 自动装箱也尽量少用
  3.数据结构与算法的解度处理 (数组，链表，栈树，树，图)
  4.枚举优化，尽量不使用枚举，增加额外的内存消耗
  5.static , static final 的问题：static final 不需要进行初始化工作，打包在 dex 文件中可以直接调用，并不会在类初始化申请内存（基本数据类型的成员，可以全写成 static final）
  6.字符串的拼接尽量少用 +=，利用StringBudiler代替String
  7.重复申请内存问题  同一个方法多次调用，如递归函数 ，回调函数中 new 对象
  不要在 onMeause() onLayout() ,onDraw() 中去刷新UI（requestLayout)
  8.避免 GC 回收将来要重新使用的对象 (内存设计模式对象池 + LRU 算法)
  9.Activity 组件泄漏（handler、非静态内部类和匿名内部类持有activity引用）
  10.Service 耗时操作尽量使用 IntentService,而不是Service
  11.垃圾回收不是防止内存泄露的保险方式。

四、内存泄漏和内存溢出的区别
  内存泄漏(Memory Leak)是指程序在申请内存后，无法释放已申请的内存空间。是造成应用程序OOM的主要原因之一。
  内存溢出(out of memory)是指程序在申请内存时，没有足够的内存空间供其使用。
  内存抖动：指程序短时间内大量创建对象，然后回收的现象

五、垃圾回收 【https://www.jianshu.com/p/214e42fc0d37】
   1、触发GC的条件：
      1.1 当应用程序空闲时,即没有应用线程在运行时,GC会被调用。GC在优先级最低的线程中进行,当应用忙时,GC线程不会被调用,但以下条件除外。
      2.2 Java堆内存不足时,GC会被调用。当应用运行过程中创建新对象,若此时内存空间不足,JVM会强制调用GC线程,以便回收内存用于新的分配。
          若GC一次之后仍不能满足内存分配的要求,JVM会再进行两次GC作进一步的尝试,若仍无法满足要求,则 JVM将报“out of memory”的错误,Java应用将停止。

   2、垃圾回收步骤：要真正宣告一个对象死亡，至少要经历两次标记过程和一次筛选。
      2.1 标记：找出所有引用不为0(live)的实例；
      2.2 计划：判断是否需要压缩；
      2.3 清理：回收所有的free空间
      2.4 引用更新：将所有引用的地址进行更新
      2.5 压缩：减少内存碎片

      JVM根据generation(代)来进行GC，一共被分为
      young generation(年轻代)、tenured generation(老年代)、permanent generation(永久代, perm gen)，heap 空间不包括 perm gen。

      绝大多数的对象都在 young generation 被分配，也在 young generation 被收回，当 young generation 的空间被填满， GC 会进行 minor collection(次回收) ，
      这次回收不涉及到 heap 中的其他 generation ， minor collection 根据 weak generational hypothesis( 弱年代假设 ) 来假设 young generation 中大量的对象都是垃圾需要回收， minor collection 的过程会非常快。
      young generation 中未被回收的对象被转移到 tenured generation ，然而 tenured generation 也会被填满，最终触发 major collection( 主回收 ) ，这次回收针对整个 heap ，由于涉及到大量对象，所以比 minor collection 慢得多。

   3、优化建议：
      3.1 如果程序允许，尽早将不用的引用对象赋为null，这样可以加速GC的工作；
      3.2 尽量少用finalize函数。finalize函数是Java提供给程序员一个释放对象或资源的机会。但是，它会加大GC的工作量！
      3.3 如果需要使用经常使用的图片，可以使用SoftReference类型。它可以尽可能将图片保存在内存中，供程序调用，而不引起OutOfMemory；
      3.4 注意集合数据类型，包括数组，树，图，链表等数据结构，这些数据结构对GC来说，回收更为复杂，所以使用结束应立即置为null。
          另外，注意一些全局的变量，以及一些静态变量。这些变量往往容易引起悬挂对象（dangling reference），造成内存浪费；
      3.5 当程序有一定的等待时间，程序员可以手动执行System.gc()，通知GC运行，但是Java语言规范并不保证GC一定会执行。使用增量式GC可以缩短Java程序的暂停时间。
          System.gc()； Runtime.getRuntime().gc() 这个方法对资源消耗较大尽量不要手动去调用这个方法，不然可能引起程序的明显卡顿；
      3.6 尽量使用StringBuffer,而不用String来累加字符串
      3.7 能用基本类型如int,long,就不用Integer,Long对象。基本类型变量占用的内存资源比相应对象占用的少得多；
      3.8 尽量少用静态对象变量，静态变量属于全局变量,不会被GC回收,它们会一直占用内存；
      3.9 分散对象创建或删除的时间，集中在短时间内大量创建新对象,特别是大对象,会导致突然需要大量内存,JVM在面临这种情况时,只能进行主GC,以回收内存或整合内存碎片,从而增加主GC的频率。

六、内存检测工具：
    default heap：当系统未指定堆时。
    image heap：系统启动映像，包含启动期间预加载的类。此处的分配确保绝不会移动或消失。
    zygote heap：写时复制堆，其中的应用进程是从 Android 系统中派生的。
    app heap：您的应用在其中分配内存的主堆。
    JNI heap：显示 Java 原生接口 (JNI) 引用被分配和释放到什么位置的堆。






