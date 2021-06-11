package com.whh.androidbase.utils;

/**
 * Matrix特性
 *  1.APK Checker
 *      具有更好的可用性：JAR 包方式提供，更方便应用到持续集成系统中，从而追踪和对比每个 APK 版本之间的变化
 *      更多的检查分析功能：除具备 APKAnalyzer 的功能外，还支持统计 APK 中包含的 R 类、检查是否有多个动态库静态链接了 STL 、搜索 APK 中包含的无用资源，以及支持自定义检查规则等
 *      输出的检查结果更加详实：支持可视化的 HTML 格式，便于分析处理的 JSON ，自定义输出等等
 *  2.Resource Canary
 *      分离了检测和分析部分，便于在不打断自动化测试的前提下持续输出分析后的检测结果
 *      对检测部分生成的 Hprof 文件进行了裁剪，移除了大部分无用数据，降低了传输 Hprof 文件的开销
 *      增加了重复 Bitmap 对象检测，方便通过减少冗余 Bitmap 数量，降低内存消耗
 *  3.Trace Canary
 *      编译期动态修改字节码, 高性能记录执行耗时与调用堆栈
 *      准确的定位到发生卡顿的函数，提供执行堆栈、执行耗时、执行次数等信息，帮助快速解决卡顿问题
 *      自动涵盖卡顿、启动耗时、页面切换、慢函数检测等多个流畅性指标
 *  4.SQLite Lint
 *      接入简单，代码无侵入
 *      数据量无关，开发、测试阶段即可发现SQLite性能隐患
 *      检测算法基于最佳实践，高标准把控SQLite质量*
 *      底层是 C++ 实现，支持多平台扩展
 *  5.IO Canary
 *      接入简单，代码无侵入
 *      性能、泄漏全面监控，对 IO 质量心中有数
 *  6.兼容到 Android P
 *      Battery Canary
 *      接入简单，开箱即用
 *      预留 Base 类和 Utility 工具以便扩展监控特性
 *  7.Memory Hook
 *      一个检测 Android native 内存泄漏的工具
 *      无侵入，基于 PLT-hook(iqiyi/xHook)，无需重编 native 库
 *      高性能，基于 Wechat-Backtrace 进行快速 unwind 堆栈，支持 aarch64 和 armeabi-v7a 架构
 *  8.Pthread Hook
 *      一个检测 Android Java 和 native 线程泄漏的工具
 *      无侵入，基于 PLT-hook(iqiyi/xHook)，无需重编 native 库
 *  9.Backtrace Component
 *      基于 DWARF 以及 ARM 异常处理数据进行简化并生成全新的 quicken unwind tables 数据，用于实现可快速回溯 native 调用栈的 backtrace 组件。回溯速度约是 libunwindstack 的 15x ~ 30x 左右。
 */
public class MatrixUtils {

}
