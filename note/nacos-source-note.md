[console](../console)
console 模块 是启动入口
它依赖了 nacos-config 、nacos-naming
这两个模块又依赖了 nacos-core

nacos-config 是web

https://nacos.io/zh-cn/docs/v2/quickstart/quick-start.html

git clone https://github.com/alibaba/nacos.git
cd nacos/
mvn -Prelease-nacos -Dmaven.test.skip=true clean install -U  
ls -al distribution/target/

// change the $version to your actual path
cd distribution/target/nacos-server-$version/nacos/bin

startup.cmd -m standalone

什么是 gRPC ？https://cloud.tencent.com/developer/article/1889023


## [auth](..%2Fplugin%2Fauth) 鉴权插件,包含客户端和服务端的鉴权实现
https://nacos.io/zh-cn/docs/v2/plugin/auth-plugin.html
Nacos从2.1.0版本开始，支持通过SPI的方式注入鉴权相关插件，并在application.properties配置文件中选择某一种插件实现作为实际鉴权服务。本文档会详细介绍如何实现一个鉴权插件和如何使其生效。


## [encryption](..%2Fplugin%2Fencryption) 加密的

https://nacos.io/zh-cn/docs/v2/plugin/config-encryption-plugin.html
插件化实现：
通过 SPI 的机制抽象出加密和解密的操作，Nacos 默认提供 AES 的实现。用户也可以自定义加解密的实现方式。具体的实现在 nacos-plugin 仓库。

在 Nacos 服务端启动的时候就会加载所有依赖的加解密算法，然后通过发布配置的 dataId 的前缀来进行匹配是否需要加解密和使用的加解密算法。

客户端发布的配置会在客户端通过filter完成加解密，也就是配置在传输过程中都是密文的。而控制台发布的配置会在服务端进行处理。

## [datasource](..%2Fplugin%2Fdatasource)
sql模板，根据不同的数据库编写自定义的sql，而且是插件式的，支持扩展。
sql 的执行是依赖 JdbcTemplate
现在的多数据源插件通过SPI机制，将SQL操作按照数据表进行抽象出多个Mapper接口，Mapper接口的实现类需要按照不同的数据源编写对应的SQL方言实现; 现在插件默认提供Derby以及MySQL的Mapper实现，可直接使用；而其他的数据源则需要用户使用数据源插件进行加载，其改造后架构图如下。


## [trace](..%2Fplugin%2Ftrace)
Nacos从2.2.0版本开始，可通过SPI机制注入轨迹追踪实现插件，在插件中订阅并处理追踪事件，并按照您期望的方式进行处理（如打日志，写入存储等）。本文档详细介绍一个轨迹追踪插件如何实现以及如何使其生效。
在nacos-group/nacos-plugin中，有一个demo的轨迹追踪插件实现，该demo插件订阅了注册及注销实例的事件，并打印到日志中。

## 自定义环境变量插件
[environment](..%2Fplugin%2Fenvironment)
EnvUtil 会依赖 environment 中的接口

## [control](..%2Fplugin%2Fcontrol)
好像就是分开了好几个线程池，做一些统计的信息（连接、tps）


## Java SDk 
https://nacos.io/zh-cn/docs/v2/guide/user/sdk.html

# 重要的类 
NotifyCenter 通知中心 


## 有用的连接
Nacos 术语说明 https://nacos.io/zh-cn/docs/v2/concepts.html
jraft 文档 https://www.sofastack.tech/projects/sofa-jraft/overview/
jraft 用户指南 https://www.sofastack.tech/projects/sofa-jraft/jraft-user-guide/