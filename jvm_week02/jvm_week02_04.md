### jvm_week02_04

1. #### Serial GC

   1）串行GC对年轻代使用mark-copy（标记-复制），对老年代使用mark-sweep-compact（标记-清除-整理）算法；

   2）Serial GC是单线程的垃圾收集器，不能进行并行处理，所有的GC操作都会触发全线暂停（STW），停止掉所有的业务线程。

   3）这种GC算法的弊端是不能充分利用多核CPU的并行处理能力，不管你的机器有多少个CPU内核，JVM在垃圾收集时都只能使用单个核心；但是其也有优点：就是CPU利用率高；

   4）Serial GC启动JVM的参数设置

   ```java
   java -XX:+UseSerialGC -Xms512m -Xmx512m -Xloggc:gc.demo.log -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis
   ```

   5）日志分析

   ```
   Java HotSpot(TM) 64-Bit Server VM (25.291-b10) for windows-amd64 JRE (1.8.0_291-b10), built on Apr  9 2021 00:02:00 by "java_re" with MS VC++ 15.9 (VS2017)
   Memory: 4k page, physical 16608320k(6743980k free), swap 26045504k(11176620k free)
   CommandLine flags:
   -XX:InitialHeapSize=536870912 -XX:MaxHeapSize=536870912
   -XX:+PrintGC -XX:+PrintGCDateStamps -XX:+PrintGCDetails
   -XX:+PrintGCTimeStamps -XX:+UseCompressedClassPointers
   -XX:+UseCompressedOops -XX:-UseLargePagesIndividualAllocation
   -XX:+UseSerialGC
   ```

   （1）第一行展示的是JVM JRE等的版本信息

   （2）CommandLine flags: 表示设置的JVM启动参数与JVMGC类型参数

   （3）Minor GC日志分析

   ```
   2021-07-01T10:50:24.504+0800: 0.203:
   [GC (Allocation Failure) 2021-07-01T10:50:24.505+0800: 0.204:
       [DefNew: 139776K->17471K(157248K), 0.0235219 secs]
       139776K->51818K(506816K), 0.0247750 secs]
       [Times: user=0.02 sys=0.01, real=0.02 secs]
   ```

   ​	**[1] 2021-07-01T10:50:24.504+0800**：---指GC时间开始的时间点，其中0800表示当前时区为东八区，这只是一个标识，方便我们直观判断GC发生的时间点

   ​	**[2] 0.203**：---表示GC事件相对于JVM启动时间的间隔，单位是秒

   ​	**[3] GC**：---用来**区分Minor GC还是Full GC的标志**，GC表明这是一次小型GC（Minor GC），即年轻代GC；

   ​	**[4] Allocation Failure**：---表示触发GC的原因。本次GC事件，是由于对象分配失败，年轻代中没有空间来存放新生成的对象引起的；

   ​	**[5] DefNew**：---表示垃圾收集器的名称，这个名称表示：**年轻代**使用的是**单线程**、**标记、复制、STW垃圾收集器**。**139776K->17471K**：---表示在垃圾收集之前和之后的**年轻代使用量**，**(157248K)**：---表示**年轻代的总空间**大小。进一步分析可知：GC之后年轻代使用率为11%

   ​	**[6] 139776K->51818K(506816K)**：---表示在垃圾收集之前和之后整个堆内存的使用情况，**(506816K)**则表示**堆内存可用**的空间大小。进一步分析可知：GC之后堆内存使用量为9%；**0.0235219 secs**：---GC事件持续的时间，以秒为单位；

   ​	**【7】[Times: user=0.02 sys=0.01, real=0.02 secs]**：---此次GC事件的持续时间，通过三个部分来衡量，**user**：---部分表示所有GC线程消耗的CPU时间；**sys**：---表示系统调用和系统等待事件消耗的时间；**real**：---则表示应用程序暂停的时间，因为串行垃圾收集器（Serial Garbage Collection）**只使用单个线程**，所以这里**real = user + system**，0.03秒也就是30毫秒

   （4）Full GC日志分析

   ```
   [Full GC (Allocation Failure) 2021-07-01T10:50:25.192+0800: 0.891:
       [Tenured: 349192K->336140K(349568K), 0.0438532 secs]
       506026K->336140K(506816K), [Metaspace: 2638K->2638K(1056768K)], 0.0439250 secs]
   [Times: user=0.05 sys=0.00, real=0.04 secs]
   ```

   ​	【1】**2021-07-01T10:50:25.192+0800**：---GC事件开始的时间

   ​	【2】**Tenured**：用于清理老年代空间的垃圾收集器名称。**Tenured表明使用的是单线程的STW垃圾收集**器，使用的算法为**标记-清楚-整理（mark-sweep-compact）**

   ​	**349192K->336140K(349568K)**：---表示GC前后老年代的使用量，以及老年代的空间大小；0.0438532 secs：---是清理老年代所花的时间

   ​	【3】**506026K->336140K(506816K)**：---表示在GC前后整个堆内存部分的使用情况，以及可用的堆空间大小。

   ​	【4】[Metaspace: 2638K->2638K(1056768K)], 0.0439250 secs]：---Metaspace空间的变化情况。可以看出，此次GC过程中Metaspace也没有什么变化

   ​	【5】[Times: user=0.05 sys=0.00, real=0.04 secs]：---GC事件的持续时间，分别为user，sys，real三个部分。因为串行垃圾收集器只使用单个线程，因此real = user + system。50毫秒的暂停时时间，比起前面年轻代的GC来说增加了一倍左右，**这个是因为GC时间，与GC后存活对象的总数量关系最大。**

   6）总结

   FullGC，我们主要关注GC之后内存使用量是否下降，其次关注暂停时间。GC后堆内存的使用量为220MB左右，耗时 50ms。若是内存进一步增大，导致系统暂停时间更长，对系统性能就会有较大影响。



2. #### Parallel GC

   1）并行GC在**年轻代和老年代**的垃圾回收**都会触发STW事件**；

   2）在**年轻代**使用的是 **标记-复制（mark-copy）**算法，在**老年代**使用的是 **标记-清除-整理（mark-sweep-compact）**算法；

   3）并行垃圾收集器适用于**多核服务器**，主要目标是**增加系统的吞吐量**，因为对系统资源的有效性，能达到更高的吞吐量；

   4）在GC期间，所有的CPU内核在**并行的清理垃圾**，所以总暂停时间更短

   5）在两次GC周期的间隔期，没有GC线程在运行，不会消耗任何系统资源；

   6）由于**并行GC所有的阶段都不能中断**，所以并行GC**很可能会出现长时间的卡顿**。长时间的卡顿的意思是指：并行GC启动后，一次性完成所有的GC操作，所以单次暂停的时间较长，假设系统延迟是非常重要的性能指标，那么就应该选择其他的垃圾收集器；

   7）并行垃圾收集器启动参数

   ```
   java -XX:+UseParallelGC -Xms512m -Xmx512m -Xloggc:gc.demo.log -XX:+PrintDetails -XX:+PrintGCDateStamps GCLogAnalysis
   ```

   8）Minor GC日志分析

   ```
   2021-07-01T22:18:39.046+0800: 0.214: 
   	[GC (Allocation Failure) 
   		[PSYoungGen: 131584K->21490K(153088K)] 
   		131584K->41650K(502784K), 0.0432836 secs] 
     	[Times: user=0.02 sys=0.14, real=0.04 secs] 
   ```

   ​	**【1】2021-07-01T22:18:39.046+0800: 0.214**: ---GC事件开始的时间；

   ​	**【2】GC** ---用来区分Minor GC还是 Full GC的标志，这里是一次 小型GC（Minor GC）；	

   ​	**【3】PSYoungGen** ---垃圾收集器的名称，这个名字表示的是在年轻代中使用的：并行的 标记-复制（mark-copy） ，全线暂停（STW）垃圾收集器。**131584K->21490K(153088K)** ---表示GC前后年轻代使用量，以及年轻代的总大小，简单计算**GC后的年轻代使用率**：21490K / 153088K = 14%；

   ​	**【4】131584K->41650K(502784K)** ---表示GC前后整个堆内存的使用量，以及此时可用堆的总大小，**GC后堆内存使用率为** ：41650K / 502784K = 8%;

   ​	**【5】[Times: user=0.02 sys=0.14, real=0.04 secs]** --- GC时间的持续时间，通过三个部分来衡量：user --- 表示GC所有线程锁消耗的总CPU时间，sys --- 表示操作系统调用和系统等待事件锁消耗的时间，real --- 表示应用程序实际暂停的时间，因为并不是所以的操作过程都能全部并行，所以在Parallel GC中，real =约等于 (user + system) /GC线程数。本人的机器是12核物理线程，所以默认是12个GC线程。分析这个时间，可以发现，如果使用串行GC，可能需要暂停30ms，但是使用并行GC只需要暂停（20 + 14）/12 = 2.8ms，实际上性能有了大幅度的提升。

   9）Full GC日志分析

   ```
   2021-07-01T22:18:39.322+0800: 0.454: 
   	[Full GC (Ergonomics) 
   		[PSYoungGen: 35433K->0K(116736K)] 
   		[ParOldGen: 292460K->232524K(349696K)] 
   		327893K->232524K(466432K), 
   		[Metaspace: 2703K->2703K(1056768K)], 0.0276027 secs] 
   	[Times: user=0.16 sys=0.02, real=0.03 secs] 
   ```

   ​	【1】**2021-07-01T22:18:39.322+0800: 0.454:**  --- 表示GC事件的开始时间；

   ​	【2】**Full GC** --- 表示完全GC的意思，Full GC表示**本次清理垃圾年轻代和老年代**，**Ergonomics** --- 表示**触发GC的原因**，表示JVM内部环境认为此时可以进行一次垃圾收集；

   ​	【3】**[PSYoungGen: 35433K->0K(116736K)]**  --- 表示**清理年轻代的垃圾收集器名为PSYoungGen的STW收集器**，采用的是 标记-复制（mark-copy）算法。年轻代使用量从 35433K->0K，一般Full GC中年轻代的结果都是这样。

   ​	【4】**ParoldGen --- 用于清理老年代空间的垃圾收集器类型**，这是使用的是名为 **ParOldGen 的垃圾收集器，这是一款并行STW垃圾收集器**，算法为 标记-清除-整理（mark-sweep-compact）；292460K->232524K(349696K)] --- 在GC前后老年代内存的使用情况以及老年代空间的大小。简单计算一下，GC之前老年代使用率为 2922460K / 349696K =  83.6%，GC之后，老年代使用率为 232524K / 349696K= 66.5%，可以看出确实有一定比例的回收；

   ​	【5】**327893K->232524K(466432K)** --- 垃圾收集之前和之后堆内存的使用情况，以及可用堆内存的总容量。简单分析可知，GC之前堆内存使用率为：327893K / 466432K = 70.3%，GC之后堆内存使用率为：232524K / 466432K = 49.8%，可以看出GC回收之后，堆内存有了比例空间空余；

   ​	【6】**[Metaspace: 2703K->2703K(1056768K)]** --- 元数据区在老年代GC中没有发生任何变化，没有回收任何对象；

   ​	【7】**0.0276027 secs** --- GC事件持续时间，以秒为单位；

   ​	【8】**[Times: user=0.16 sys=0.02, real=0.03 secs]** --- GC事件的持续时间，GC时间的持续时间，通过三个部分来衡量：user --- 表示GC所有线程锁消耗的总CPU时间，sys --- 表示操作系统调用和系统等待事件锁消耗的时间，real --- 表示应用程序实际暂停的时间，因为并不是所以的操作过程都能全部并行，所以在Parallel GC中，real =约等于 (user + system) /GC线程数。本人的机器是12核物理线程，所以默认是12个GC线程。分析这个时间，可以发现，如果使用串行GC，可能需要暂停50ms，但是使用并行GC只需要暂停（160 + 20）/12 = 15ms，实际上性能有了大幅度的提升。



3. #### CMS GC

   1）CMS GC对**年轻代**采用**STW方式 标记-复制（mark-copy）算法**，对**老年代**主要使用**并发 标记-清除（mark-sweep）算法**；

   2）CMS GC的**设计目标**是**避免老年代垃圾收集时出现长时间的卡顿**，**主要通过两种手段**来达成此目标；

   ​	（1）不对老年代进行清理，而是使用空闲列表（free-lists）来管理内存空间的回收

   ​	（2）在mark-and -sweep（标记-清除）阶段的大部分工作应用线程一起并发执行，也就是说，在这些阶段并没有明显的应用线程暂停。但是值得注意的屙屎，它仍然会和应用线程争抢CPU；

   3）默认情况下，**CMS使用的并发线程数等于CPU核心线程数的1/4**；

   4）如果CPU服务器是多核CPU，并且主要调优目标是降低GC停顿所引起的系统延迟，那么使用CMS是个很明智的选择，进行老年代的并发回收时，可能会伴随着多次年轻代的Minor GC；

   5）CMS GC启动参数

   ```
   java -XX:+UseConcMarkSweepGC -Xms512m -Xmx512m -Xloggc:gc.demo.log -XX:PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis
   ```

   6）Minor GC日志分析

   ```
   2021-07-03T08:30:41.158+0800: 0.184: 
     [GC (Allocation Failure) 
       2021-07-03T08:30:41.158+0800: 0.184: 
         [ParNew: 139701K->17470K(157248K), 0.0091003 secs] 
           139701K->42445K(506816K), 0.0093387 secs] 
     [Times: user=0.00 sys=0.00, real=0.01 secs] 
   ```

   ​	（1）**2021-07-03T08:30:41.158+0800: 0.184**:  --- GC事件的开始时间；

   ​	（2）**GC (Allocation Failure)**  --- 用来区分Minor GC还是Full GC的标志。**GC**表明这是一次**小型GC：Allocation Failure** 表示触发GC的原因。本次GC事件，**是由于年轻代可用空间不足**，新对象的内存分配失败引起的。

   ​	（3）**[ParNew: 139701K->17470K(157248K), 0.0091003 secs]**  --- 其中**ParNew**是垃圾收集器的名称，对应的就是前面日志中打印的；**-XX:+UseParNew** 这个命令的而标致。表示在**年轻代**中使用的是： **并行的标记-复制（mark-copy）**垃圾收集器，**专门设计用来配合CMS垃圾收集器，因为CMS只负责回收老年代**。后面的数字表示GC前后年轻代的使用量变化**（139701K->17470K）**，以及年轻代的总大小**(157248K)**，0.0091003 secs。

   ​	（4）**139701K->42445K(506816K)**, 0.0093387 secs --- 表示GC前后**堆内存**的使用量变化（139701K->42445K），以及堆内存空间的大小（506816K）。消耗时间是 0.0093387 secs  ，和前面的ParNew部分的时间基本上一样。

   ​	（5）**[Times: user=0.00 sys=0.00, real=0.01 secs]** --- GC事件的持续时间，user是GC线程所消耗的总CPU时间；sys 是操作系统调用和系统等待事件消耗的时间；应用程序实际暂停的时间大致为 real = (user + sys)/GC线程数

   ​	（6）进一步计算和分析可以知道，在GC之前，年轻代使用量为 139701K / 157248K = 88.8%，堆内存的使用率为 139701K/506816K=27.5%。稍微估算一下，老年代的使用率为： （139701K -139701K）/（42445K-17470K）=0，这表明，第一次GC的时候并没有使用到老年代的内存；

   7）Full GC日志分析

   ​	CMS日志完全是一种不同的分割方式，并且很长，因为CMS对老年代进行垃圾收集时每个阶段都会有自己的日志；

   ​	（1）Initial Mark（初始标记）

   ​		这个阶段伴随着**STW暂停**。初始标记的目标是**标记所有的根对象**，包括GC ROOT 直接引用的对象，以及被年轻代中所有存活对象所引用的对象。后面这部分也非常重要，**因为老年代是独立运行回收的**；

   ```
   2021-07-03T08:30:41.363+0800: 0.388: 
     [GC (CMS Initial Mark) 
       [1 CMS-initial-mark: 206300K(349568K)] 
         226894K(506816K), 0.0007734 secs] 
     [Times: user=0.00 sys=0.00, real=0.00 secs] 
   ```

   ​	**【1】2021-07-03T08:30:41.363+0800: 0.388:** --- GC事件开始的时间；

   ​	**【2】CMS Initial Mark** --- 这个阶段的名称为 Initial Mark，会标记所有的GC Root；

   ​	**【3】[1 CMS-initial-mark: 206300K(349568K)]**  --- 表示**老年代**的使用量，以及老年代的空间大小；

   ​	 **【4】226894K(506816K), 0.0007734 secs]**  --- 表示当前堆内存的使用量，以及可用堆的大小，消耗的时间，可以看出这个时间非常端，只有0.7毫秒左右，因为要标记的这些Root数量很少；

   ​	**【5】[Times: user=0.00 sys=0.00, real=0.00 secs]** --- 初始标记事件暂停的时间，可以看到可以忽略不计；

   ​	（2）Concurrent Mark（并发标记）

   ```
   2021-07-03T08:30:41.364+0800: 0.389: 
     [CMS-concurrent-mark-start]
   2021-07-03T08:30:41.366+0800: 0.391: 
     [CMS-concurrent-mark: 0.002/0.002 secs] 
     [Times: user=0.00 sys=0.00, real=0.00 secs] 
   ```

   ​	**【1】[CMS-concurrent-mark-start]** --- 知名了CMS垃圾收集器所处的阶段为并发标记（Concurrent Mark）	

   ​	**【2】0.002/0.002 secs** --- 此阶段的持续实际爱你，分别是GC线程消耗的时间和实际消耗的时间[**Times: 		**

   ​	**【3】user=0.00 sys=0.00, real=0.00 secs]** --- Times对并发阶段来说这些时间并没有多少意义，因为是从并发标记开始的时刻计算的，而这段时间应用线程也在执行，所以这个时间只是一个大概的值；

   ​	（3）Concurrent Mark（并发标记）

   ```
   2021-07-03T08:30:41.364+0800: 0.389: 
     [CMS-concurrent-mark-start]
   2021-07-03T08:30:41.366+0800: 0.391: 
     [CMS-concurrent-mark: 0.002/0.002 secs] 
     [Times: user=0.00 sys=0.00, real=0.00 secs] 
   ```

   ​	**【1】[CMS-concurrent-mark-start]** --- 知名了CMS垃圾收集器所处的阶段为并发标记（Concurrent Mark）

   ​	**【2】0.002/0.002 secs** --- 此阶段的持续实际爱你，分别是GC线程消耗的时间和实际消耗的时间[**Times: **

   ​	**【3】user=0.00 sys=0.00, real=0.00 secs]** --- Times对并发阶段来说这些时间并没有多少意义，因为是从并发标记开始的时刻计算的，而这段时间应用线程也在执行，所以这个时间只是一个大概的值；

   （4）Concurrent Preclean（并发预清理）

   ```
   2021-07-03T08:30:41.366+0800: 0.391: 
     [CMS-concurrent-preclean-start]
   2021-07-03T08:30:41.366+0800: 0.391: 
     [CMS-concurrent-preclean: 0.001/0.001 secs] 
     [Times: user=0.00 sys=0.00, real=0.00 secs] 
   ```

   ​		**【1】[CMS-concurrent-preclean-start]** --- 表明这是并发预清理阶段的日志，这个阶段会统计前面的并发标记阶段执行过程中发生了改变的对象。

   ​	**【2】0.001/0.001 sec** --- 此阶段的持续时间，分别是GC线程运行时间和实际占用的时间**Times: user=0.00 **

   ​	**【3】sys=0.00, real=0.00 secs** --- Times 这部分对并发阶段来说没多少意义，因为是从开始时间计算的，而这段时间内不仅GC线程在执行并发预清理，应用线程也在运行；

   ​	（5）Concurrent Abortable Preclean（可取消的并发预清理）

   ​	此阶段也不停止应用线程，尝试在会触发STW的Final Remark阶段开始之前，尽可能多干一些活。本阶段的具体时间取决于多种因素，因为它循环做同样的而是情，直到满足某一个退出条件（如迭代次数，有用工作量，消耗的系统时间等等）；

   ```
   2021-07-03T08:30:41.366+0800: 0.391: 
     [CMS-concurrent-abortable-preclean-start]
   2021-07-03T08:30:41.537+0800: 0.563: 
     [CMS-concurrent-abortable-preclean: 0.003/0.172 secs] 
     [Times: user=1.06 sys=0.09, real=0.17 secs] 
   ```

   ​	**【1】CMS-concurrent-abortable-preclean-start** --- 指示此阶段的名称：“Concurrent Abortable Preclean”；

   ​	**【2】0.003/0.172 secs** --- 此阶段GC线程的运行时间和实际占用的时间。从本质上讲，GC线程试图在执行STW暂停之前等待尽可能长的时间。默认条件下，此阶段可以持续最长5秒钟的时间；**Times: user=1.06 **

   **【3】sys=0.09, real=0.17 secs** --- Times对并发阶段来说这些时间并没有多少意义，因为是从并发标记开始的时刻计算的，而这段时间应用线程也在执行，所以这个时间只是一个大概的值

   此阶段完成的工作可能对STW停顿的时间有较大的影响，并且有许多重要的配置选项和失败模式；

   ​	（6）Final Remark（最终标记）

   ​	最终标记阶段是此次GC时间中**第二次（也是最后一次）STW停顿**

   ​	本阶段的**目标**是完成老年代中所有存活对象的标记。因为之前的预清理阶段是并发之情形的，有可能GC线程跟不上应用程序的修改速度。所以需要一次STW暂停来处理各种复杂的情况；

   ​	通常CMS会尝试在年轻代尽可能空的情况下执行final remark阶段，以免连续触发多次STW事件；

   ```
   [GC (CMS Final Remark) 
     [YG occupancy: 21267 K (157248 K)]
     2021-07-03T08:30:41.538+0800: 0.563: 
       [Rescan (parallel) , 0.0006883 secs]
     2021-07-03T08:30:41.538+0800: 0.564: 
       [weak refs processing, 0.0000182 secs]
     2021-07-03T08:30:41.538+0800: 0.564: 
       [class unloading, 0.0002224 secs]
     2021-07-03T08:30:41.539+0800: 0.564: 
       [scrub symbol table, 0.0003575 secs]
     2021-07-03T08:30:41.539+0800: 0.564: 
       [scrub string table, 0.0001287 secs]
       [1 CMS-remark: 340496K(349568K)] 
      361764K(506816K), 0.0015307 secs] 
     [Times: user=0.00 sys=0.00, real=0.00 secs] 
   ```

   ​	**CMS Final Remark** --- 这是此阶段的名称，最终标记阶段，会标记老年代中所有存活的对象，包括此前并发标记过程中创建/修改的引用；

   ​	**YG occupancy: 21267 K (157248 K)** --- 当前年轻代使用量与总容量

   ​	**Rescan (parallel) , 0.0006883 secs** --- 在程序暂停后进行重新扫描（Rescan），以完成存活对象的标记，这部分是并行执行的，消耗的时间为 0.0006883 secs

   ​	**weak refs processing, 0.0000182 secs** --- 第一个子阶段：处理弱引用的持续时间

   ​	**class unloading, 0.0002224 secs** --- 第二个子阶段；卸载不使用的类，以及持续的时间；

   ​	**scrub symbol table, 0.0003575 secs** --- 第三个子阶段：清理符号表，即持有class级别metadata的符号表（symbol tables）；

   ​	**scrub string table, 0.0001287 secs** ---  第四个子阶段： 清理内联字符串对应的 string tables

   ​	**1 CMS-remark: 340496K(349568K)** --- 此阶段完成后老年代的使用量和总容量

   ​	**361764K(506816K), 0.0015307 secs** --- 此阶段完成后，整个堆内存的使用量和总容量

   （7）Concurrent Sweep（并发清除）

   ​	此阶段与应用程序并发执行，不需要STW停顿。目的是删除不再使用的对象，并回收他们所占用的内存空间；

   （8）Concurrent Reset（并发重置）

   ​	此阶段与应用程序线程并发执行，重置CMS算法相关的内部数据结构，下一次触发就可以直接使用；

   7）总结

   ​	总之，CMS垃圾收集器在**减少停顿时间上做了很多给力的工作**，**很大一部分GC线程是与应用线程并发运行的，不需要暂停应用线程**，这样可以在一般情况下每次暂停的时候较少。当然，CMS也有一些缺点，其中最大的问题就是老年代的内存碎片问题，在某些情况下GC会有不可预测的暂停时间，特别是堆内存较大的情况下。

​	

4. #### G1 GC

   1）G1 GC设计最主要的目标是：将STW停顿的时间和分布，变成可预期且可配置；

   2）G1 GC是一款实时垃圾收集器，可以为期设置某些特定的性能指标，为了达成可预期停顿的时间指标；

   3）G1 GC **堆不再分成年轻代和老年代**，而是划分为多个（通常是2048个）可以存放对象的小块堆区域(smaller heap regions)。每个小块，可能一会被定义成 Eden 区，一会被指定为 Survivor区或者Old 区。在逻辑上，所有的 Eden 区和 Survivor 区合起来就是年轻代，所有的 Old 区拼在一起那就是老年代。

   ![img](https://uploader.shimo.im/f/Cvn6dIcipOFqS9wO.png!thumbnail?accessToken=eyJhbGciOiJIUzI1NiIsImtpZCI6ImRlZmF1bHQiLCJ0eXAiOiJKV1QifQ.eyJhdWQiOiJhY2Nlc3NfcmVzb3VyY2UiLCJleHAiOjE2MjUzNTcyOTYsImciOiJIWEhReDZyY2M5UlBXd0pYIiwiaWF0IjoxNjI1MzU2OTk2LCJ1c2VySWQiOjY3MzkzOTAzfQ.AqIF_nmskh-k7HyuLyL2jD98bONuyVQy9Sni6RjTYrI)

   4）这样划分之后，使得 G1 不必每次都去收集整个堆空间，而是以**增量的方式**来进行处理: 每次只处理一部分内存块，称为此次 GC 的回收集(collection set)。每次 GC 暂停都会收集所有年轻代的内存块，但一般只包含部分老年代的内存块。

   5）G1 的另一项创新是，**在并发阶段估算每个小堆块存活对象的总数**。构建回收集的原则是： 垃圾最多的小块会被优先收集。这也是 G1 名称的由来。

   6）**G1 GC的处理步骤**

   （1）年轻代模式转移暂停（Evacuation Pause）

   ​	G1 GC 会通过前面一段时间的运行情况来不断的调整自己的回收策略和行为，以此来比较稳定地控制暂停时间。在应用程序刚启动时，G1 还没有采集到什么足够的信息，这时候就处于初始的 fully-young模式。当年轻代空间用满后，应用线程会被暂停，年轻代内存块中的存活对象被拷贝到存活区。如果还没有存活区，则任意选择一部分空闲的内存块作为存活区。

   ​	拷贝的过程称为转移（Evacuation)，这和前面介绍的其他年轻代收集器是一样的工作原理 ；

   ​	（2）并发标记（Concurrent Marking）  

   ​	同时我们也可以看到，G1 GC 的很多概念建立在 CMS 的基础上，所以下面的内容需要对 CMS 有一定的理解。 

   ​	 G1 并发标记的过程与 CMS 基本上是一样的。G1 的并发标记通过 Snapshot-At-The-Beginning（起始快照）的方式，在标记阶段开始时记下所有的存活对象。即使在标记的同时又有一些变成了垃圾。通过对象的存活信息，可以构建出每个小堆块的存活状态，以便回收集能高效地进行选择。  

   ​	这些信息在接下来的阶段会用来执行老年代区域的垃圾收集。 

   ​	有两种情况是可以完全并发执行的：  

   ​	  一、如果在标记阶段确定某个小堆块中没有存活对象，只包含垃圾；   

   ​	 二、在 STW 转移暂停期间，同时包含垃圾和存活对象的老年代小堆块。  当堆内存的总体使用比例达到一定数值，就会触发并发标记。这个默认比例是 45%，但也可以通过 JVM参数InitiatingHeapOccupancyPercent 来设置。和 CMS 一样，G1 的并发标记也是由多个阶段组成，其中一些阶段是完全并发的，还有一些阶段则会暂停应用线程；    

   ​	（3）转移暂停: 混合模式（Evacuation Pause (mixed)）    

   ​		并发标记完成之后，G1将执行一次混合收集（mixed collection），就是不只清理年轻代，还将一部分老年代区域也加入到回收集中。混合模式的转移暂停不一定紧跟并发标记阶段。有很多规则和历史数据会影响混合模式的启动时机。比如，假若在老年代中可以并发地腾出很多的小堆块，就没有必要启动混合模式。

   ​	因此，在并发标记与混合转移暂停之间，很可能会存在多次 young 模式的转移暂停。

   

   7）G1 GC启动参数设置

   ```
   java -XX:+UseG1GC -Xms512m -Xmx512m -Xloggc:gc.demo.log -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis
   ```

   8）日志分析

   （1）Evacuation Pause: young(纯年轻代模式转移暂停)

   ```
   2019-12-23T01:45:40.605-0800: 0.181:
     [GC pause (G1 Evacuation Pause) (young)，0.0038577 secs]
       [Parallel Time: 3.1 ms，GC Workers: 8]
         ...... worker线程的详情，下面单独讲解
       [Code Root Fixup: 0.0 ms]
       [Code Root Purge: 0.0 ms]
       [Clear CT: 0.2 ms]
       [Other: 0.6 ms]
         ...... 其他琐碎任务，下面单独讲解
       [Eden: 25.0M(25.0M)->0.0B(25.0M)
       Survivors: 0.0B->4096.0K Heap: 28.2M(512.0M)->9162.7K(512.0M)]
   [Times: user=0.01 sys=0.01，real=0.00 secs]
   ```

   ​	**当年轻代空间用满后，应用线程会被暂停**，年轻代内存块中的存活对象被拷贝到存活区。如果还没有存活区，则任意选择一部分空闲的内存块作为存活区；

   ​	拷贝的过程成为转移（Evacuation），这和前面介绍的其他年轻代收集器时一样的工作原理；

   ​	**[GC pause (G1 Evacuation Pause) (young)**，0.0038577 secs] --- G1转移暂停，纯年轻代模式，只清理年轻代空间。这次暂停在JVM启动之后182ms，持续的系统时间为3.8ms；

   ​	**Parallel Time: 3.1 ms，GC Workers: 8** --- 表明后面的活动是由8个Worker线程并行执行，消耗时间为3.1毫秒（real time），worker是一种模式，类似于一个老板指挥多个工人干活；

   ​	**Code Root Fixup: 0.0 ms** --- 释放用于管理并行活动的内部数据，一般都接近于零。这个过程是串行执行的；

   ​	**Code Root Purge: 0.0 ms** --- 清理其他部分数据，也是非常快的，如非必要基本上等于零。也是串行执行的过程。

   ​	**[Other: 0.6 ms]** – 其他活动消耗的时间，其中大部分是并行执行的；

   ​	**[Eden: 25.0M(25.0M)->0.0B(25.0M)** – 暂停之前和暂停之后，Eden 区的使用量/总容量。

   ​	**Survivors: 0.0B->4096.0K** – GC暂停前后，存活区的使用量。Heap:28.2M(512.0M)->9162.7K(512.0M)] – 暂停前后，整个堆内存的使用量与总容量。