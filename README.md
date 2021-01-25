#rpc-service Rpc远程服务调用

##测试依赖项
rpc-client-test：服务调用方，客户端（也可以做服务提供方，@EnableRpcService(mode = RpcMode.ALL)）
rpc-server-test：服务提供方，服务端（定义为只对外提供服务，@EnableRpcService(mode = RpcMode.SERVER)）
rpc-server-facade，rpc-server-facade2：facade接口，提供给客户端的依赖，由服务端进行具体实现

##核心实现
rpc-common：包含rpc-server及rpc-client基本依赖，稍后详细说明
rpc-client：客户端具体实现，稍后详细说明
rpc-server：服务端具体实现，稍后详细说明
rpc-register-center：服务注册中心，稍后详细说明


##rpc-register-center
###核心类说明
- curator
    - ZookeeperClientFactory：zk工厂类
        1. 需要调用init()方法对ServiceCuratorFramework进行初始化
        2. 根据digestMap分配的不同digest权限，创建客户端CuratorFramework，指定auth
    - ServiceCuratorFramework: 封装的CuratorFramework，包含zk的基本操作
- loader
    - ServiceRegister：服务注册的静态方法
- properties
    - RpcServiceZookeeperProperties：zk配置文件，包含zk启动配置
    
#服务注册示例
    
    //获取ServiceCuratorFrameork
    ZookeeperClientFactory.init()
    ServiceCuratorFramework default = ZookeeperClientFactory.getDefaultClient()
    ServiceCuratorFramework app = ZookeeperClientFactory.getCuratorFrameworkByAppName(appName);
    //持久化临时节点
    framework.ephemeral(StringUtils.joinWith(SEPARATOR, SERVICE_INTERFACE_PATH, interfaceName, CONSUMER, appName, ip), null);
    //注册服务
    ServiceRegister.registerProviderInterface(rpcServiceZookeeperProperties.getAppName(), IpUtil.getHostIP(), properties.getPort());

##rpc-common
###核心类说明
- annotation
    - EnableRpcService：开启RpcService注解，可指定mode为CLIENT,SERVER,ALL，表示该项目是哪种模式
    - RpcService：注解类，facade接口需要指定该注解，表示这个类向外提供服务，且只针对接口
        - 1. value() 表示这个接口，有哪几种实现实现，对应的可以是服务端的bean name
        - 2. default() 表示这个注解，默认实现是什么，对应的可能是一个默认的bean name
        - 3. 考虑到存在同名的bean name，比如用户接口，两个服务端，都定义成为userService，那么可以使用#com.cy.rpc.server.facade2.FacadeServer = facadeService配置，指定一个自定义bean name
    - RpcServiceConfigurationSelector：EnableRpcService注解对应的选择器，import对应的配置
- payload
    - MethodPayload：客户端向服务端实际发送的实体对象，包装对应的接口信息
    - ResultPayload：服务端向客户端实际返回的实体对象，包装对应的返回结果及错误码
    
##rpc-server
###核心类说明
- Server：Netty服务启动类，核心类，bind一个配置化端口
- service：
    - AbstractServiceFactory：一个抽象的服务工厂类，有服务端自己定义，比如通过serviceName加载对应的Bean
- handler：Netty pipeline() 指定的handler
    - ByteToParamsPayloadDecode：将字节码解码成为MethodPayload对象，进行解析
    - ResultPayloadToByteEncode：将返回结果编码成ResultPayload进行发送
    - RpcServerChannelHandler：专门处理MethodPayload，通过AbstractServiceFactory获取到的对象，反射调用具体的接口，获取到ResultPayload发送给客户端
    - ServerHeartPongHandler：接受客户端心跳，并返回客户端一个收到心跳的消息，用于心跳检测
    - DelimiterBasedFrameDecoder：Netty封装的防粘包handler，也要加入到pipeline中
- configuration
    - RpcServerConfiguration：具体的启动配置，主要写服务端注册中心，启动服务端

##rpc-client
###接口核心类说明
- Client：Netty客户端启动类，创建一个客户端连接，connect到一个指定的服务端，远程ip及端口一般从注册中心获取
- future：对结果的处理
    - ResultFuture：用于同步当前请求的返回结果，但是不会锁所有请求
- handler：Netty pipeline() 指定的handler
    - ByteToResultPayloadDecode：将字节码解码成为ResultPayload对象，进行解析
    - ParamsPayloadToByteEncode：将请求的信息编码成MethodPayload进行发送
    - RpcClientChannelHandler：接受服务端返回的ResultPayload结果，并通知future
    - ClientHeartPingHandler：客户端监听active和inactive事件，进行定时发送心跳检测和重连
    - ClientHeartPongHandler：客户端收到服务端返回的心跳信息
    - ClientIdleStateHandler：监听超时读、超时写、服务端连接断开的事件，并作出相应的处理
    - DelimiterBasedFrameDecoder：Netty封装的防粘包handler，也要加入到pipeline中
- annotation
    - RpcClientScan：包扫描注解，扫描指定包下面的facade接口
    - RpcServiceRegistrar: 包扫描注册器
    - RpcServiceScanner：包扫描器，扫描到接口后，进行BeanFactory实例化bean代理类
- cluster
    - ClientCluster：客户端集群类，包含一个appName服务端，对应的所有的客户端连接，因为服务端可能部署到多台服务器
    - ClientClusterCache：缓存客户端集群的连接信息
    - ClientConnect：实际的连接入口，所有的连接走connect()方法，且加上断开重连机制
    - selector：选择器，从集群列表选择一个机器，执行
    - retry：重试策略，用于计算重试间隔
- configuration
    - RpcServerConfiguration：具体的启动配置，主要写客户端注册中心，根据扫描到的接口，查找appName下的所有客户端，然后继续客户端连接
    

