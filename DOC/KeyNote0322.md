1、LRU：最近最少使用算法，当缓存满时, 会优先淘汰那些近期最少使用的缓存对象。LinkedHashMap来作为LRUCache的存储容器；

2、SurfaceView双缓冲机制：通过 surfaceHolder.lockCanvas 锁定画布，实现下一张图片的绘制，再通过另外的线程刷新界面，绘制图片

3、多进程通信：AIDL（Android Interface Definition Language，Android接口定义语言）：大部分应用程序不应该使用AIDL去创建一个绑定服务，
              因为它需要多线程能力，并可能导致一个更复杂的实现。功能强大，支持进程间一对多的实时并发通信，并可实现 RPC (远程过程调用)。
          Messenger：支持一对多的串行实时通信， AIDL 的简化版本。利用Handler实现。（适用于多进程、单线程，不需要考虑线程安全），其底层基于AIDL。【方法简易、可实战】
          Bundle：四大组件的进程通信方式，只能传输 Bundle 支持的数据类型。
          ContentProvider：强大的数据源访问支持，主要支持 CRUD 操作，一对多的进程间数据共享，例如我们的应用访问系统的通讯录数据。
          BroadcastReceiver：即广播，但只能单向通信，接收者只能被动的接收消息。
          文件共享：在非高并发情况下共享简单的数据。
          Socket：通过网络传输数据。
          
4、①standard每一次都会创建新的实例；②singleTop栈顶复用。和standard相似，但是如果栈顶已有实例，复用该实例，回调onNewIntent()方法；③singleTask栈内复用。
  查找栈内有没有该实例，有则复用回调onNewIntent()方法，如果没有，新建Activity，并入栈；④singleInstance单例模式，全局唯一。具备singleTask所有特性，独占一个任务栈。
(A)onPause→(B)onCreate→(B)onStart→(B)onResume→(A)onStop

5、 【RecycleView的缓存机制】 一级缓存：屏幕内缓存、 二级缓存：屏幕外缓存2、三级缓存：自定义缓存、四级缓存：缓存池2

6、自定义View：measure()测量View的宽高、layout()计算当前View以及子View的位置、draw()视图的绘制工作

7、【内存溢出（OOM）】：程序在申请内存时，没有足够的内存空间使用；
      【内存泄漏（memory leak）】： 程序在申请内存后，无法释放已申请的内存空间，一次泄漏危害可忽略，但堆积严重最终会导致OOM；

8、使用Android Jetpack组件的优势：
   （1）Lifecycles轻松管理应用程序的生命周期。
   （2）LiveData构建可观察的数据对象，以便在基础数据更改时通知视图。
   （3）ViewModel存储在应用程序轮换中未销毁的UI相关数据，在界面重建后恢复数据。
   （4）Room轻松的实现SQLite数据库。
   （5）WorkManager系统自动调度后台任务的执行，优化使用性能。
   （6）Navigation导航组件轻松管理Fragment等页面跳转问题。

9、mvvm模式将Presener改名为View Model，基本上与MVP模式完全一致，唯一的区别是，它采用双向绑定(data-binding): View的变动，
    自动反映在View Model，反之亦然。使得视图和控制层之间的耦合程度进一步降低，关注点分离更为彻底，同时减轻了Activity的压力。
    这样开发者就不用处理接收事件和View更新的工作，框架已经帮你做好了。

10、Glide内存缓存的流程
    读：是先从lruCache取，取不到再从弱引用中取；
    存：内存缓存取不到，从网络拉取回来先放在弱引用里，渲染图片，图片对象Resources使用计数加一；
    渲染完图片，图片对象Resources使用计数减一，如果计数为0，图片缓存从弱引用中删除，放入lruCache缓存。
    Picasso包体积小、加载效率低、清晰，但功能有局限不能加载gif、只能缓存全尺寸（100K）；
      Glide功能全面，擅长大型图片流，支持生命周期管理、自动裁剪图片、三级缓存、Gif图加载、体积适中（500K）
      Fresco内存优化，减少oom，体积更大。

11、OkHttp：基于HttpURLConnection，专注于提升网络连接效率的Http客户端，能够实现IP和端口的请求重用一个socket，大大降低了连接时间，也降低了服务器的压力，
    对Http和https都有良好的支持，不用担心app版本更换的困扰，但是okHttp请求是在线程里执行，不能直接刷新UI，需要手动处理。

12、HTTPS 为了兼顾安全与效率，同时使用了对称加密和非对称加密。数据是被对称加密传输的，对称加密过程需要客户端的一个密钥，
  为了确保能把该密钥安全传输到服务器端，采用非对称加密对该密钥进行加密传输，总的来说，对数据进行对称加密，对称加密所要使用的密钥通过非对称加密传输。

13、HashMap基于AbstractMap类，实现了Map、Cloneable（能被克隆）、Serializable（支持序列化）接口； 非线程安全；
允许存在一个为null的key和任意个为null的value；采用链表散列的数据结构，即数组和链表的结合；初始容量为16，填充因子默认为0.75，扩容时是当前容量翻倍，即2capacity
如何实现HashMap线程同步？
  ①使用 java.util.Hashtable 类，此类是线程安全的。
  ②使用 java.util.concurrent.ConcurrentHashMap，此类是线程安全的。
  ③使用 java.util.Collections.synchronizedMap() 方法包装 HashMap object，得到线程安全的Map，并在此Map上进行操作。
  【CurrentHashMap 注意 key和value的null值：如果集合中包含有null的key或value，在遍历时会出现NullPointerException，
  导致遍历终止，影响最终数据结果，因此，应该有value值需谨慎使用CurrentHashMap】

14、四大引用类型
    强引用：创建一个对象并把这个对象赋给一个引用变量。 永远不会被垃圾回收，JVM宁愿抛出OutOfMemory错误也不会回收这种对象；
    软引用（SoftReference）：内存空间不足了，就会回收这些对象的内存。可用来实现内存敏感的高速缓存，比如网页缓存、图片缓存等。使用软引用能防止内存泄露，增强程序的健壮性；
    弱引用（WeakReference）：弱引用也是用来描述非必需对象的，当JVM进行垃圾回收时，无论内存是否充足，都会回收被弱引用关联的对象。
          在java中用java.lang.ref.WeakReference类或java.util.WeakHashMap来表示；
    虚引用（PhantomReference）：如果一个对象与虚引用关联，则跟没有引用与之关联一样，在任何时候都可能被垃圾回收器回收。在java中用java.lang.ref.PhantomReference类表示。

15、为什么要使用多线程？
   ①更好地利用CPU资源；
   ②进程间数据不能数据共享，线程可以；
   ③系统创建进程需要为该进程重新分配系统资源，创建线程代价较小；
   ④Java语言内置了多线程功能支持，简化了java多线程编程。

16、synchronized 保证三大性，原子性，有序性，可见性，
       volatile 保证有序性，可见性，不能保证原子性
       线程安全是一个多线程环境下正确性的概念，也就是保证多线程环境下共享的、可修改的状态的正确性
      线程安全需要保证的基本特征
   原子性：简单来说，就是相关操作不会中途被其他线程干扰，一般通过同步机制实现。
   可见性：是一个线程修改了某个共享变量，其状态能够立即被其他线程知晓，通常被解释为将线程本地状态反映到主内存上，volatile 就是负责保证可见性的。
   有序性：是保证线程内串行语义，避免指令重排等。


17、View的事件分发机制？
   事件分发本质：就是对MotionEvent事件分发的过程。即当一个MotionEvent产生了以后，系统需要将这个点击事件传递到一个具体的View上。
   点击事件的传递顺序：Activity（Window） -> ViewGroup -> View
   三个主要方法：
   dispatchTouchEvent：进行事件的分发（传递）。返回值是 boolean 类型，受当前onTouchEvent和下级view的dispatchTouchEvent影响
   onInterceptTouchEvent：对事件进行拦截。该方法只在ViewGroup中有，View（不包含 ViewGroup）是没有的。一旦拦截，则执行ViewGroup的onTouchEvent，在ViewGroup中处理事件，而不接着分发给View。且只调用一次，所以后面的事件都会交给ViewGroup处理。
   onTouchEvent：进行事件处理。

   onTouch()、onTouchEvent()和onClick()关系？
   优先度onTouch()>onTouchEvent()>onClick()。因此onTouchListener的onTouch()方法会先触发；如果onTouch()返回false才会接着触发onTouchEvent()，同样的，内置诸如onClick()事件的实现等等都基于onTouchEvent()；如果onTouch()返回true，这些事件将不会被触发。
