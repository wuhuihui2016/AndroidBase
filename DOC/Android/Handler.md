# Handler
1、机制
   主线程不能进行耗时操作，子线程不能更新UI，Handler实现线程间通信，将要发送的消息保存到Message中，
   Handler调用sendMessage()方法将message发送到MessageQueue，Looper对象不断调用loop()方法，
   不断从MessageQueue中取出message交给handler处理，从而实现线程间的通信。
2、主线程handler不需要调用Looper.prepare()(主线程是ActivityThread，ActivityThread被创建的时候就会初始化Looper。)，
   Looper.loop()，通过sendMessage将message添加到messagequeue。
3、子线程可以new Handler，但是必须在new之前，必须调用Looper.prepare()方法，
   否则报错：java.lang.RuntimeException:Can’t create handler inside thread that has not called Looper.prepare()。
4、当创建Handler时将通过ThreadLocal在当前线程绑定一个Looper对象，而Looper持有MessageQueue对象。
   执行Handler.sendMessage(Message)方法将一个待处理的Message插入到MessageQueue中，这时候通过Looper.loop()
   方法获取到队列中Message，然后再交由Handler.handleMessage(Message)来处理。
5、Handler如何实现线程切换？
   Handler创建的时候会采用当前线程的Looper来构造消息循环系统，Looper在哪个线程创建，就跟哪个线程绑定，并且Handler是在他关联的Looper对应的线程中处理消息的。
6、Handler内部如何获取到当前线程的Looper呢？
   ThreadLocal。ThreadLocal可以在不同的线程中互不干扰的存储并提供数据，通过ThreadLocal可以轻松获取每个线程的Looper。
   当然需要注意的是:
   ①线程是默认没有Looper的，如果需要使用Handler，就必须为线程创建Looper。我们经常提到的主线程，也叫UI线程，它就是ActivityThread，
   ②ActivityThread被创建时就会初始化Looper，这也是在主线程中默认可以使用Handler的原因。
7、子线程有哪些更新UI的方法？
  主线程中定义Handler，子线程通过mHandler发送消息，主线程Handler的handleMessage更新UI。
  用Activity对象的runOnUiThread方法。 创建Handler，传入getMainLooper。 View.post(Runnable)。
8、为什么系统不建议在子线程访问UI？
  不建议在子线程访问UI的原因是，UI控件非线程安全，在多线程中并发访问可能会导致UI控件处于不可预期的状态。而不对UI控件的访问加上锁机制的原因有：
  上锁会让UI控件变得复杂和低效，上锁后会阻塞某些进程的执行。
  子线程访问UI的崩溃原因和解决办法？
  崩溃发生在ViewRootImpl类的checkThread方法中：
        void checkThread() {
            if (mThread != Thread.currentThread()) {
                throw new CalledFromWrongThreadException(
                        "Only the original thread that created a view hierarchy can touch its views.");
            }
        }
  解决办法：
     在新建视图的线程进行这个视图的UI更新，主线程创建View，主线程更新View。
     在ViewRootImpl创建之前进行子线程的UI更新，比如onCreate方法中进行子线程更新UI。
     子线程切换到主线程进行UI更新，比如Handler、view.post方法。
9、一个Thread可以有几个Looper？几个Handler？
  一个Thread只能有一个Looper，可以有多个Handler
  对应关系Thread(1):Looper(1):MessageQueen(1):Handler(n).
  引申：更多数量关系：Looper有一个MessageQueue，可以处理来自多个Handler的Message；MessageQueue有一组待处理的Message，这些Message可来自不同的Handler；
      Message中记录了负责发送和处理消息的Handler；Handler中有Looper和MessageQueue。

10、如何将一个Thread线程变成Looper线程？Looper线程有哪些特点？
  通过Looper.prepare()可将一个Thread线程转换成Looper线程。Looper线程和普通Thread不同，它通过MessageQueue来存放消息和事件、Looper.loop()进行消息轮询。

11、Message可以如何创建？哪种效果更好，为什么？
  Message msg = new Message();
  Message msg = Message.obtain();
  Message msg = handler1.obtainMessage();
  后两种方法都是从整个Messge池中返回一个新的Message实例，能有效避免重复Message创建对象，因此更鼓励这种方式创建Message。

12、ThreadLocal有什么作用？
  hreadLocal类可实现线程本地存储的功能，把共享数据的可见范围限制在同一个线程之内，无须同步就能保证线程之间不出现数据争用的问题，这里可理解为ThreadLocal帮助Handler找到本线程的Looper。
  底层数据结构：每个线程的Thread对象中都有一个ThreadLocalMap对象，它存储了一组以ThreadLocal.threadLocalHashCode为key、以本地线程变量为value的键值对，而ThreadLocal对象就是当前线程的ThreadLocalMap的访问入口，也就包含了一个独一无二的threadLocalHashCode值，通过这个值就可以在线程键值值对中找回对应的本地线程变量。
  【引申】 ThreadLocal
       实现：ThreadLocal 的作用是提供线程内的局部变量，这种变量在线程的生命周期内起作用，减少同一个线程内多个函数或者组件之间一些公共变量的传递的复杂度。
       每个Thread 维护一个 ThreadLocalMap 映射表，这个映射表的 key 是 ThreadLocal 实例本身，value 是真正需要存储的 Object。

       为什么会内存泄漏？
       ThreadLocalMap使用ThreadLocal的弱引用作为key，如果一个ThreadLocal没有外部强引用来引用它，那么系统 GC 的时候，这个ThreadLocal势必会被回收，
       这样一来，ThreadLocalMap中就会出现key为null的Entry，就没有办法访问这些key为null的Entry的value，如果当前线程再迟迟不结束的话，
       这些key为null的Entry的value就会一直存在一条强引用链：Thread Ref -> Thread -> ThreaLocalMap -> Entry -> value永远无法回收，造成内存泄漏。
       为了避免内存泄漏，ThreadLocalMap在get()，remove()的时候会清除线程ThreadLocalMap里所有key为null的value，但是这样也并能完全保证不会内存泄漏
       ThreadLocal内存泄漏的根源是：由于ThreadLocalMap的生命周期跟Thread一样长，如果没有手动删除对应key就会导致内存泄漏，而不是因为弱引用。

       因此，ThreadLocal 的最佳实践：
       每次使用完ThreadLocal，都调用它的remove()方法，清除数据。在使用线程池的情况下，没有及时清理ThreadLocal，不仅是内存泄漏的问题，更严重的是可能导致业务逻辑出现问题。所以，使用ThreadLocal就跟加锁完要解锁一样，用完就清理。

13、主线程中Looper的轮询死循环为何没有阻塞主线程？
  Android是依靠事件驱动的，通过Loop.loop()不断进行消息循环，可以说Activity的生命周期都是运行在 Looper.loop()的控制之下，一旦退出消息循环，应用也就退出了。
  而所谓的导致ANR多是因为某个事件在主线程中处理时间太耗时，因此只能说是对某个消息的处理阻塞了Looper.loop()，反之则不然。

14、使用Hanlder的postDealy()后消息队列会发生什么变化？
  postDelay的Message并不是先等待一定时间再放入到MessageQueue中，而是直接进入并阻塞当前线程，然后将其delay的时间和队头的进行比较，按照触发时间进行排序，如果触发时间更近则放入队头，保证队头的时间最小、队尾的时间最大。此时，如果队头的Message正是被delay的，则将当前线程堵塞一段时间，直到等待足够时间再唤醒执行该Message，否则唤醒后直接执行。
  handler.post和handler.sendMessage的区别? post本质上还是用sendMessage实现的，post只是一种更方便的用法
  
  System.currentTimeMillis()与SystemClock.uptimeMillis() ?????
  1、System.currentTimeMillis()获取的是系统的时间，可以使用SystemClock.setCurrentTimeMillis(long millis)进行设置。
     如果使用System.currentTimeMillis()来获取当前时间进行计时，应该考虑监听ACTION_TIME_TICK, ACTION_TIME_CHANGED 和 
     ACTION_TIMEZONE_CHANGED这些广播ACTION，如果系统时间发生了改变，可以通过监听广播来获取。
  2、SystemClock.uptimeMillis()表示系统开机到当前的时间总数，单位是毫秒，但是，当系统进入深度睡眠（CPU休眠、屏幕休眠、
     设备等待外部输入）时间就会停止，但是不会受到时钟缩放、空闲或者其他节能机制的影响。
  3、SystemClock.elapsedRealtime()和SystemClock.elapsedRealtimeNanos()表示系统开机到当前的时间总数。它包括了系统深度睡眠的时间。
     这个时钟是单调的，它保证一直计时，即使CPU处于省电模式，所以它是推荐使用的时间计时器。
  
    有几种机制，用来控制事件时间： 
       1、标准函数Thread.sleep(millis)和Object.wait(millis)是一直被使用的。如果这些函数使用SystemClock.uptimeMillis()时钟，
          如果设备进入休眠状态，那么提醒时间可能会被推迟到设备唤醒。并且这些同步函数可以被Thread.interrupt()打断。
       2、SystemClock.sleep(millis)是一个工具函数类似于Thread.sleep(millis)，不同的是它忽略了InterruptedException异常，
          如果你不使用Thread.interrupt()的话，就可以使用这个函数来延迟。
       3、Handler可以在一个绝对或者相对的时间里同步的调度Runnable对象，它可以使用SystemClock.uptimeMillis()时钟。
       4、AlarmManager可以设置一个定时器事件，当时间到来的时候，不管设备是否处于深度睡眠或者正在运行，事件都会发生。

15、Android中还了解哪些方便线程切换的类？
  AsyncTask：底层封装了线程池和Handler，便于执行后台任务以及在子线程中进行UI操作。
  HandlerThread：一种具有消息循环的线程，其内部可使用Handler。
  IntentService：是一种异步、会自动停止的服务，内部采用HandlerThread。

  Handler机制存在的问题：多任务同时执行时不易精确控制线程。
  引入AsyncTask的好处：创建异步任务更简单，直接继承它可方便实现后台异步任务的执行和进度的回调更新UI，而无需编写任务线程和Handler实例就能完成相同的任务。

  HandlerThread是一个线程类，它继承自Thread。与普通Thread不同，HandlerThread具有消息循环的效果，这是因为它内部HandlerThread.run()方法中有Looper，能通过Looper.prepare()来创建消息队列，并通过Looper.loop()来开启消息循环。
  HandlerThread实现方法
  实例化一个HandlerThread对象，参数是该线程的名称；
  通过 HandlerThread.start()开启线程；
  实例化一个Handler并传入HandlerThread中的looper对象，使得与HandlerThread绑定；
  利用Handler即可执行异步任务；
  当不需要HandlerThread时，通过HandlerThread.quit()/quitSafely()方法来终止线程的执行。

16、HandlerThread机制原理
   在线程中创建一个Looper循环器循环消息队列，当有耗时任务进入对列时，不需要再开启新线程，避免线程阻塞；
   继承自Thread,在Thread开始执行时跟主线程在ActivityThread.main()方法内执行代码逻辑类似，初始化Looper--Looper.prepare(),轮询消息--Looper.loop();
   初始化Handler时，使用HandlerThread线程的Looper对象初始化---- new Handler(Looper)构造方法。
   此Handler使用的Looper是子线程创建的，执行message.target.dispatchMessage()也在子线程内，所以最终执行的Runnable或者handleMessage()也会在子线程内。

   示例代码
     // 步骤1：创建HandlerThread实例对象
     // 传入参数 = 线程名字，作用 = 标记该线程
     HandlerThread mHandlerThread = new HandlerThread("handlerThread");
     // 步骤2：启动线程
     mHandlerThread.start();
     // 步骤3：创建工作线程Handler & 复写handleMessage（）
     // 作用：关联HandlerThread的Looper对象、实现消息处理操作 & 与 其他线程进行通信
     // 注：消息处理操作（HandlerMessage（））的执行线程 = mHandlerThread所创建的工作线程中执行
     Handler workHandler = new Handler( handlerThread.getLooper() ) {
               @Override
               public boolean handleMessage(Message msg) {
                   ...//消息处理
                   return true;
               }
           });

     // 步骤4：使用工作线程Handler向工作线程的消息队列发送消息
     // 在工作线程中，当消息循环时取出对应消息 & 在工作线程执行相关操作
     // a. 定义要发送的消息
     Message msg = Message.obtain();
     //消息的标识
     msg.what = 1;
     // b. 通过Handler发送消息到其绑定的消息队列
     workHandler.sendMessage(msg);

   // 步骤5：结束线程，即停止线程的消息循环
     mHandlerThread.quit();

17、如何判断当前线程是主线程？
    ① 通过Thread.currentThread()得到当前线程，通过Looper.getMainLooper().getThread()得到主线程，进行比较即可。
    public boolean isMainThread() {
        方法A: return Looper.getMainLooper() == Looper.myLooper(); //通过Looper.getMainLooper()比较
        方法B: return Thread.currentThread() == Looper.getMainLooper().getThread(); //通过Looper.getMainLooper().getThread()比较
        方法C: return Looper.getMainLooper().getThread().getId() == Thread.currentThread().getId(); //通过Looper.getMainLooper().getThread()比较
        方法D: return Looper.getMainLooper().isCurrentThread(); //通过Looper.getMainLooper().getThread()比较
    }

    ② 另外，在Java中没有Looper对象，所以这种方法没用，可以通过Thread.getName()，来判断是否是主线程
    public boolean isMainThread() {
        return Thread.currentThread().getName().equals("main");
    }

18、Looper.loop() 会退出吗？
   不会自动退出，但是我们可以通过 Looper.quit() 或者 Looper.quitSafely() 让它退出。
   两个方法都是调用了 MessageQueue.quit(boolean) 方法，当 MessageQueue.next() 方法发现已经调用过 MessageQueue.quit(boolean) 时会 return null 结束当前调用，否则即使 MessageQueue 已经是空的了也会阻塞等待。
