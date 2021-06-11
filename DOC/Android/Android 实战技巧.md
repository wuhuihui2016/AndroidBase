# Android 问题解决
一、通信方式
   1、线程间通讯方式：①共享变量；②管道；③handler；④runOnUiThread(Runnable)；⑤view.post(Runnable)；⑥view.postDelayed(Runnable, long)。
   2、进程间通讯方式：①管道；②FIFO；③消息队列；④信号量；⑤共享内存区；⑥套接字socket信号。
     【线程是进程的子集，一个进程可有多个线程】
   3、Activity间的通信方式：①Intent；②借助类的静态变量；③借助全局变量/Application；
      ④借助外部工具（SharedPreference、SQLite、File、剪贴板）；⑤借助Service。

二、SQLite 升级而不影响现有数据，DBHelper单例，在OnUpgrade()方法中判断oldVersion对数据库进行增删改查以实现数据库升级。
    插入大量数据时提速方法：开一个事务能提高一些速度。
     /**
         * 插入一组数据.
         *
         * @param values    数据键值对.
         * @param tableName 表名.
         */
        public int insert(ContentValues values[], String tableName) {
            int flag = 0;
            db = getWritableDatabase();
            db.beginTransaction(); //开启事务*****
            try {
                for (int i = 0; i < values.length; i++) {
                    db.insert(tableName, null, values[i]);
                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
                flag = -1;
            } finally {
                db.endTransaction();
                db.close();
            }
            return flag;
        }


三、性能优化技巧
    启动速度优化，布局优化，内存、电量、APP大小优化、列表滑动优化等等。
    性能优化工具：TraceView、Hierarchy Viewer。

四、ANR（Application Not Responding）
   Android应用程序有一段时间响应不够灵敏，系统会向用户显示应用程序无响应（ANR：Application Not Responding）对话框。
   默认情况下，当操作在一段时间内系统无法处理时，会在系统层面会弹出ANR对话框
              产生ANR可能是因为5s内无响应用户输入事件、10s内未结束BroadcastReceiver、20s内未结束Service
              想要避免ANR就不要在主线程做耗时操作，而是通过开子线程，方法比如继承Thread或实现Runnable接口、使用AsyncTask、IntentService、HandlerThread等
              引申：快读定位ANR方法：使用命令导出ANR日志，并分析关键信息。
   原因：主线程做耗时操作；主线程被其他线程锁；CPU被其他进程占用，该进程没有分配CPU资源；
     OnReceiver过多操作，IO操作，如数据库、文件、网络。


五、屏幕适配
   布局文件中view设置高宽时不限定大小，尽量使用wrap_content，match_parent；代码中设置高宽前可获取屏幕大小，
   如果是线性布局可设置view在LinearLayout的weight；单位dp适配屏幕，单位sp适用字体，多图片，多布局。

六、Android 五大存储方式
   使用SharedPreferences存储数据； 文件存储数据；SQLite数据库存储数据；使用ContentProvider存储数据；网络存储数据。
   当APP没有获取文件存储权限时，当需要存储大文件时，可以保存在APP-data-cache目录里。
   ContentProvider：抽象类，为不同应用数据提供数据共享，提供统一接口，通过uri标识要访问的数据。

   【扩展】apply将修改提交到内存，再异步提交磁盘文件，并发提交时会等待正在处理的数据更新后继续往下执行；而commit直接同步提交到磁盘。
          SharedPreferences不要存储特别的key/value,避免造成卡顿/ANR,高频操作SP应适当拆分文件。

七、UI卡顿原因
   每16ms绘制一次Activity，如果由于一些原因导致了我们的逻辑、CPU耗时、GPU耗时大于16ms(应用卡顿的根源就在于16ms内不能完成绘制渲染合成过程,
   16ms需要完成视图树的所有测量、布局、绘制渲染及合成)，UI就无法完成一次绘制，那么就会造成卡顿。①内存抖动问题，②方法耗时，③view本身卡顿。
   原因：1.主线程的作用：把事件分发给合适的view或者widget,解决办法：我们通过handler在子线程中做耗时操作：runOnUiThread方法、View.post 方法、View.postDelayed方法
        2.布局layout过于复杂，没办法在16ms中完成渲染
        3.View的过度绘制，由于过度绘制导致在同一帧重复绘制
        4.view频繁的触发measure，layout
        5.内存频繁触发GC过多（在同一帧内频繁的创建临时变量）

   【解决办法】：修改方法，使其不耗时，放到子线程中，如网络访问，大文件操作等，防止ANR，避免GPU过度绘制。

八、如何避免因引入的开源库导致的安全性和稳定性？
   由于项目引入了太多第三方开源库，Android APP有65536方法数的问题，可使用multidex解决。Android Methods Count插件可以高效统计Android开源库的方法数。

九、如何节省内存使用，主动回收内存？
   尽量多使用内部类，提高程序效率，回收已使用的资源，合理使用缓存，合理设置变量的作用范围。

十、谈谈onSaveInstanceState()方法？何时会调用？
  当非人为终止Activity时，比如系统配置发生改变时导致Activity被杀死并重新创建、资源内存不足导致低优先级的Activity被杀死，会调用 onSavaInstanceState() 来保存状态。该方法调用在onStop之前，但和onPause没有时序关系。
  onSaveInstanceState()适用于对临时性状态的保存，而onPause()适用于对数据的持久化保存。

  使用场景：①进程被异常杀死；②系统配置发生变化（比如横竖屏切花换）。
  当Activity处于onPause() ，onStop() ，onDestroy() 三种状态时程序可能会被Android系统回收掉，这时可能会造成用户在程序当中的数据或者修改丢失。
  于是我们需要”现场保护”，当下次重启程序或activity时恢复上一次的数据。
  因此Android提供了onSaveInstanceState(Bundlout State)方法会在程序被回收前进行调用，但需要注意的是onSaveInstanceState()方法只适合保存瞬态数据,
  比如UI控件的状态, 成员变量的值等，而不应该用来保存持久化数据。onRestoreInstanceState方法，需要注意的是onSaveInstanceState方法和onRestoreInstanceState方法
  “不一定”是成对的被调用的，onRestoreInstanceState被调用的前提是，activity A“确实”被系统销毁了，而如果仅仅是停留在有这种可能性的情况下，则该方法不会被调用，
  例如，当正在显示activity A的时候，用户按下HOME键回到主界面，然后用户紧接着又返回到activity A，这种情况下activity A一般不会因为内存的原因被系统销毁，
  故activity A的onRestoreInstanceState方法不会被执行。

十一、Activity和Fragment的异同？
   Activity和Fragment的相似点在于，它们都可包含布局、可有自己的生命周期，Fragment可看似迷你活动。
   不同点是，由于Fragment是依附在Activity上的，多了些和宿主Activity相关的生命周期方法，
   如onAttach()、onActivityCreated()、onDetach()；另外，Fragment的生命周期方法是由宿主Activity而不是操作系统调用的，
   Activity中生命周期方法都是protected，而Fragment都是public，也能印证了这一点，因为Activity需要调用Fragment那些方法并管理它。
      【Fragment生命周期】
      onAttach()： 完成Fragment和Activity的绑定，参数中的Activity即为要绑定的Activity，可以进行赋值等操作。
      onCreate() : 完成Fragment的初始化
      onCreateView() : 加载Fragment布局，绑定布局文件
      onActivityCreated() : 表名与Fragment绑定的Activity已经执行完成了onCreate，可以与Activity进行交互操作。
      onStart() : Fragment变为可见状态
      onResume() : Fragment变为可交互状态
      onPause()： Fragment变为不可交互状态(不代表不可见)
      onSaveInstanceState()：保存当前Fragment的状态。记录一些数据，比如EditText键入的文本，即使Fragment被回收又重新创建，一样能恢复EditText之前键入的文本。
      onStop(): Fragment变为不可见状态
      onDestroyView() : 销毁Fragment的有关视图，但并未和Activity解绑，可以通过onCreateView()重新创建视图。Fragment销毁时或者ViewPager+Fragment情况下会调用
      onDestroy() : 销毁Fragment时调用
      onDetach() : 解除和Activity的绑定。Fragment销毁最后一步。

十二、View和SurfaceView的区别
   SurfaceView是从View基类中派生出来的显示类，他和View的区别有：
   View需要在UI线程对画面进行刷新，而SurfaceView可在子线程进行页面的刷新
   View适用于主动更新的情况，而SurfaceView适用于被动更新，如频繁刷新，这是因为如果使用View频繁刷新会阻塞主线程，导致界面卡顿
   SurfaceView在底层已实现双缓冲机制，而View没有，因此SurfaceView更适用于需要频繁刷新、刷新时数据处理量很大的页面

十三、invalidate()与postInvalidate()的区别
   invalidate()与postInvalidate()都用于刷新View，主要区别是invalidate()在主线程中调用，若在子线程中使用需要配合handler；而postInvalidate()可在子线程中直接调用。

十四、不同密度的图片资源，像素从高到低依次排序为xxxhdpi > xxhdpi > xhdpi > hdpi > mdpi > ldpi

十五、资源文件
   res/raw中的文件会被映射到R.java文件中，访问时可直接使用资源ID，不可以有目录结构
   assets文件夹下的文件不会被映射到R.java中，访问时需要AssetManager类，可以创建子文件夹。

十六、在近期任务列表显示单个APP的多个Activity(仿微信打开小程序，打开新任务activity，在近期任务中呈现多个界面)
   实现方式一：代码实现
      ①在页面跳转时设置flag:
        Intent intent = new Intent(this, Main2Activity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT); //此标志用于将文档打开到一个基于此意图的新任务中
        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK); //此标志用于创建新任务并将活动导入其中
        startActivity(intent);
      ②在关闭时调用方法：finishAndRemoveTask();
      [注意]使用这种方式，必须具有在清单文件中设置的 android:launchMode="standard" 属性值（默认就是这个属性）

   实现方式二：配置 AndroidManifest.xml
      在要跳转的 Activity 配置
         <activity
             android:name=".Main3Activity"
             android:documentLaunchMode="intoExisting"
             android:excludeFromRecents="true"
             android:maxRecents="3"/>

         AndroidManifest.xml 中的属性：
         1. documentLaunchMode(启动模式)：
         intoExisting：如果之前已经打开过，则会打开之前的(类似于 Activity 的 singleTask)；
         always：不管之前有没有打开，都新创建一个(类似于 Activity 的 standard)；
         none：不会在任务列表创建新的窗口，依旧显示单个任务；
         never：不会在任务列表创建新的窗口，依旧显示单个任务，设置此值会替代 FLAG_ACTIVITY_NEW_DOCUMENT 和 FLAG_ACTIVITY_MULTIPLE_TASK 标志的行为（如果在 Intent 中设置了其中一个标志）。
         注：对于除 none 和 never 以外的值，必须使用 launchMode="standard" 定义 Activity。如果未指定此属性，则使用 documentLaunchMode="none"。
         2. excludeFromRecents：
         默认为 false 。
         设置为 true 时，只要你离开了这个页面，它就会从最近任务列表里移除掉。
         3. maxRecents：
         设置为整型值，设置应用能够包括在概览屏幕中的最大任务数。默认值为 16。达到最大任务数后，最近最少使用的任务将从概览屏幕中移除。
         android:maxRecents 的最大值为 50（内存不足的设备上为 25）；小于 1 的值无效。

十七、使用Android Jetpack组件的优势：
   （1）Lifecycles轻松管理应用程序的生命周期。
   （2）LiveData构建可观察的数据对象，以便在基础数据更改时通知视图。
   （3）ViewModel存储在应用程序轮换中未销毁的UI相关数据，在界面重建后恢复数据。
   （4）Room轻松的实现SQLite数据库。
   （5）WorkManager系统自动调度后台任务的执行，优化使用性能。
   （6）Navigation导航组件轻松管理Fragment等页面跳转问题。

   google推荐的基于Jetpack的Android客户端软件开发架构图：
   （1）通过定义Repository管理数据来源(Model)。
   （2）使用LiveData驱动界面(View)更新。
   （3）使用ViewModel代替Presenter管理数据(VM)。
   （4）Room（Sqlite）储存本地序列化的数据，Retrofit获取远程数据的数据。

十八、MVP、MVVM模式总结
   在MVP里，其中M层处理数据，业务逻辑等；V层处理界面的显示结果；C层起到桥梁的作用，来控制V层和M层通信以此来达到分离视图显示和业务逻辑层。
   Presenter完全把Model和View进行了分离，主要的程序逻辑在Presenter里实现。而且，Presenter与具体的 View是没有直接关联的，
   而是通过定义好的接口进行交互，从而使得在变更View时候可以保持Presenter的不变，即重用！不仅如此，我们还可以编写测试用的View，
   模拟用户的各种操作，从而实现对Presenter的测试 —— 而不需要使用自动化的测试工具。 我们甚至可以在Model和View都没有完成时候，
   就可以通过编写Mock Object（即实现了Model和View的接口，但没有具体的内容的）来测试Presenter的逻辑。
   MVP框架由3部分组成：View负责显示，Presenter负责逻辑处理，Model提供数据。在MVP模式里通常包含3个要素（加上View interface是4个）
   MVP的优势
    1、模型与视图完全分离，我们可以修改视图而不影响模型
    2、可以更高效地使用模型，因为所有的交互都发生在一个地方——Presenter内部
    3、我们可以将一个Presener用于多个视图，而不需要改变Presenter的逻辑。这个特性非常的有用，因为视图的变化总是比模型的变化频繁。
    4、如果我们把逻辑放在Presenter中，那么我们就可以脱离用户界面来测试这些逻辑（单元测试）

    MVVM模式的设计思想
    MVVM模式中，一个ViewModel和一个View匹配，它没有MVP中的IView接口，而是完全的和View绑定，所有View中的修改变化，
    都会自动更新到ViewModel中，同时ViewModel的任何变化也会自动同步到View上显示。这种自动同步之所以能够的原因是ViewModel
    中的属性都实现了observable这样的接口，也就是说当使用属性的set的方法，都会同时触发属性修改的事件，使绑定的UI自动刷新。
    (在WPF中，这个observable接口是 INotifyPropertyChanged; 在knockoutjs中，是通过函数ko.observable() 和ko.observrableCollection()来实现的)。
    所以MVVM比MVP更升级一步，在MVP中，V是接口IView, 解决对于界面UI的耦合; 而MVVM干脆直接使用ViewModel和UI无缝结合,
    ViewModel直接就能代表UI. 但是MVVM做到这点是要依赖具体的平台和技术实现的，比如WPF和knockoutjs, 这也就是为什么ViewModel不需要实现接口的原因，
    因为对于具体平台和技术的依赖，本质上使用MVVM模式就是不能替换UI的使用平台的.

    https://www.jianshu.com/p/ff6de219f988
    mvvm模式将Presener改名为View Model，基本上与MVP模式完全一致，唯一的区别是，它采用双向绑定(data-binding): View的变动，
    自动反映在View Model，反之亦然。使得视图和控制层之间的耦合程度进一步降低，关注点分离更为彻底，同时减轻了Activity的压力。
    这样开发者就不用处理接收事件和View更新的工作，框架已经帮你做好了。

十九、Android 日志系统(https://blog.csdn.net/selflearner/article/details/65630290)
    1.封装控制系统原生Log，然后依据不同的级别分别输出到Logcat和文件里，主要有类LogcatLog和FileLog实现。
    2.收集手机其它信息，在将log上报到server时一同上报，这些信息包含Settings信息、DropBox打印的log、应用的SharedPreference、设备分辨率信息等。全部这些被时限为XXColector类，能够依据须要（后台配置控制）进行上传。
    3.将FileLog信息、Crash信息、以及Collector收集的手机信息上报到server。上报的方式主要分为：通过Email发送和通过HTTP（以及后台CGI）发送。当然你也能够选择发送到Google Form等。
    4.Crash异常捕获处理（即：继承实现UncaughtExceptionHandler），有LogCenter中实现。
    5.良好的可配置信息，即：系统中全部的日志收集、发送方式都是后台可配置的。

    log的捕获(https://blog.csdn.net/selflearner/article/details/65630290)
    java层错误日志捕获：java本身提供了接口Thread.setDefaultUncaughtExceptionHandler，apk或android系统可利用该接口实现错误日志存储。
    native层错误日志捕获：/data/tombstones。
    其他系统日志：/data/system/dropbox， /data/anr。












