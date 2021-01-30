# Java
一、数据类型
   1、byte 1个字符，short 、char2 个字符，int 、float 4个字符，long 、double 8个字符。
   2、volatile是一个类型修饰符（type specifier）volatile的作用是作为指令关键字，
      确保本条指令不会因编译器的优化而省略，且要求每次直接读值
   3、在JDK1.7之前，switch语句仅支持byte、short、char、int，在JDK1.7之后，枚举、字符串类型都可以，
      通过String.hashcode转成int进行判断
   4、String不可变原因：字符串常量池的需要；运行String对象缓存HashCode，提高效率；多线程安全
      字符串运行速度：StringBuilder > StringBuffer （线程安全）> String ，String为常量，其它为变量，所以运行慢。
      StringBuilder和StringBuffer基本相似，两个类的构造器和方法也基本相同。不同的是：StringBuffer是线程安全的，
      而StringBuilder则没有实现线程安全功能，所以性能略高

二、equal和==的区别：equal 比较对象，== 比较原生类型
    equal：存储空间的值是否相同，字符串内容比较，值是否相同
    ==：是否为同一内存空间，内存空间是否相同，引用是否相同
    【引申】：如果一个类重写了equals()方法，则一定也要重写hashCode()方法，原因是：虽然equals()方法重写可以保证正确判断两个对象
    在逻辑是否相同，但是hashCode()方法映射的物理地址是不相同的，依然会将逻辑上相同的两个元素存入集合，但是第二个对象的内容会是Null.

三、Queue 队列：先进先出，Stack 栈：后进先出。Collect -->List/Set/Map

五、常用集合
    1、HashMap
       1.1 HashMap由数组+链表组成的，数组是HashMap的主体，链表则是主要为了解决哈希冲突而存在的，如果定位到的数组位置不含链表
           （当前entry的next指向null）,那么对于查找，添加等操作很快，仅需一次寻址即可；如果定位到的数组包含链表，对于添加操作，
           其时间复杂度为O(n)，首先遍历链表，存在即覆盖，否则新增；对于查找操作来讲，仍需遍历链表，
           然后通过key对象的equals方法逐一比对查找。所以，性能考虑，HashMap中的链表出现越少，性能才会越好。
       1.2 HashMap的实例有两个参数影响其性能：初始容量和加载因子。
           容量是哈希表中桶的数量，初始容量只是哈希表在创建时的容量。加载因子是哈希表在其容量自动增加之前可以达到多满的一种尺度。
           当哈希表中的条目数超出了加载因子与当前容量的乘积时，则要对该哈希表进行 rehash 操作（即重建内部数据结构），
           从而哈希表将具有大约两倍的桶数。在Java编程语言中，加载因子默认值为0.75，默认哈希表元为101
       1.3 hashMap的默认加载因子为0.75，加载因子表示Hsah表中元素的填满的程度。加载因子越大,填满的元素越多,空间利用率越高，
           但冲突的机会加大了。反之,加载因子越小,填满的元素越少,冲突的机会减小,但空间浪费多了。冲突的机会越大,则查找的成本越高。
           反之,查找的成本越小。当桶中元素到达8个的时候，概率已经变得非常小，也就是说用0.75作为加载因子，每个碰撞位置的链表长度超过
           ８个是几乎不可能的。
    2、HashMap与几个集合的性能比较
       2.1 SparseArray稀疏数组与HashMap相比，正序插入快，逆序插入慢，查找慢占用内存少于HashMap；

       2.2 HashMap和ArrayMap的区别
           ①查找效率  HashMap依据HashCode查找，效率增加；ArrayMap使用二分法查找，效率下降。数量大时用HashMap
           ②扩展数量  HashMap初始值16个长度，每次扩容申请双倍的数组空间；A扩容申请空间更少
           ③扩容效率  ArrayMap更好
           ④内存消耗  数据量小时，ArrayMap更节省内存
           总结：数据量小时，并需要频繁使用map存储时，用ArrayMap，数据量大时，用HashMap。
    3、LinkedList和ArrayList的比较
       LinkedList 链表结构，查找慢，插入删除快；
       ArrayList 数组结构，查找快，插入慢。


六、四大引用类型
    强引用：创建一个对象并把这个对象赋给一个引用变量。 永远不会被垃圾回收，JVM宁愿抛出OutOfMemory错误也不会回收这种对象；
    软引用（SoftReference）：内存空间不足了，就会回收这些对象的内存。可用来实现内存敏感的高速缓存，比如网页缓存、图片缓存等。使用软引用能防止内存泄露，增强程序的健壮性；
    弱引用（WeakReference）：弱引用也是用来描述非必需对象的，当JVM进行垃圾回收时，无论内存是否充足，都会回收被弱引用关联的对象。
          在java中用java.lang.ref.WeakReference类或java.util.WeakHashMap来表示；
    虚引用（PhantomReference）：如果一个对象与虚引用关联，则跟没有引用与之关联一样，在任何时候都可能被垃圾回收器回收。在java中用java.lang.ref.PhantomReference类表示。

七、类加载
    经历7个阶段：加载、验证、准备、解析、初始化、使用和卸载。

八、Java优化细节：
    ① 避免随意使用static变量， GC通常不会回收这些对象所占用的内存；
    ② 避免过多过常地创建Java对象，比如循环方法中创建对象，应该最大限度地重用对象，同样需要避免不必要的创建对象；
    ③ 尽量使用final修饰符，能够使性能平均提高50%，不要使用finalize方法；
    ④ 慎用synchronized，尽量减小synchronize的方法。实现同步是要很大的系统开销作为代价的，甚至可能造成死锁。
    ⑤ 使用基本数据类型代替对象；
    ⑥ 多线程在未发生线程安全前提下应尽量使用HashMap、ArrayList
      HashTable、Vector等使用了同步机制，降低了性能。
      合理创建HashMap，默认initialCapacity为16，而loadFactor是0.75，需要多大的容量，最好能准确的估计你所需要的最佳大小，Hashtable，Vectors同理。
      确定StringBuffer的容量，默认16
    ⑦ 减少变量的重复计算；
    ⑧ 在finally块中释放资源，不要在循环中使用Try/Catch语句，应把Try/Catch放在循环最外层
    ⑨ 乘除使用移位操作：
      int num = a * 4;改为 int num = a << 2;   int num = a * 8; 改为 int num = a << 3;
      int num = a / 4;改为 int num = a >> 2;   int num = a / 8; 改为 int num = a >> 3;
    ⑩ 避免使用split：由于支持正则表达式，所以效率比较低；如果确实需要频繁的调用split，可以考虑使用apache的StringUtils.split(string,char)，频繁split的可以缓存结果。
      避免使用二维数组：比一维数组多得多，大概10倍以上!
      使用System.arraycopy ()代替通过来循环复制数组：System.arraycopy() 要比通过循环来复制数组快的多。

九、Java泛型
   设计原则：只要在编译时期没有出现警告，那么运行时期就不会出现ClassCastException异常.
   泛型：把类型明确的工作推迟到创建对象或调用方法的时候才去明确的特殊的类型
   优点：
     ① 代码更加简洁【不用强制转换】
     ② 程序更加健壮【只要编译时期没有警告，那么运行时期就不会出现ClassCastException异常】
     ③ 可读性和稳定性【在编写集合的时候，就限定了类型】

十、静态内部类和非静态内部类之间到底有什么不同？
   （1）内部静态类不需要有指向外部类的引用。但非静态内部类需要持有对外部类的引用。
   （2）非静态内部类能够访问外部类的静态和非静态成员。静态类不能访问外部类的非静态成员。他只能访问外部类的静态成员。
   （3）一个非静态内部类不能脱离外部类实体被创建，一个非静态内部类可以访问外部类的数据和方法，因为他就在外部类里面。
