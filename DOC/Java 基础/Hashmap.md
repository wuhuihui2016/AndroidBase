# Hashmap
HashMap基于AbstractMap类，实现了Map、Cloneable（能被克隆）、Serializable（支持序列化）接口； 非线程安全；
允许存在一个为null的key和任意个为null的value；采用链表散列的数据结构，即数组和链表的结合；初始容量为16，填充因子默认为0.75，扩容时是当前容量翻倍，即2capacity

HashMap由数组+链表组成的，数组是HashMap的主体，链表则是主要为了解决哈希冲突而存在的，如果定位到的数组位置不含链表（当前entry的next指向null）,那么对于查找，添加等操作很快，
仅需一次寻址即可；如果定位到的数组包含链表，对于添加操作，其时间复杂度为O(n)，首先遍历链表，存在即覆盖，否则新增；对于查找操作来讲，仍需遍历链表，
然后通过key对象的equals方法逐一比对查找。所以，性能考虑，HashMap中的链表出现越少，性能才会越好。
HashMap 的实例有两个参数影响其性能：初始容量和加载因子。容量是哈希表中桶的数量，初始容量只是哈希表在创建时的容量。加载因子 是哈希表在其容量自动增加之前可以达到多满的一种尺度。
当哈希表中的条目数超出了加载因子与当前容量的乘积时，则要对该哈希表进行 rehash 操作（即重建内部数据结构），从而哈希表将具有大约两倍的桶数。在Java编程语言中，加载因子默认值为0.75，默认哈希表元为101
hashMap的默认加载因子为0.75，加载因子表示Hsah表中元素的填满的程度。加载因子越大,填满的元素越多,空间利用率越高，但冲突的机会加大了。反之,加载因子越小,填满的元素越少,
冲突的机会减小,但空间浪费多了。冲突的机会越大,则查找的成本越高。反之,查找的成本越小。当桶中元素到达8个的时候，概率已经变得非常小，也就是说用0.75作为加载因子，每个碰撞位置的链表长度超过８个是几乎不可能的。

Hashtable基于Map接口和Dictionary类；线程安全，开销比HashMap大，如果多线程访问一个Map对象，使用Hashtable更好；不允许使用null作为key和value；
底层基于哈希表结构；初始容量为11，填充因子默认为0.75，扩容时是容量翻倍+1，即2capacity+1

一、HashMap、HashTable、HashSet的异同
  转载文章：HashSet HashTable HashMap的区别 及其Java集合介绍 - ywl925 - 博客园
  ①HashSet是Set的一个实现类，HashMap是Map的一个实现类，同时HashMap是HashTable的替代品
  ②HashSet以对象作为元素，而HashMap以(key-value)的一组对象作为元素，且HashSet拒绝接受重复的对象。HashMap可以看作三个视图：key的Set，value的Collection，Entry的Set。 这里HashSet就是其实就是HashMap的一个视图。
   HashSet内部就是使用HashMap实现的，和HashMap不同的是它不需要Key和Value两个值。
   HashMap是一个数组和链表的结合体，新加入的放在链头，重复的key不同的alue被新value替代
  ③继承不同
   public class Hashtable extends Dictionary<> implements Map<>
   public class HashMap  extends AbstractMap<> implements Map<>
  ④HashTable 方法同步，而HashMap需要自己增加同步处理。
  ⑤HashTable中，key和value都不允许出现null值。
   在HashMap中，null可以作为键，这样的键只有一个；可以有一个或多个键所对应的值为null。用containsKey()方法来判断是否存在某个键。
  ⑥两个遍历方式的内部实现上不同。
   HashTable、HashMap都使用了 Iterator。而由于历史原因，HashTable还使用了Enumeration的方式。
  ⑦哈希值的使用不同
   HashTable直接使用对象的hashCode，HashTable中hash数组默认大小是11，增加的方式是 old*2+1。
   而HashMap重新计算hash值，HashMap中hash数组的默认大小是16，而且一定是2的指数。

二、为什么HashMap线程不安全（hash碰撞与扩容导致）
  HashMap的底层存储结构是一个Entry数组，每个Entry又是一个单链表，一旦发生Hash冲突的的时候，HashMap采用拉链法解决碰撞冲突，
  因为hashMap的put方法不是同步的，所以他的扩容方法也不是同步的，在扩容过程中，会新生成一个新的容量的数组，然后对原数组的
  所有键值对重新进行计算和写入新的数组，之后指向新生成的数组。当多个线程同时检测到hashmap需要扩容的时候就会同时调用resize操作，
  各自生成新的数组并rehash后赋给该map底层的数组table，结果最终只有最后一个线程生成的新数组被赋给table变量，其他线程的均会丢失。
  而且当某些线程已经完成赋值而其他线程刚开始的时候，就会用已经被赋值的table作为原始数组，这样也会有问题。扩容的时候 可能会引发链表形成环状结构

三、如何实现HashMap线程同步？
  ①使用 java.util.Hashtable 类，此类是线程安全的。
  ②使用 java.util.concurrent.ConcurrentHashMap，此类是线程安全的。
  ③使用 java.util.Collections.synchronizedMap() 方法包装 HashMap object，得到线程安全的Map，并在此Map上进行操作。
  【CurrentHashMap 注意 key和value的null值：如果集合中包含有null的key或value，在遍历时会出现NullPointerException，
  导致遍历终止，影响最终数据结果，因此，应该有value值需谨慎使用CurrentHashMap】

四、HashMap是有序的吗？如何实现有序？
  HashMap是无序的，而LinkedHashMap是有序的HashMap，默认为插入顺序，还可以是访问顺序，基本原理是其内部通过Entry维护了一个双向链表，负责维护Map的迭代顺序

五、HashMap是如何扩容的？如何避免扩容？
  HashMap几个默认值，初始容量为16、填充因子默认为0.75、扩容时容量翻倍。也就是说当HashMap中元素个数超过16*0.75=12时会把数组的大小扩展为2*16=32，然后重新计算每个元素在数组中的位置
  由于每次扩容还需要重新计算元素Hash值，损耗性能，所以建议在使用HashMap时，最好先估算Map的大小，设置初始值，避免频繁扩容