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

   【解决办法】：修改方法，使其不耗时，放到子线程中，如网络访问，大文件操作等，防止ANR，避免GPU过度绘制。

八、如何避免因引入的开源库导致的安全性和稳定性？
   由于项目引入了太多第三方开源库，Android APP有65536方法数的问题，可使用multidex解决。Android Methods Count插件可以高效统计Android开源库的方法数。

九、如何节省内存使用，主动回收内存？
   尽量多使用内部类，提高程序效率，回收已使用的资源，合理使用缓存，合理设置变量的作用范围。

十、谈谈onSaveInstanceState()方法？何时会调用？
  当非人为终止Activity时，比如系统配置发生改变时导致Activity被杀死并重新创建、资源内存不足导致低优先级的Activity被杀死，会调用 onSavaInstanceState() 来保存状态。该方法调用在onStop之前，但和onPause没有时序关系。
  onSaveInstanceState()适用于对临时性状态的保存，而onPause()适用于对数据的持久化保存。

十一、Activity和Fragment的异同？
   Activity和Fragment的相似点在于，它们都可包含布局、可有自己的生命周期，Fragment可看似迷你活动。
   不同点是，由于Fragment是依附在Activity上的，多了些和宿主Activity相关的生命周期方法，如onAttach()、onActivityCreated()、onDetach()；另外，Fragment的生命周期方法是由宿主Activity而不是操作系统调用的，Activity中生命周期方法都是protected，而Fragment都是public，也能印证了这一点，因为Activity需要调用Fragment那些方法并管理它。

十二、View的事件分发机制？
   事件分发本质：就是对MotionEvent事件分发的过程。即当一个MotionEvent产生了以后，系统需要将这个点击事件传递到一个具体的View上。
   点击事件的传递顺序：Activity（Window） -> ViewGroup -> View
   三个主要方法：
   dispatchTouchEvent：进行事件的分发（传递）。返回值是 boolean 类型，受当前onTouchEvent和下级view的dispatchTouchEvent影响
   onInterceptTouchEvent：对事件进行拦截。该方法只在ViewGroup中有，View（不包含 ViewGroup）是没有的。一旦拦截，则执行ViewGroup的onTouchEvent，在ViewGroup中处理事件，而不接着分发给View。且只调用一次，所以后面的事件都会交给ViewGroup处理。
   onTouchEvent：进行事件处理。

   onTouch()、onTouchEvent()和onClick()关系？
   优先度onTouch()>onTouchEvent()>onClick()。因此onTouchListener的onTouch()方法会先触发；如果onTouch()返回false才会接着触发onTouchEvent()，同样的，内置诸如onClick()事件的实现等等都基于onTouchEvent()；如果onTouch()返回true，这些事件将不会被触发。

十三、View和SurfaceView的区别
   SurfaceView是从View基类中派生出来的显示类，他和View的区别有：
   View需要在UI线程对画面进行刷新，而SurfaceView可在子线程进行页面的刷新
   View适用于主动更新的情况，而SurfaceView适用于被动更新，如频繁刷新，这是因为如果使用View频繁刷新会阻塞主线程，导致界面卡顿
   SurfaceView在底层已实现双缓冲机制，而View没有，因此SurfaceView更适用于需要频繁刷新、刷新时数据处理量很大的页面

十四、invalidate()与postInvalidate()的区别
   invalidate()与postInvalidate()都用于刷新View，主要区别是invalidate()在主线程中调用，若在子线程中使用需要配合handler；而postInvalidate()可在子线程中直接调用。

十五、不同密度的图片资源，像素从高到低依次排序为xxxhdpi>xxhdpi>xhdpi>hdpi>mdpi>ldpi

十六、资源文件
   res/raw中的文件会被映射到R.java文件中，访问时可直接使用资源ID，不可以有目录结构
   assets文件夹下的文件不会被映射到R.java中，访问时需要AssetManager类，可以创建子文件夹。







