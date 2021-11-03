# Handler
1、机制
   主线程不能进行耗时操作，子线程不能更新UI，Handler实现线程间通信，将要发送的消息保存到Message中，
   Handler调用sendMessage()方法将message发送到MessageQueue，Looper对象不断调用loop()方法，
   不断从MessageQueue中取出message交给handler处理，从而实现线程间的通信。
   调度流程：
   handler ==> sendMsg ==> messageQueue.enqueueMessage ==> Looper.loop() 
   ==> messageQueue.next() ==> handler.dispatchMessage() ==> handler.handleMessage()
   MessageQueue 消息队列的数据结构：由单链表实现的优先级队列，插入排序算法

2、主线程handler不需要调用Looper.prepare()(主线程是ActivityThread，ActivityThread被创建的时候就会初始化Looper。)，
   Looper.loop()，通过sendMessage将message添加到messagequeue。
   子线程中 new Handler 必须 Looper.prepare(); Looper.loop();
   
   子线程中维护的looper，消息队列无消息时的处理方案？有什么用？
   必须quit()，使得子线程的handler 的msg为空，当执行quit()时，会将msg清空。

3、子线程可以new Handler，但是必须在new之前，必须调用Looper.prepare()方法，
   否则报错：java.lang.RuntimeException:Can’t create handler inside thread that has not called Looper.prepare()。

4、当创建Handler时将通过ThreadLocal在当前线程绑定一个Looper对象，而Looper持有MessageQueue对象。
   执行Handler.sendMessage(Message)方法将一个待处理的Message插入到MessageQueue中，这时候通过Looper.loop()
   方法获取到队列中Message，然后再交由Handler.handleMessage(Message)来处理。

5、Handler如何实现线程切换？(sendMsg 在子线程，dispatchMsg在主线程)
   Handler创建的时候会采用当前线程的Looper来构造消息循环系统，Looper在哪个线程创建，就跟哪个线程绑定，并且Handler是在他关联的Looper对应的线程中处理消息的。
   【xx】在子线程中通过sendMessage(msg)发送消息，将消息放入MessageQueue，而此时消息队列作为了内存中的一块资源(static final ThreadLocal<Looper>  sThreadLocal = new  ThreadLocal<Looper>)，
   且供线程共享。主线程的Lopper一直在轮询消息队列MessageQueue，获取到其中的msg，并处理dispathMessage(msg)

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
  
  一个线程怎么保证只有一个looerp且不能更改，利用threadmap中使用<唯一的threadlocal,value>来保证只有一个threadlocal，从而保证一个线程只有一个looper。
  final MessageQueue mQueue也是唯且不可更改的。

10、如何将一个Thread线程变成Looper线程？Looper线程有哪些特点？
  通过Looper.prepare()可将一个Thread线程转换成Looper线程。Looper线程和普通Thread不同，它通过MessageQueue来存放消息和事件、Looper.loop()进行消息轮询。

11、Message可以如何创建？哪种效果更好，为什么？
  Message msg = new Message();
  Message msg = Message.obtain();
  Message msg = handler1.obtainMessage();
  后两种方法都是从整个Messge池中返回一个新的Message实例，能有效避免重复Message创建对象，因此更鼓励这种方式创建Message。
  不建议 new Message(); 因为消息被处理后，会调用msg.recycleUnchecked();(Looper.java) ，回收消息时会将消息内容全部清空，再将置空的消息传入sPool，形成回路，可避免内存抖动。
  new Message()时需要申请内存，将形成多余的内存碎片，导致内存抖动，甚至OOM
void recycleUnchecked() {
  // Mark the message as in use while it remains in the recycled object pool.
  // Clear out all other details.
  flags = FLAG_IN_USE;
  what = 0;
  arg1 = 0;
  arg2 = 0;
  obj = null;
  replyTo = null;
  sendingUid = UID_NONE;
  workSourceUid = UID_NONE;
  when = 0;
  target = null;
  callback = null;
  data = null;
  synchronized (sPoolSync) {
    if (sPoolSize < MAX_POOL_SIZE) {
    next = sPool;
    sPool = this;
    sPoolSize++;
    }
  }
}

12、ThreadLocal有什么作用？
  ThreadLocal类可实现线程本地存储的功能，把共享数据的可见范围限制在同一个线程之内，无须同步就能保证线程之间不出现数据争用的问题，这里可理解为ThreadLocal帮助Handler找到本线程的Looper。
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

   handler 支持无限量的加入消息，没有阻塞队列的机制，为什么可以无限量？
   因为还有系统消息的处理，如果被限量，会导致系统崩溃。

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
   
   当系统有多个耗时任务需要执行时，每个任务都会开启个新线程去执行耗时任务，这样会导致系统多次创建和销毁线程，从而影响性能。
   为了解决这一问题，Google提出了HandlerThread，HandlerThread本质上是一个线程类，它继承了Thread。
   HandlerThread有自己的内部Looper对象，可以进行loopr循环。通过获取HandlerThread的looper对象传递给Handler对象，可以在handleMessage()方法中执行异步任务。
   创建HandlerThread后必须先调用HandlerThread.start()方法，Thread会先调用run方法，创建Looper对象。
   当有耗时任务进入队列时，则不需要开启新线程，在原有的线程中执行耗时任务即可，否则线程阻塞。
   它在Android中的一个具体的使用场景是IntentService。
   由于HanlderThread的run()方法是一个无限循环，因此当明确不需要再使用HandlerThread时，可以通过它的quit或者quitSafely方法来终止线程的执行。
   
   HanlderThread 的优缺点
   HandlerThread 优点是异步不会堵塞，减少对性能的消耗，方便使用(方便初始化，方便获取Looper)，保证了线程安全。
   HandlerThread 缺点是不能同时继续进行多任务处理，要等待进行处理，处理效率较低。
   HandlerThread 与线程池不同，HandlerThread是一个串队列，背后只有一个线程。

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
   两个方法都是调用了 MessageQueue.quit(boolean) 方法，当 MessageQueue.next() 方法发现已经调用过 MessageQueue.quit(boolean) 时会 return null 结束当前调用，
   否则即使 MessageQueue 已经是空的了也会阻塞等待。

19、ActivityThread
     为Android的主线程，其中的main方法为app的入口，main方法做ActivityThread的初始化和Handler、mainLooper的创建；

20、同步屏障机制
    (https://blog.csdn.net/cpcpcp123/article/details/115374057)
    Handler Message分类
      同步消息：正常情况下通过Handler发送的Message都属于同步消息，除非在发送的时候指定其是一个异步消息，同步消息会按顺序排列在队列中。
      异步消息：一般情况下与同步消息没有区别，只有在设置了同步屏障(barrier)时才有所不同。
      屏障消息(Barrier)：屏障(Barrier)是一种特殊的Message，它的target为null(只有屏障的target可以为null)，
       并且arg1属性被用作屏障的标识符来区别不同的屏障。屏障的作用是用于拦截队列中同步消息，放行异步消息。
    由 isAsynchronous 方法得到，通过 MessageQueue.postSyncBarrier() 方法可以发送一个Barrier。当发现遇到barrier后，
    队列中后续的同步消息会被阻塞，而异步消息则不受barrier的影响，直到通过调用MessageQueue.removeSyncBarrier()释放了指定的barrier。
    屏障和普通消息一样可以根据时间来插入到消息队列中的适当位置，并且只会挡住它后面的同步消息的分发。插入普通消息会唤醒消息队列，但是插入屏障不会。
    (https://blog.csdn.net/asdgbc/article/details/79148180)
    只在Looper死循环获取待处理消息时才会起作用，也就是说同步屏障在MessageQueue.next函数中发挥着作用。
    同步屏障可以通过MessageQueue.postSyncBarrier函数来设置，当设置了同步屏障之后，next函数将会忽略所有的同步消息，返回异步消息。
    换句话说就是，设置了同步屏障之后，Handler只会处理异步消息。
    再换句话说，同步屏障为Handler消息机制增加了一种简单的优先级机制，异步消息的优先级要高于同步消息。
    同步屏障的应用
    Android应用框架中为了更快的响应UI刷新事件在ViewRootImpl.scheduleTraversals中使用了同步屏障,UI先行
    
    Message next() {
    			//1、如果有消息被插入到消息队列或者超时时间到，就被唤醒，否则阻塞在这。
                nativePollOnce(ptr, nextPollTimeoutMillis);
    
                synchronized (this) {        
                    Message prevMsg = null;
                    Message msg = mMessages;
                    if (msg != null && msg.target == null) {//2、遇到屏障  msg.target == null
                        do {
                            prevMsg = msg;
                            msg = msg.next;
                        } while (msg != null && !msg.isAsynchronous());//3、遍历消息链表找到最近的一条异步消息
                    }
                    if (msg != null) {
                    	//4、如果找到异步消息
                        if (now < msg.when) {//异步消息还没到处理时间，就在等会（超时时间）
                            nextPollTimeoutMillis = (int) Math.min(msg.when - now, Integer.MAX_VALUE);
                        } else {
                            //异步消息到了处理时间，就从链表移除，返回它。
                            mBlocked = false;
                            if (prevMsg != null) {
                                prevMsg.next = msg.next;
                            } else {
                                mMessages = msg.next;
                            }
                            msg.next = null;
                            if (DEBUG) Log.v(TAG, "Returning message: " + msg);s
                            msg.markInUse();
                            return msg;
                        }
                    } else {
                        // 如果没有异步消息就一直休眠，等待被唤醒。
                        nextPollTimeoutMillis = -1;
                    }
    			//。。。。
            }
        }

    
    void scheduleTraversals() {
        if (!mTraversalScheduled) {
            mTraversalScheduled = true;
            //设置同步障碍，确保mTraversalRunnable优先被执行
            mTraversalBarrier = mHandler.getLooper().getQueue().postSyncBarrier();
            //内部通过Handler发送了一个异步消息
            mChoreographer.postCallback(
                    Choreographer.CALLBACK_TRAVERSAL, mTraversalRunnable, null);
            if (!mUnbufferedInputDispatch) {
                scheduleConsumeBatchedInput();
            }
            notifyRendererOfFramePending();
            pokeDrawLockIfNeeded();
        }
    }
    mTraversalRunnable调用了performTraversals执行measure、layout、draw。    
    为了让mTraversalRunnable尽快被执行，在发消息之前调用MessageQueue.postSyncBarrier设置了同步屏障。
    
    https://blog.csdn.net/start_mao/article/details/98963744
    屏障消息和普通消息的区别在于屏障没有tartget，普通消息有target是因为它需要将消息分发给对应的target，而屏障不需要被分发，它就是用来挡住普通消息来保证异步消息优先处理的。
    屏障和普通消息一样可以根据时间来插入到消息队列中的适当位置，并且只会挡住它后面的同步消息的分发。
    postSyncBarrier返回一个int类型的数值，通过这个数值可以撤销屏障。
    postSyncBarrier方法是私有的，如果我们想调用它就得使用反射。
    插入普通消息会唤醒消息队列，但是插入屏障不会。
    
21、Handler使用的设计模式(https://blog.csdn.net/cpcpcp123/article/details/115261890)
    Message 使用了享元模式 ----减少对象的创建,对象可以反复使用
    MessageQueue 生产者消费者

22、Handler 造成内存泄露的原因？
    Activity 中匿名使用 Handler 实际上会导致 Handler 内部类持有外部类的引用，而 SendMessage() 的时候 Message 会持有 Handler，
    enqueueMessage 机制又会导致 MeassageQueue 持有 Message。所以当发送的是延迟消息那么 Message 并不会立即的遍历出来处理而是阻塞到对应的 Message 触发时间以后再处理。
    那么阻塞的这段时间中页面销毁一定会造成内存泄漏。

23、IntentService：是一种异步、会自动停止的服务，内部采用HandlerThread。
   一种特殊的Service,继承自Service并且本身就是一个抽象类
   用于在后台执行耗时的异步任务，当任务完成后会自动停止
   拥有较高的优先级，不易被系统杀死（继承自Service的缘故），因此比较适合执行一些高优先级的异步任务
   内部通过HandlerThread和Handler实现异步操作
   创建IntentService时，只需实现onHandleIntent和构造方法，onHandleIntent为异步方法，可以执行耗时操作

24、runWithScissors()方法（handler 中的同步方法，handler 方法多为异步方法）
    是Handler 标记为hide的方法，不允许普通开发者使用；
    提问:如何在子线程通过 Handler 向主线程发送一个任务，并等主线程处理此任务后，再继续执行？
        答：借助runWithScissors()方法实现。
    执行流程：
        先简单的对入参进行校验；
        如果当前线程和 Handler 的处理线程一致，则直接运行 run() 方法；
        线程不一致，则通过 BlockingRunnable 包装一下，并执行其 postAndWait() 方法；
    该方法在Framework中用的多，比如 WMS 启动流程中，分别在 main() 和 initPolicy() 中，
       通过 runWithScissors() 切换到 "android.display" 和 "android.ui" 线程去做一些初始工作。
    不允许使用的原因：
       1、如果超时了，没有取消的逻辑；
       2、可能造成死锁