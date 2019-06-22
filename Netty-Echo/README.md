
**SimpleChannelInboundHandler vs. ChannelInboundHandler**

何时用这两个要看具体业务的需要。在客户端，当 channelRead0() 完成，我们已经拿到的入站的信息。
当方法返回时，SimpleChannelInboundHandler 会小心的释放对 ByteBuf（保存信息） 的引用。
而在 EchoServerHandler,我们需要将入站的信息返回给发送者，由于 write() 是异步的，在 channelRead() 返回时，
可能还没有完成。所以，我们使用 ChannelInboundHandlerAdapter,无需释放信息。最后在 channelReadComplete() 我们调用 ctxWriteAndFlush() 
来释放信息。  

### Netty 快速入门
下面枚举所有的 Netty 应用程序的基本构建模块，包括客户端和服务器。

Bootstrap  
Netty 应用程序通过设置 bootstrap（引导）类的开始，该类提供了一个 用于应用程序网络层配置的容器。  
Channel  
底层网络传输 API 必须提供给应用 I/O操作的接口，如读，写，连接，绑定等等。对于我们来说，这是结构几乎总是会成为一个“socket”。 Netty 中的接口 Channel 定义了与 socket 丰富交互的操作集：bind, close, config, connect, isActive, isOpen, isWritable, read, write 等等。 Netty 提供大量的 Channel 实现来专门使用。这些包括 AbstractChannel，AbstractNioByteChannel，AbstractNioChannel，EmbeddedChannel， LocalServerChannel，NioSocketChannel 等等。  
ChannelHandler  
ChannelHandler 支持很多协议，并且提供用于数据处理的容器。我们已经知道 ChannelHandler 由特定事件触发。 ChannelHandler 可专用于几乎所有的动作，包括将一个对象转为字节（或相反），执行过程中抛出的异常处理。  
常用的一个接口是 ChannelInboundHandler，这个类型接收到入站事件（包括接收到的数据）可以处理应用程序逻辑。当你需要提供响应时，你也可以从 ChannelInboundHandler 冲刷数据。一句话，业务逻辑经常存活于一个或者多个 ChannelInboundHandler。  
ChannelPipeline  
ChannelPipeline 提供了一个容器给 ChannelHandler 链并提供了一个API 用于管理沿着链入站和出站事件的流动。每个 Channel 都有自己的ChannelPipeline，当 Channel 创建时自动创建的。 ChannelHandler 是如何安装在 ChannelPipeline？ 主要是实现了ChannelHandler 的抽象 ChannelInitializer。ChannelInitializer子类 通过 ServerBootstrap 进行注册。当它的方法 initChannel() 被调用时，这个对象将安装自定义的 ChannelHandler 集到 pipeline。当这个操作完成时，ChannelInitializer 子类则 从 ChannelPipeline 自动删除自身。  
EVENTLOOP  
EventLoop 用于处理 Channel 的 I/O 操作。一个单一的 EventLoop通常会处理多个 Channel 事件。一个 EventLoopGroup 可以含有多于一个的 EventLoop 和 提供了一种迭代用于检索清单中的下一个。  
CHANNELFUTURE  
Netty 所有的 I/O 操作都是异步。因为一个操作可能无法立即返回，我们需要有一种方法在以后确定它的结果。出于这个目的，Netty 提供了接口 ChannelFuture,它的 addListener 方法注册了一个 ChannelFutureListener ，当操作完成时，可以被通知（不管成功与否）。  

### Channel、Event和I/0
Netty 是一个非阻塞、事件驱动的网络框架。Netty 实际上是使用 Threads（多线程）处理 I/O 事件，对于熟悉多线程编程的读者可能会需要关注同步代码。这样的方式不好，因为同步会影响程序的性能，Netty 的设计保证程序处理事件不会有同步。图 Figure 3.1 展示了，你不需要在 Channel 之间共享 ChannelHandler 实例的原因：  
![](https://ws1.sinaimg.cn/large/006mOQRagy1fxvzod2o7sj30fz0dugma.jpg)   
该图显示，一个 EventLoopGroup 具有一个或多个 EventLoop。想象 EventLoop 作为一个 Thread 给 Channel 执行工作。 （事实上，一个 EventLoop 是势必为它的生命周期一个线程。）  
当创建一个 Channel，Netty 通过 一个单独的 EventLoop 实例来注册该 Channel（并同样是一个单独的 Thread）的通道的使用寿命。这就是为什么你的应用程序不需要同步 Netty 的 I/O操作;所有 Channel 的 I/O 始终用相同的线程来执行。  

### Bootstrapping  
Bootstrapping（引导） 是 Netty 中配置程序的过程，当你需要连接客户端或服务器绑定指定端口时需要使用 Bootstrapping  
如前面所述，Bootstrapping 有两种类型，一种是用于客户端的Bootstrap，一种是用于服务端的ServerBootstrap。不管程序使用哪种协议，无论是创建一个客户端还是服务器都需要使用“引导”。  

**面向连接 vs. 无连接**

请记住，这个讨论适用于 TCP 协议，它是“面向连接”的。这样协议保证该连接的端点之间的消息的有序输送。无连接协议发送的消息，无法保证顺序和成功性。  

分类 | Bootstrap | ServerBootstrap
 ---|---|--- 
网络功能 | 连接到远程主机和端口	| 绑定本地端口
EventLoopGroup 数量	| 1	| 2

Bootstrap用来连接远程主机，有1个EventLoopGroup  
ServerBootstrap用来绑定本地端口，有2个EventLoopGroup  

**差异**  
第一个差异很明显，“ServerBootstrap”监听在服务器监听一个端口轮询客户端的“Bootstrap”或DatagramChannel是否连接服务器。
通常需要调用“Bootstrap”类的connect()方法，但是也可以先调用bind()再调用connect()进行连接，之后使用的Channel包含在bind()返回的ChannelFuture中。

一个 ServerBootstrap 可以认为有2个 Channel 集合，第一个集合包含一个单例 ServerChannel，代表持有一个绑定了本地端口的 socket；
第二集合包含所有创建的 Channel，处理服务器所接收到的客户端进来的连接。下图形象的描述了这种情况：  
![](https://ws1.sinaimg.cn/large/006mOQRagy1fxw03vbc16j30jh0cq0uw.jpg)   
与 ServerChannel 相关 EventLoopGroup 分配一个 EventLoop 是 负责创建 Channels 用于传入的连接请求。一旦连接接受，第二个EventLoopGroup 分配一个 EventLoop 给它的 Channel。  

### ChannelPipeline和ChannelHandler
ChannelPipeline 是 ChannelHandler 链的容器。  
在许多方面的 ChannelHandler 是在您的应用程序的核心，尽管有时它 可能并不明显。ChannelHandler 支持广泛的用途，使它难以界定。因此，最好是把它当作一个通用的容器，处理进来的事件（包括数据）并且通过ChannelPipeline。ChannelInboundHandler 和 ChannelOutboundHandler 继承自父接口 ChannelHandler。  
Netty 中有两个方向的数据流，图3.4 显示的入站(ChannelInboundHandler)和出站(ChannelOutboundHandler)之间有一个明显的区别：若数据是从用户应用程序到远程主机则是“出站(outbound)”，相反若数据时从远程主机到用户应用程序则是“入站(inbound)”。  

为了使数据从一端到达另一端，一个或多个 ChannelHandler 将以某种方式操作数据。这些 ChannelHandler 会在程序的“引导”阶段被添加ChannelPipeline中，并且被添加的顺序将决定处理数据的顺序。  
![](https://ws1.sinaimg.cn/large/006mOQRagy1fxw0dknj2uj30k006dgml.jpg)   
## 核心功能
### Transport(传输)  
####  Transport API 
 Transport API 的核心是 Channel 接口，用于所有的出站操作，见下图  
 ![](https://ws1.sinaimg.cn/large/006mOQRagy1fxw134wx1jj30ji07st95.jpg)  
 
如上图所示，每个 Channel 都会分配一个 ChannelPipeline 和ChannelConfig。ChannelConfig 负责设置并存储 Channel 的配置，并允许在运行期间更新它们。传输一般有特定的配置设置，可能实现了 ChannelConfig. 的子类型。  
ChannelPipeline 容纳了使用的 ChannelHandler 实例，这些ChannelHandler 将处理通道传递的“入站”和“出站”数据以及事件。ChannelHandler 的实现允许你改变数据状态和传输数据。  
现在我们可以使用 ChannelHandler 做下面一些事情：  
* 传输数据时，将数据从一种格式转换到另一种格式  
* 异常通知
* Channel 变为 active（活动） 或 inactive（非活动） 时获得通知* Channel 被注册或注销时从 EventLoop 中获得通知
* 通知用户特定事件

 Channel | main methods
---|---
方法名称 |	描述
eventLoop() |	返回分配给Channel的EventLoop
pipeline() |	返回分配给Channel的ChannelPipeline
isActive() |	返回Channel是否激活，已激活说明与远程连接对等
localAddress() |	返回已绑定的本地SocketAddress
remoteAddress() |	返回已绑定的远程SocketAddress
write() |	写数据到远程客户端，数据通过ChannelPipeline传输过去
flush() |	刷新先前的数据
writeAndFlush(...) | 一个方便的方法用户调用write(...)而后调用 flush()

Channel 是线程安全(thread-safe)的，它可以被多个不同的线程安全的操作，在多线程环境下，所有的方法都是安全的。正因为 Channel 是安全的，我们存储对Channel的引用，并在学习的时候使用它写入数据到远程已连接的客户端，使用多线程也是如此。  

#### 包含的Transport 
Netty 自带了一些传输协议的实现，虽然没有支持所有的传输协议，但是其自带的已足够我们来使用。Netty应用程序的传输协议依赖于底层协议，本节我们将学习Netty中的传输协议。  
Netty中的传输方式有如下几种：  

方法名称 |	包 | 描述
---|---|---
NIO |	io.netty.channel.socket.nio	| 基于java.nio.channels的工具包，使用选择器作为基础的方法。
OIO |	io.netty.channel.socket.oio |	基于java.net的工具包，使用阻塞流。
Local |	io.netty.channel.local |	用来在虚拟机之间本地通信。
Embedded |	io.netty.channel.embedded |	嵌入传输，它允许在没有真正网络的传输中使用 ChannelHandler，可以非常有用的来测试ChannelHandler的实现。

### Buffer(缓冲)
正如我们先前所指出的，网络数据的基本单位永远是 byte(字节)。Java NIO 提供 ByteBuffer 作为字节的容器，但它的作用太有限，也没有进行优化。使用ByteBuffer通常是一件繁琐而又复杂的事。  
幸运的是，Netty提供了一个强大的缓冲实现类用来表示字节序列以及帮助你操作字节和自定义的POJO。这个新的缓冲类，ByteBuf,效率与JDK的ByteBuffer相当。设计ByteBuf是为了在Netty的pipeline中传输数据。它是为了解决ByteBuffer存在的一些问题以及满足网络程序开发者的需求，以提高他们的生产效率而被设计出来的。  
请注意，在本书剩下的章节中，为了帮助区分，我将使用数据容器指代Netty的缓冲接口及实现，同时仍然使用Java的缓冲API指代JDK的缓冲实现。  
在本章中，你将会学习Netty的缓冲API,为什么它能够超过JDK的实现，它是如何做到这一点，以及为什么它会比JDK的实现更加灵活。你将会深入了解到如何在Netty框架中访问被交换数据以及你能对它做些什么。这一章是之后章节的基础，因为几乎Netty框架的每一个地方都用到了缓冲。  
因为数据需要经过ChannelPipeline和ChannelHandler进行传输，而这又离不开缓冲，所以缓冲在Netty应用程序中是十分普遍的。  
#### Buffer API
主要包括

* ByteBuf
* ByteBufHolder

Netty 使用 reference-counting(引用计数)来判断何时可以释放 ByteBuf 或 ByteBufHolder 和其他相关资源，从而可以利用池和其他技巧来提高性能和降低内存的消耗。这一点上不需要开发人员做任何事情，但是在开发 Netty 应用程序时，尤其是使用 ByteBuf 和 ByteBufHolder 时，你应该尽可能早地释放池资源。 Netty 缓冲 API 提供了几个优势：  
* 可以自定义缓冲类型
* 通过一个内置的复合缓冲类型实现零拷贝
* 扩展性好，比如 StringBuilder
* 不需要调用 flip() 来切换读/写模式
* 读取和写入索引分开
* 方法链
* 引用计数
* Pooling(池)

#### ByteBuf - 字节数据的容器
因为所有的网络通信最终都是基于底层的字节流传输，因此一个高效、方便、易用的数据接口是必要的，而 Netty 的 ByteBuf 满足这些需求。  
ByteBuf 是一个很好的经过优化的数据容器，我们可以将字节数据有效的添加到 ByteBuf 中或从 ByteBuf 中获取数据。为了便于操作，ByteBuf 提供了两个索引：一个用于读，一个用于写。我们可以按顺序的读取数据，也可以通过调整读取数据的索引或者直接将读取位置索引作为参数传递给get方法来重复读取数据。  


### ChannelHandler和ChannelPipeline
#### Channel家族
##### Channel生命周期
Channel 有个简单但强大的状态模型，与 ChannelInboundHandler API 密切相关。下面表格是 Channel 的四个状态  

状态 |	描述
---|---
channelUnregistered	| channel已创建但未注册到一个 EventLoop.
channelRegistered | channel 注册到一个 EventLoop.
channelActive | channel 变为活跃状态(连接到了远程主机)，现在可以接收和发送数据了
channelInactive	| channel 处于非活跃状态，没有连接到远程主机

Channel 的正常的生命周期如下图，当状态出现变化，就会触发对应的事件，这样就能与 ChannelPipeline 中的 ChannelHandler进行及时的交互。  
![](https://ws1.sinaimg.cn/large/006mOQRagy1fxwtfj1bvrj30gt08e3z2.jpg)  
#### ChannelHandler的生命周期
ChannelHandler 定义的生命周期操作如下表，当 ChannelHandler 添加到 ChannelPipeline，或者从 ChannelPipeline 移除后，对应的方法将会被调用。每个方法都传入了一个 ChannelHandlerContext 参数  

类型 | 	描述
---|---
handlerAdded |	当 ChannelHandler 添加到 ChannelPipeline 调用
handlerRemoved |	当 ChannelHandler 从 ChannelPipeline 移除时调用
exceptionCaught |	当 ChannelPipeline 执行抛出异常时调用

#### ChannelPipeline
ChannelPipeline 是一系列的ChannelHandler 实例,用于拦截 流经一个 Channel 的入站和出站事件,ChannelPipeline允许用户自己定义对入站/出站事件的处理逻辑，以及pipeline里的各个Handler之间的交互    

##### 修改ChannelPipeline
ChannelHandler 可以实时修改 ChannelPipeline 的布局，通过添加、移除、替换其他 ChannelHandler（也可以从 ChannelPipeline 移除 ChannelHandler 自身）。这个 是 ChannelHandler 重要的功能之一。  
Table 6.6 ChannelHandler methods for modifying a ChannelPipeline  

名称 |	描述
---|---
addFirst addBefore addAfter addLast	| 添加 ChannelHandler 到 ChannelPipeline.
Remove | 从 ChannelPipeline 移除 ChannelHandler.
Replace | 在 ChannelPipeline 替换另外一个 ChannelHandler

**总结：**
* 一个 ChannelPipeline 是用来保存关联到一个 Channel 的ChannelHandler
* 可以修改 ChannelPipeline 通过动态添加和删除 ChannelHandler
* ChannelPipeline 有着丰富的API调用动作来回应入站和出站事件。
  
#### ChannelHandlerContext
接口 ChannelHandlerContext 代表 ChannelHandler 和ChannelPipeline 之间的关联,并在 ChannelHandler 添加到 ChannelPipeline 时创建一个实例。ChannelHandlerContext 的主要功能是管理通过同一个 ChannelPipeline 关联的 ChannelHandler 之间的交互。  
ChannelHandlerContext 有许多方法,其中一些也出现在 Channel 和ChannelPipeline 本身。然而,如果您通过Channel 或ChannelPipeline 的实例来调用这些方法，他们就会在整个 pipeline中传播 。相比之下,一样的 的方法在 ChannelHandlerContext的实例上调用， 就只会从当前的 ChannelHandler 开始并传播到相关管道中的下一个有处理事件能力的 ChannelHandler 。  

因为 ChannelHandler 可以属于多个 ChannelPipeline ,它可以绑定多个 ChannelHandlerContext 实例。然而,ChannelHandler 用于这种用法必须添加 @Sharable 注解。否则,试图将它添加到多个 ChannelPipeline 将引发一个异常。此外,它必须既是线程安全的又能安全地使用多个同时的通道(比如,连接)。  

使用@Sharable的话，要确定 ChannelHandler 是线程安全的。   

### Codec框架
编写一个网络应用程序需要实现某种 codec (编解码器)，codec的作用就是将原始字节数据与目标程序数据格式进行互转。网络中都是以字节码的数据形式来传输数据的，codec 由两部分组成：decoder(解码器)和encoder(编码器)  
编码器和解码器一个字节序列转换为另一个业务对象。我们如何区分?  
想到一个“消息”是一个结构化的字节序列,语义为一个特定的应用程序——它的“数据”。encoder 是组件,转换消息格式适合传输(就像字节流),而相应的 decoder 转换传输数据回到程序的消息格式。逻辑上,“从”消息转换来是当作操作 outbound（出站）数据,而转换“到”消息是处理 inbound（入站）数据。  
我们看看 Netty 的提供的类实现的 codec 。  
解码器负责将消息从字节或其他序列形式转成指定的消息对象，编码器则相反；解码器负责处理“入站”数据，编码器负责处理“出站”数据。编码器和解码器的结构很简单，消息被编码后解码后会自动通过ReferenceCountUtil.release(message)释放，如果不想释放消息可以使用ReferenceCountUtil.retain(message)，这将会使引用数量增加而没有消息发布，大多数时候不需要这么做。  

#### 解码器
Netty 提供了丰富的解码器抽象基类，我们可以很容易的实现这些基类来自定义解码器。主要分两类：  
* 解码字节到消息（ByteToMessageDecoder 和 ReplayingDecoder）  
* 解码消息到消息（MessageToMessageDecoder）  

decoder 负责将“入站”数据从一种格式转换到另一种格式，Netty的解码器是一种 ChannelInboundHandler 的抽象实现。实践中使用解码器很简单，就是将入站数据转换格式后传递到 ChannelPipeline 中的下一个ChannelInboundHandler 进行处理；这样的处理是很灵活的，我们可以将解码器放在 ChannelPipeline 中，重用逻辑。  
**ByteToMessageDecoder**    
ByteToMessageDecoder 是用于将字节转为消息（或其他字节序列）。  
**ReplayingDecoder**
ReplayingDecoder 是 byte-to-message 解码的一种特殊的抽象基类，读取缓冲区的数据之前需要检查缓冲区是否有足够的字节，使用ReplayingDecoder就无需自己检查；若ByteBuf中有足够的字节，则会正常读取；若没有足够的字节则会停止解码。  
ByteToMessageDecoder 和 ReplayingDecoder    
注意到 ReplayingDecoder 继承自 ByteToMessageDecoder  
也正因为这样的包装使得 ReplayingDecoder 带有一定的局限性：  
* 不是所有的标准 ByteBuf 操作都被支持，如果调用一个不支持的操作会抛出 UnreplayableOperationException
* ReplayingDecoder 略慢于 ByteToMessageDecoder


#### 编码器
Encoder(编码器)  
回顾之前的定义，encoder 是用来把出站数据从一种格式转换到另外一种格式，因此它实现了 ChannelOutboundHandler。正如你所期望的一样，类似于 decoder，Netty 也提供了一组类来帮助你写 encoder，当然这些类提供的是与 decoder 相反的方法，如下所示：  
* 编码从消息到字节  
* 编码从消息到消息  

##### SPDY
SPDY（读作“SPeeDY”）是Google 开发的基于 TCP 的应用层协议，用以最小化网络延迟，提升网络速度，优化用户的网络使用体验。SPDY 并不是一种用于替代 HTTP 的协议，而是对 HTTP 协议的增强。SPDY 实现技术：  
* 压缩报头
* 加密所有
* 多路复用连接
* 提供支持不同的传输优先级

SPDY 主要有5个版本：
    
    1 - 初始化版本，但没有使用
    2 - 新特性，包含服务器推送
    3 - 新特性包含流控制和更新压缩
    3.1 - 会话层流程控制
    4.0 - 流量控制，并与 HTTP 2.0 更加集成

#### 空闲连接以及超时
检测空闲连接和超时是为了及时释放资源。常见的方法发送消息用于测试一个不活跃的连接来,通常称为“心跳”,到远端来确定它是否还活着。(一个更激进的方法是简单地断开那些指定的时间间隔的不活跃的连接)。  

### 引导 Bootstrap
#### Bootstrap 类型
Netty 的包括两种不同类型的引导。而不仅仅是当作的“服务器”和“客户”的引导，更有用的是考虑他们的目的是支持的应用程序功能。从这个意义上讲,“服务器”应用程序把一个“父”管道接受连接和创建“子”管道,而“客户端”很可能只需要一个单一的、非“父”对所有网络交互的管道（对于无连接的比如 UDP 协议也是一样）。
##### 客户端引导方法
下表是Bootstrap常用的方法  

名称 |	描述
---|---
group | 设置 EventLoopGroup 用于处理所有的 Channel 的事件
channel channelFactory | channel() 指定 Channel 的实现类。如果类没有提供一个默认的构造函数,你可以调用 channelFactory() 来指定一个工厂类被 bind() 调用。
localAddress | 指定应该绑定到本地地址 Channel。如果不提供,将由操作系统创建一个随机的。或者,您可以使用 bind() 或 connect()指定localAddress
option | 设置 ChannelOption 应用于 新创建 Channel 的 ChannelConfig。这些选项将被 bind 或 connect 设置在通道,这取决于哪个被首先调用。这个方法在创建管道后没有影响。所支持 ChannelOption 取决于使用的管道类型。请参考9.6节和 ChannelConfig 的 API 文档 的 Channel 类型使用。
attr | 这些选项将被 bind 或 connect 设置在通道,这取决于哪个被首先调用。这个方法在创建管道后没有影响。请参考9.6节。
handler | 设置添加到 ChannelPipeline 中的 ChannelHandler 接收事件通知。
clone | 创建一个当前 Bootstrap的克隆拥有原来相同的设置。
remoteAddress | 设置远程地址。此外,您可以通过 connect() 指定
connect | 连接到远端，返回一个 ChannelFuture, 用于通知连接操作完成
bind | 将通道绑定并返回一个 ChannelFuture,用于通知绑定操作完成后,必须调用 Channel.connect() 来建立连接。  

##### 引导服务器的方法
下表显示了 ServerBootstrap 的方法

名称 | 	描述
---|---
group |	设置 EventLoopGroup 用于 ServerBootstrap。这个 EventLoopGroup 提供 ServerChannel 的 I/O 并且接收 Channel
channel channelFactory |	channel() 指定 Channel 的实现类。如果管道没有提供一个默认的构造函数,你可以提供一个 ChannelFactory。
localAddress |	指定 ServerChannel 实例化的类。如果不提供,将由操作系统创建一个随机的。或者,您可以使用 bind() 或 connect()指定localAddress
option |	指定一个 ChannelOption 来用于新创建的 ServerChannel 的 ChannelConfig 。这些选项将被设置在管道的 bind() 或 connect(),这取决于谁首先被调用。在此调用这些方法之后设置或更改 ChannelOption 是无效的。所支持 ChannelOption 取决于使用的管道类型。请参考9.6节和 ChannelConfig 的 API 文档 的 Channel 类型使用。
childOption |	当管道已被接受，指定一个 ChannelOption 应用于 Channel 的 ChannelConfig。
attr |	指定 ServerChannel 的属性。这些属性可以被 管道的 bind() 设置。当调用 bind() 之后，修改它们不会生效。
childAttr |	应用属性到接收到的管道上。后续调用没有效果。
handler |	设置添加到 ServerChannel 的 ChannelPipeline 中的 ChannelHandler。 具体详见 childHandler() 描述
childHandler |	设置添加到接收到的 Channel 的 ChannelPipeline 中的 ChannelHandler。handler() 和 childHandler()之间的区别是前者是接收和处理ServerChannel，同时 childHandler() 添加处理器用于处理和接收 Channel。后者代表一个套接字绑定到一个远端。
clone |	克隆 ServerBootstrap 用于连接到不同的远端，通过设置相同的原始 ServerBoostrap。
bind |	绑定 ServerChannel 并且返回一个 ChannelFuture,用于 通知连接操作完成了（结果可以是成功或者失败）



