# Android View
一、ListView机制：用到的适配器有ArrayAdapter、SimpleAdapter、BaseAdapter
    BaseAdapter 重写的方法getCount()、getItem()、getItemId()、getView()，每绘制一次就调用一次getView()，在getView()中将事先定好的layout布局确定显示的效果并返回一个view对象作为一个item 显示出来，getItem()、getItemId()在调用LIstView响应方法时调用。

二、RecycleView
    方法：onCreateViewHolder() 、onBinderViewHolder()、getItemCount()
    三种布局：垂直or水平、网格、瀑布流
    需要自定义分割线、易于回收、View复用、便于实现添加和删除item动画。

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

五、Android 常见布局
    FrameLayout （框架布局）、LinearLayout（线性布局）、AbsoluteLayout（绝对布局）、RelativeLayout（相对布局）、TableLayout（表格布局）

六、Activity如何生成View？
      答：Activity执行在attch()方法的时候，会创建一个PhoneWindow（Window的子类），在onCreate()方法的setContentView()方法中，创建DecorView，DecorView的addView()方法，把layout布局加载出来。
      通过onDraw()画出来，画View之前调用onMeasure()方法计算显示的大小。

