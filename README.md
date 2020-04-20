## 执行
```
mvn package
java -jar server/target/server-1.0-SNAPSHOT-jar-with-dependencies.jar
java -jar client/target/client-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## 作业

### 作业1 实现Json Rpc请求 Serializer (完成)
使用fastjson库

### 作业2 扩展功能 : 单service支持多个方法(完成) , 
反射获得方法名数组,遍历生成模板

### 作业3 方法支持多个参数(完成)  
java如何获取方法的参数列表?  method.getParameters();
遍历参数列表生成字符串  
代理类里的参数序列化后是什么?   
目前是把string参数序列化成字节数组 , 然后在server的handler里parse出arg
考虑使用参数数组[   
 ['name'=>123,'value'=>'sd','type=>'asd']  , 不支持复杂数据类型(还没实现具体的Serializer)
 ], 
 序列化成json数组, server parse出多个args 
 
  定义Argument  name ,value,type (name可以不用) 完成
  在模板里向RpcRequest里传入Argument[]  完成
  RpcRequest序列化成json , 传到server 完成 
  server端handler变化的地方: 遍历json字符串数组 , 生成每个参数对应的参数类 , 传入方法中  


### 作业4 支持多种类型

### 作业5 支持多种返回值

### 作业6 实现JDBC协议的NameService

实现一个支持JDBC协议 数据库的注册中心 (调用 RpcAccessPoint.getNameService() 方法，获取注册中心实例时，传入的参数就是 JDBC 的 URL，比如：“jdbc:mysql://127.0.0.1/mydb”;
                       不能修改 RPC 框架的源代码 ;
                       实现必须具有通用性，可以支持任意一种 JDBC 数据库。) 

### 作业7 完整文字描述整个执行过程 , 描述每个类的职责

### 作业8 画结构图

### 作业9 用其他语言实现一遍这个框架

### 作业10 描述一下动态代理的设计思想

### 作业11 singleton单例的注解实现 (@Singleton 的注解和获取单例的实现在 ServiceSupport)
                      
### 作业12 理解请求分发 / eventloop / reactor 
```在 RPC 框架的服务端处理客户端请求的业务逻辑中，我们分两层做了两次请求分发：
   
   在 RequestInvocation 类中，根据请求命令中的请求类型 (command.getHeader().getType())，分发到对应的请求处理器 RequestHandler 中；
   RpcRequestHandler 类中，根据 RPC 请求中的服务名，把 RPC 请求分发到对应的服务实现类的实例中去。
   ```
 
 
 
 
## 笔记

apple(win)键 + alt + b 可以从接口类跳转到实现类


java 客户端可通过实例化一个HelloService , 直接调用java服务端的方法


服务端实现HelloService , 提供服务

只支持java单语言之间的调用 ,  如果要支持跨语言调用 , 需要由IDL来生成Stub

和消息队列的共同点: 高性能网络传输 , 序列化,反序列化, 服务发现


### 底层执行原理

客户端实例化helloService ,  实例化出一个代理类,也称为Stub , 这个Stub代替客户端执行远程请求

这个作业中的Stub在编译阶段生成 ,  有的是在运行时动态生成


代理类代替发出请求,  带上方法名和所有参数

服务端解析请求 , 找到对应的方法 , 传入参数进行调用 , 

得到结果后,通过服务端的Stub代替返回远程请求



假设有多个服务端提供服务 , 客户端不知道该请求哪个地址 ,  因此还需要实现一个NamingService , 相当于注册中心 , 提供的功能 : 服务端注册 , 客户端获取服务(地址)



### 项目结构

RPC框架门面类 , RPCAccessPoint , 唯一对外提供接口的类 , 目前仅有 注册实例和获取实例 , 和一个startServer启动服务端常驻进程

NameService , 注册服务和查询服务地址



### 使用方法

双方遵从同一个接口约定 , 比如
```
public interface HelloService {
    String hello(String name);
}
```

```
//查找到服务的远程地址
URI uri = nameService.lookupService(serviceName);
//获取代理类(Stub)
HelloService helloService = rpcAccessPoint.getRemoteService(uri, HelloService.class);
//执行远程请求 ,像本地服务一样调用 ( 最终目的)
String response = helloService.hello(name);
logger.info(" 收到响应: {}.", response);



服务端实现接口
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String name) {
        String ret = "Hello, " + name;
        return ret;
    }
}


//启动服务监听
rpcAccessPoint.startServer();
//注册接口具体实现
URI uri = rpcAccessPoint.addServiceProvider(helloService, HelloService.class);
//注册服务 , 提供客户端发现
nameService.registerService(serviceName, uri);



//序列化相关的门面类 ,  存放着多个 Serializer , 不同对象类型的序列化实现不一样 , 有两个map获取对应的实现类
public class SerializeSupport {
private static Map<Class<?>/* 序列化对象类型 */, Serializer<?>/* 序列化实现 */> serializerMap = new HashMap<>();
    private static Map<Byte/* 序列化实现类型 */, Class<?>/* 序列化对象类型 */> typeMap = new HashMap<>();
    public static  <E> E parse(byte [] buffer) {
        // ...
    }
    public static <E> byte [] serialize(E  entry) {
        // ...
    }
}


// 序列化
MyClass myClassObject = new MyClass();
byte [] bytes = SerializeSupport.serialize(myClassObject);
// 反序列化
MyClass myClassObject1 = SerializeSupport.parse(bytes);



//Serializer接口

public interface Serializer<T> {
    /**
     * 计算对象序列化后的长度，主要用于申请存放序列化数据的字节数组
     * @param entry 待序列化的对象
     * @return 对象序列化后的长度
     */
    int size(T entry);
 
    /**
     * 序列化对象。将给定的对象序列化成字节数组
     * @param entry 待序列化的对象
     * @param bytes 存放序列化数据的字节数组
     * @param offset 数组的偏移量，从这个位置开始写入序列化数据
     * @param length 对象序列化后的长度，也就是{@link Serializer#size(java.lang.Object)}方法的返回值。
     */
    void serialize(T entry, byte[] bytes, int offset, int length);
 
    /**
     * 反序列化对象
     * @param bytes 存放序列化数据的字节数组
     * @param offset 数组的偏移量，从这个位置开始写入序列化数据
     * @param length 对象序列化后的长度
     * @return 反序列化之后生成的对象
     */
    T parse(byte[] bytes, int offset, int length);
 
    /**
     * 用一个字节标识对象类型，每种类型的数据应该具有不同的类型值
     */
    byte type();
 
    /**
     * 返回序列化对象类型的 Class 对象。
     */
    Class<T> getSerializeClass();
}


//字符串序列化实现 , 统一UTF8
public class StringSerializer implements Serializer<String> {
    @Override
    public int size(String entry) {
        return entry.getBytes(StandardCharsets.UTF_8).length;
    }
 
    @Override
    public void serialize(String entry, byte[] bytes, int offset, int length) {
        byte [] strBytes = entry.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(strBytes, 0, bytes, offset, strBytes.length);
    }
 
    @Override
    public String parse(byte[] bytes, int offset, int length) {
        return new String(bytes, offset, length, StandardCharsets.UTF_8);
    }
 
    @Override
    public byte type() {
        return Types.TYPE_STRING;
    }
 
    @Override
    public Class<String> getSerializeClass() {
        return String.class;
    }
}
```


### 网络通信
```
public interface Transport {
    /**
     * 发送请求命令
     * @param request 请求命令
     * @return 返回值是一个 Future，Future
     */
    CompletableFuture<Command> send(Command request);
}


通信格式定义 

public class Command {
    protected Header header;
    private byte [] payload;
    //...
}
 
public class Header {
    private int requestId;
    private int version;
    private int type;
    // ...
}
public class ResponseHeader extends Header {
    private int code;
    private String error;
    // ...
}
```



使用 netty发送请求

定义inFlightRequests结构 保存在途中的请求

异步通信时 , 背压机制的实现和存在的必要性 , 相当于令牌分发和回收 , 在InFlightRequests类中,用信号量(类似锁)实现?

```
@Override
public  CompletableFuture<Command> send(Command request) {
    // 构建返回值
    CompletableFuture<Command> completableFuture = new CompletableFuture<>();
    try {
        // 将在途请求放到 inFlightRequests 中
        inFlightRequests.put(new ResponseFuture(request.getHeader().getRequestId(), completableFuture));
        // 发送命令
        channel.writeAndFlush(request).addListener((ChannelFutureListener) channelFuture -> {
            // 处理发送失败的情况
            if (!channelFuture.isSuccess()) {
                completableFuture.completeExceptionally(channelFuture.cause());
                channel.close();
            }
        });
    } catch (Throwable t) {
        // 处理发送异常
        inFlightRequests.remove(request.getHeader().getRequestId());
        completableFuture.completeExceptionally(t);
    }
    return completableFuture;
   ``` 
    

### 疑问

这里是如何把响应返回给客户端上层的 ?     AbstractStub里阻塞wait了
通讯方式是阻塞还是非阻塞 ?   发送网络请求的过程是非阻塞的 , 但是wait响应是阻塞的 ,    如果多个线程同时请求 , 会由inflightRequest类进行请求数限制

Stub是怎么生成的
Stub职责是什么 , 做了什么

用依赖倒置原则解耦 , 不能直接依赖也不能第三方间接依赖(如工厂) ,  用Java的SPI注入 , 配置文件配置 , 具体代码ServiceSupport


### 服务端
实现的单机版的注册中心 , 但可通过接口扩展成跨服务器的

NameService
LocalFileNameService
并发读写文件加锁 , 不能使用语言提供的锁(只能锁住进程内) , 使用系统提供的文件锁

RequestHandlerRegistry
把请求分发给对应的handler 处理完后返回响应给客户端
RpcRequestHandler 这个框架的核心 (注册 RPC 服务和处理客户端 RPC 请求)
```
@Override
public Command handle(Command requestCommand) {
    Header header = requestCommand.getHeader();
    // 从 payload 中反序列化 RpcRequest
    RpcRequest rpcRequest = SerializeSupport.parse(requestCommand.getPayload());
    // 查找所有已注册的服务提供方，寻找 rpcRequest 中需要的服务
    Object serviceProvider = serviceProviders.get(rpcRequest.getInterfaceName());
    // 找到服务提供者，利用 Java 反射机制调用服务的对应方法
    String arg = SerializeSupport.parse(rpcRequest.getSerializedArguments());
    Method method = serviceProvider.getClass().getMethod(rpcRequest.getMethodName(), String.class);
    String result = (String ) method.invoke(serviceProvider, arg);
    // 把结果封装成响应命令并返回
    return new Command(new ResponseHeader(type(), header.getVersion(), header.getRequestId()), SerializeSupport.serialize(result));
    // ...
}
```
