# Android View
一、ListView机制：用到的适配器有ArrayAdapter、SimpleAdapter、BaseAdapter
    BaseAdapter 重写的方法getCount()、getItem()、getItemId()、getView()，每绘制一次就调用一次getView()，在getView()中将事先定好的layout布局确定显示的效果并返回一个view对象作为一个item 显示出来，getItem()、getItemId()在调用LIstView响应方法时调用。

二、RecycleView
    方法：onCreateViewHolder() 、onBinderViewHolder()、getItemCount()
    三种布局：垂直or水平、网格、瀑布流
    需要自定义分割线、易于回收、View复用、便于实现添加和删除item动画。

    【RecycleView的缓存机制】
       缓存机制，分为四级缓存【https://www.jianshu.com/p/f9e21269da26】
       一级缓存：屏幕内缓存（mAttachedScrap）
       屏幕内缓存指在屏幕中显示的ViewHolder，这些ViewHolder会缓存在mAttachedScrap、mChangedScrap中 ：
       mChangedScrap 表示数据已经改变的ViewHolder列表，需要重新绑定数据（调用onBindViewHolder）
       mAttachedScrap 未与RecyclerView分离的ViewHolder列表

       二级缓存：屏幕外缓存（mCachedViews）
       用来缓存移除屏幕之外的 ViewHolder，默认情况下缓存容量是 2，可以通过 setViewCacheSize 方法来改变缓存的容量大小。
       如果 mCachedViews 的容量已满，则会优先移除旧 ViewHolder，把旧ViewHolder移入到缓存池RecycledViewPool 中。

       三级缓存：自定义缓存（ViewCacheExtension）
       给用户的自定义扩展缓存，需要用户自己管理 View 的创建和缓存，可通过Recyclerview.setViewCacheExtension()设置。

       四级缓存：缓存池（RecycledViewPool ）
       ViewHolder 缓存池，在mCachedViews中如果缓存已满的时候（默认最大值为2个），先把mCachedViews中旧的ViewHolder 存入到RecyclerViewPool。
       如果RecyclerViewPool缓存池已满，就不会再缓存。从缓存池中取出的ViewHolder ，需要重新调用bindViewHolder绑定数据。

       按照 ViewType 来查找 ViewHolder
       每个 ViewType 默认最多缓存 5 个
       可以多个 RecyclerView 共享 RecycledViewPool
       RecyclerViewPool底层是使用了SparseArray来分开存储不同ViewType的ViewHolder集合

       缓存策略
       Recyclerview在获取ViewHolder时按四级缓存的顺序查找，如果没找到就创建。其中只有RecycledViewPool找到时才会调用 onBindViewHolder，
       其它缓存不会重新bindViewHolder 。

       RecyclerView优化
       1.降低item的布局层次：降低item布局层级，可以减少界面创建的渲染时间，使用约束布局等。
       2.去除冗余的setOnItemClick事件：直接在onBindViewHolder方法中创建一个匿名内部类的方式来实现setOnItemClick是不可取的，这会导致在RecyclerView快速滑动时创建很多对象，
         优化方法为：事件的绑定和viewholder对应的rootView进行绑定
       3.复用pool缓存：如果存在RecyclerView中嵌套RecyclerView的情况，可以考虑复用RecyclerViewPool缓存池，减少开销。

       【总结】通过了解RecyclerView的四级缓存，我们可以知道，RecyclerView最多可以缓存 N（屏幕最多可显示的item数） + 2 (屏幕外的缓存) + 5*M (M代表M个ViewType，缓存池的缓存)，
           只有RecycledViewPool找到时才会重新调用 onBindViewHolder。

三、自定义View
    1、分类
       自定义组合控件  多个控件组合成为一个新的控件，方便多处复用
       继承系统View控件  继承自TextView等系统控件，在系统控件的基础功能上进行扩展
       继承View  不复用系统控件逻辑，继承View进行功能定义
       继承系统ViewGroup  继承自LinearLayout等系统控件，在系统控件的基础功能上进行扩展
       继承ViewViewGroup  不复用系统控件逻辑，继承ViewGroup进行功能定义
    2、方法
       View绘制流程基本由measure()、layout()、draw()这个三个函数完成
       函数作用相关方法
       measure()测量View的宽高measure(),setMeasuredDimension(),onMeasure()
       layout()计算当前View以及子View的位置layout(),onLayout(),setFrame()
       draw()视图的绘制工作draw(),onDraw()
    3、自定义View的注意事项   参考文章：Android自定义View注意事项 - 简书
       ①需要在onMeasure方法中处理wrap_content的方法，让View支持wrap_content；
       ②避免padding和子元素的margin失效，让View支持padding；
       ③尽量不要在View中使用Handler，View中已提供了post系列方法，可替代Handler作用；
       ④避免造成内存泄漏，View中如果有线程或者动画，需要及时停止。
    4、measure、layout、draw耗时对比(https://blog.csdn.net/cpcpcp123/article/details/115127899)
      各类布局除了measure差异比较大外，layout和draw耗时差异不大。
      RelativeLayout在measure这一步耗时贼严重。是由于相对布局须要给全部子View水平方向测量一次，再竖直方向测量一次，才能肯定每一个子View的大小。层级一旦太深，measure时间以指数上升。
      若是能够，在不增长嵌套的状况下，尽可能使用FrameLayout。FrameLayout可以实现重心，经过Gravity来实现。别老爱用RelativeLayout的AlignParentxxx属性
      LinearLayout若是子View的LayoutParams里有使用weight属性的话，measure时间和RelativeLayout几乎接近，由于也须要给每一个子View测量两次。
      ConstraintLayout是Google新推出的一个布局，在性能上比RelativeLayout好，比LinearLayout差一些。
      尽可能少写层级深的布局，能减小一个试图节点就少一些measure时间

四、事件分发机制
   1、dispatchTouchEvent() 负责事件分发。当点击事件产生后，事件首先传递给当前Activity，调用Activity的dispatchTouchEvent()方法，
      返回值为false则表示View或子View消费了此事件，如果返回true，则表示没有消费事件，并调用父View的onTouchEvent方法。
   2、onTouchEvent()用于处理事件，返回值决定当前控件是否消费了这个事件，也就是说在当前控件在调用父View的onTouchEvent方法完Touch事件后，
      是否还允许Touch事件继续向上（父控件）传递，一但返回True，则父控件不用操心自己来处理Touch事件。返回true，则向上传递给父控件。
   3、onInterceptTouchEvent() ViewGroup的一个方法，用于处理事件（类似于预处理，当然也可以不处理）并改变事件的传递方向，
      也就是决定是否允许Touch事件继续向下（子控件）传递，一但返回True（代表事件在当前的viewGroup中会被处理），则向下传递之路被截断
      （所有子控件将没有机会参与Touch事件），同时把事件传递给当前的控件的onTouchEvent()处理；返回false，则把事件交给子控件的onInterceptTouchEvent()。
   4、当一个Touch事件(触摸事件为例)到达根节点，即Acitivty的ViewGroup时，它会依次下发，下发的过程是调用子View(ViewGroup)的dispatchTouchEvent方法实现的。
     简单来说，就是ViewGroup遍历它包含着的子View，调用每个View的dispatchTouchEvent方法，而当子View为ViewGroup时，又会通过调用ViwGroup的dispatchTouchEvent
     方法继续调用其内部的View的dispatchTouchEvent方法。（见图：Android/Image/事件分发机制原理图.jpg）上述例子中的消息下发顺序是这样的：①-②-⑤-⑥-⑦-③-④。dispatchTouchEvent方法只负责事件的分发，
     它拥有boolean类型的返回值，当返回为true时，顺序下发会中断。
   【小结】onInterceptTouchEvent()默认返回false，不做截获。返回true之后，事件流的后端控件就没有机会处理touch事件。view的onTouchEvent()返回了false，
      那么该事件将被传递至其上一层次的view的onTouchEvent()处理，如果onTouchEvent()返回了true，那么后续事件将可以继续传递给该view的onTouchEvent()处理。
        
     View的事件分发机制？
        事件分发本质：就是对MotionEvent事件分发的过程。即当一个MotionEvent产生了以后，系统需要将这个点击事件传递到一个具体的View上。
        点击事件的传递顺序：Activity（Window） -> ViewGroup -> View
        三个主要方法：
        dispatchTouchEvent：进行事件的分发（传递）。返回值是 boolean 类型，受当前onTouchEvent和下级view的dispatchTouchEvent影响
        onInterceptTouchEvent：对事件进行拦截。该方法只在ViewGroup中有，View（不包含 ViewGroup）是没有的。一旦拦截，则执行ViewGroup的onTouchEvent，在ViewGroup中处理事件，而不接着分发给View。且只调用一次，所以后面的事件都会交给ViewGroup处理。
        onTouchEvent：进行事件处理。
     
        onTouch()、onTouchEvent()和onClick()关系？
        优先度onTouch()>onTouchEvent()>onClick()。因此onTouchListener的onTouch()方法会先触发；如果onTouch()返回false才会接着触发onTouchEvent()，同样的，内置诸如onClick()事件的实现等等都基于onTouchEvent()；如果onTouch()返回true，这些事件将不会被触发。


>>>解决RecyclerView嵌套时，子RecyclerView不能滑动，记录以下3种方法。
      参考文章：[https://www.jianshu.com/p/c5ccf0c38186](https://www.jianshu.com/p/c5ccf0c38186)
   
   >>public boolean dispatchTouchEvent(MotionEvent ev)
   用来进行事件的分发。如果事件能够传递给当前View，那么此方法一定会被调用，返回的结果受当前View的onTouchEvent和下级View的dispatchTouchEvent方法影响，表示是否消耗当前事件。
   >>public boolean onInterceptTouchEvent(MotionEvent ev)
   在dispatchTouchEvent(MotionEvent ev)方法内部调用，用来判断是否拦截某个事件，那么在同一个事件系列当中，此方法不会被再次调用，返回结果表示是否拦截当前事件。
   >>public boolean onTouchEvent(MotionEvent ev)
   在dispatchTouchEvent(MotionEvent ev)方法中调用，用来处理点击事件，返回的结果表示是否消耗当前事件，如锅不消耗，这在同一个事件系列中，当前View无法再接收到事件。
   
   >方法一：自定义父recyclerView并重写onInterceptTouchEvent()方法
   //不拦截，继续分发下去
   @Override
   public boolean onInterceptTouchEvent(MotionEvent e) {
       return false;
   }
   
   >方法二：子布局通知父布局不要拦截事件
   @Override
   public boolean dispatchTouchEvent(MotionEvent ev) {
        //父层ViewGroup不要拦截点击事件
       getParent().requestDisallowInterceptTouchEvent(true);
       return super.dispatchTouchEvent(ev);
   }}
   
   >方法三：OnTouchListener优先级很高，通过这个来告诉父布局，不要拦截我的事件
   childRecyclerView.setOnTouchListener(new View.OnTouchListener() {
   @Override
   public boolean onTouch(View v, MotionEvent event) {
       int action = event.getAction();
       if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
           v.getParent().requestDisallowInterceptTouchEvent(true);
       } else {
           v.getParent().requestDisallowInterceptTouchEvent(false);
       }
       return false;
       }
   });

五、Android 常见布局
    FrameLayout （框架布局）、LinearLayout（线性布局）、AbsoluteLayout（绝对布局）、RelativeLayout（相对布局）、TableLayout（表格布局）

六、Activity如何生成View？
      答：Activity执行在attch()方法的时候，会创建一个PhoneWindow（Window的子类），在onCreate()方法的setContentView()方法中，创建DecorView，DecorView的addView()方法，把layout布局加载出来。
      通过onDraw()画出来，画View之前调用onMeasure()方法计算显示的大小。

七、viewpager2
    新特性：
        基于RecyclerView实现。这意味着RecyclerView的优点将会被ViewPager2所继承。
        支持竖直滑动。只需要一个参数就可以改变滑动方向。
        支持关闭用户输入。通过setUserInputEnabled来设置是否禁止用户滑动页面。
        支持通过编程方式滚动。通过fakeDragBy(offsetPx)代码模拟用户滑动页面。
        CompositePageTransformer 支持同时添加多个PageTransformer。
        支持DiffUtil ，可以添加数据集合改变的item动画。
        支持RTL (right-to-left)布局。我觉得这个功能对国内开发者来说可能用处不大..
        
    原理：支持垂直滚动, 重写之前的 viewpager，使用 recycleview + LinearLayoutManager 实现竖直滚动, 其实可以理解为对recyclerview的二次封装；
      viewpager2的适配器FragmentStateAdapter在遇到预加载时，只会创建Fragment对象，不会把Fragment真正的加入到布局中，所以自带懒加载效果；
    API变动:
      FragmentStateAdapter替换了原来的 FragmentStatePagerAdapter
      RecyclerView.Adapter替换了原来的 PagerAdapter
      registerOnPageChangeCallback替换了原来的 addPageChangeListener
      移除了setPargeMargin方法
      FragmentStateAdapter和FragmentStatePagerAdapter作用相同, 可以用viewpager来管理fragment, 区别在于viewpager2的FragmentStateAdapter与recycleview的生命周期绑定
      另外viewpager2的Adapter是继承自recyclerview的adapter, 支持除了notifyDataSetChanged()以外的notifyItemChanged(int position)等方式, 使用上更加的便捷
    引用方式：implementation "androidx.viewpager2:viewpager2:1.0.0"
    在xml中设置orientation, 或者在代码中设置setOrientation(),可以控制横纵向
    ViewPager2内部封装的是RecyclerView，因此它的Adapter也就是RecyclerView的Adapter。
    常用方法：
      notifyDataSetChanged(); //刷新Viewpager 同样支持recyclerView的局部刷新
      setUserInputEnabled(false);//禁止手动滑动
      setCurrentItem(0, false);//跳转到指定页面，false不带滚动动画
      setCurrentItem(0);//跳转到指定页面，带滚动动画
      addItemDecoration()//设置分割线 同RecyclerView
      setOffscreenPageLimit();//设置预加载数量
      setOrientation();//设置方向
      fakeDragBy(offsetPx)代码模拟用户滑动页面。支持通过编程方式滚动。
      setPageTransformer()设置滚动动画，参数可传 CompositePageTransformer，PageTransformer
      
      懒加载也叫延迟加载:延迟加载或符合某些条件时才加载。 
      预加载:提前加载,当用户需要查看时可直接从本地缓存中读取。
      
      ViewPager2 实现懒加载：
          方法一：使用setUserVisibleHint(); 在AndroidX已被废弃，因此不能依靠该方法判断Fragment是否可见；
          方法二：

八、androidx.constraintlayout.widget.ConstraintLayout
    dependencies {
        compile 'com.android.support.constraint:constraint-layout:1.0.1'
    }
    优点：
      Constraint Layout可以在不嵌套view group的情况下实现非常庞大、复杂的布局。实现扁平化；
      Constraint Layout同时具有Relative Layout和Linear Layout的优点、特性。功能强大；
      使用Constraint Layout来布局时性能要比其他布局方式高；
      Constraint Layout无论是通过布局管理器拖拽，鼠标控制的形式实现还是使用XML代码去写，都比较方便；
      可以有效地解决布局嵌套过多的问题

九、AndroidX的常见依赖总结
   1、CardView
    implementation 'androidx.cardview:cardview:1.0.0'
   2、TabLayout
    implementation 'com.google.android.material:material:1.0.0'
   3、RecycleView
   implementation 'androidx.recyclerview:recyclerview:1.0.0'
   4、Snackbar
   implementation 'com.google.android.material.snackbar.Snackbar:1.0.0-rc01'
   5、swiperefreshlayout
   implementation 'androidx.swiperefreshlayout:swiperefreshlayout:1.0.0'
   6、viewpager
   implementation 'androidx.viewpager:viewpager:1.0.0'
   7、design ui 库
   implementation 'com.google.android.material:material:1.0.0-rc01'
   8、coordinatorlayout
   implementation 'androidx.coordinatorlayout:coordinatorlayout:1.0.0'
   9、constraintlayout 约束布局
   implementation 'androidx.constraintlayout:constraintlayout:1.1.2'
   10、NavigationView 侧滑
   implementation 'com.google.android.material.navigation.NavigationView：1.0.0'
   11、drawerlayout 抽屉布局
   implementation'androidx.drawerlayout:drawerlayout:1.0.0'
   12、gridlayout 网格布局
   implementation'androidx.gridlayout:gridlayout:1.0.0'
   13、recyclerview-selection RecycleView 高亮显示方案
   implementation'androidx.recyclerview:recyclerview-selection:1.0.0'
   14、viewPager 2 这个是2与viewPager不同哦
   implementation 'androidx.viewpager2:viewpager2:1.0.0'

十、自定义键盘 (https://www.jianshu.com/p/eb85dd20c6f8)：继承自keyboard



