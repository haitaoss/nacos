package cn.haitaoss;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.remote.request.InstanceRequest;
import com.alibaba.nacos.api.naming.remote.request.SubscribeServiceRequest;
import com.alibaba.nacos.api.remote.request.RequestMeta;
import com.alibaba.nacos.client.config.NacosConfigService;
import com.alibaba.nacos.client.env.NacosClientProperties;
import com.alibaba.nacos.client.naming.NacosNamingMaintainService;
import com.alibaba.nacos.client.naming.NacosNamingService;
import com.alibaba.nacos.common.notify.Event;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.notify.SlowEvent;
import com.alibaba.nacos.common.notify.listener.Subscriber;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * @author haitao.chen
 * email haitaoss@aliyun.com
 * date 2023-06-12 17:24
 */
@SpringBootApplication
@RestController
public class NacosClient {
    
    private NamingService naming;
    
    private ConfigService configService;
    
    private NamingMaintainService maintainService;
    
    @PostConstruct
    public void init() throws Exception {
        String serverList = "http://localhost:8848";
        naming = NamingFactory.createNamingService(serverList);
        configService = NacosFactory.createConfigService(serverList);
        maintainService = NacosFactory.createMaintainService(serverList);
    }
    
    @RequestMapping("/service")
    public Object service(@RequestParam(required = false, defaultValue = "haitao2") String serviceName,
            @RequestParam(required = false, defaultValue = Constants.DEFAULT_GROUP) String groupName) throws Exception {
        // 维修服务，在 nacos 集群环境才行
        maintainService.createService(serviceName, groupName);
        return "ok...";
    }
    
    @RequestMapping("/instance")
    public Object instance(@RequestParam(required = false, defaultValue = "haitao") String serviceName)
            throws Exception {
        // 注册实例
        naming.registerInstance(serviceName, "127.0.0.1", 8080);
        //        naming.registerInstance(serviceName, "127.0.0.1", 8080, "TEST1");
        List<Instance> allInstances = naming.getAllInstances(serviceName);
        return allInstances;
    }
    
    @RequestMapping("/config")
    public Object config(@RequestParam(required = false, defaultValue = "haitao") String dataId,
            @RequestParam(required = false, defaultValue = "DEFAULT_GROUP") String group) throws Exception {
        // 注册实例
        String content = configService.getConfig(dataId, group, 5000);
        return content;
    }
    
    public static void main(String[] args) throws Exception {
        SpringApplication.run(NacosClient.class, args);
        /**
         * {@link NacosFactory}
         * NotifyCenter 通知中心
         *
         * nacos client 是注册 服务的
         * nacos console 是 server
         *
         * {@link NacosNamingService#init(Properties)}
         *      服务列表的
         *
         * {@link NacosConfigService#NacosConfigService(Properties)}
         *      配置文件的
         *
         * {@link NacosNamingMaintainService#init(Properties)}
         *      Nacos命名维护服务。
         *
         * 啥时候启动的 server ？？？
         *
         * naming 是 server 模块 ？
         *
         * jraft 是用于 nacos 集群的数据同步的。
         *
         * 读取配置文件，注册实例，
         *
         * 服务端、客户端 是如何存储 服务列表、实例信息、配置信息的
         * 客户端是定时任务 调 服务端的接口获取数据吗？
         *
         * Nacos Server 是如何存储 服务、实例、配置的，啥时候落库的
         *
         * grpc 的使用demo
         *
         * client 通过 grpc 调 server 注册实例，server端的处理逻辑
         * {@link com.alibaba.nacos.naming.remote.rpc.handler.InstanceRequestHandler#handle(InstanceRequest, RequestMeta)}
         * client 通过 grpc 调 server 查询实例信息，server端的处理逻辑
         * {@link com.alibaba.nacos.naming.remote.rpc.handler.SubscribeServiceRequestHandler#handle(SubscribeServiceRequest, RequestMeta)}
         */
    }
    
    
    private void extracted() {
        try {
            // 配置相关
            
            // 注销实例
            deregistry();
            
            // 获取实例
            get_instance();
            
            // 监听服务下的实例列表变化。
            listener();
            deListener();
            
            // 配置派生 Properties、JVM、ENV、
            nacos_properties();
            
            // 测试让主线程不退出，因为订阅配置是守护线程，主线程退出守护线程就会退出。 正式代码中无需下面代码
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private void config2() throws NacosException {
        //        用于服务启动的时候从 Nacos 获取配置。
        String serverAddr = "{serverAddr}";
        String dataId = "{dataId}";
        String group = "{group}";
        Properties properties = new Properties();
        properties.put("serverAddr", serverAddr);
        ConfigService configService = NacosFactory.createConfigService(properties);
        String content = configService.getConfig(dataId, group, 5000);
        System.out.println(content);
        // 监听器
        configService.addListener(dataId, group, new Listener() {
            @Override
            public void receiveConfigInfo(String configInfo) {
                System.out.println("recieve1:" + configInfo);
            }
            
            @Override
            public Executor getExecutor() {
                return null;
            }
        });
        
        // 移除监听器
        configService.removeListener(dataId, group, null);
        
        // 发布配置
        boolean isPublishOk = configService.publishConfig(dataId, group, "content");
        System.out.println(isPublishOk);
        
        // 删除配置
        boolean isRemoveOk = configService.removeConfig(dataId, group);
        System.out.println(isRemoveOk);
    }
    
    private void nacos_properties() {
        // NacosClientProperties 类似于 Spring Environment 来统一管理客户端的各种配置项
        // global properties
        NacosClientProperties.PROTOTYPE.setProperty("global-key1", "global-value1");
        
        // properties1
        NacosClientProperties properties1 = NacosClientProperties.PROTOTYPE.derive(); // 派生
        properties1.setProperty("properties1-key1", "properties1-value1");
        
        // properties2
        NacosClientProperties properties2 = properties1.derive(); // 派生
        properties2.setProperty("properties2-key1", "properties2-value1");
        
        String value = properties2.getProperty("global-key1");
    }
    
    private void deListener() throws NacosException {
        NamingService naming = NamingFactory.createNamingService(System.getProperty("serveAddr"));
        naming.unsubscribe("nacos.test.3", event -> {
        });
    }
    
    private void listener() throws NacosException {
        NamingService naming = NamingFactory.createNamingService(System.getProperty("serveAddr"));
        naming.subscribe("nacos.test.3", event -> {
            if (event instanceof NamingEvent) {
                System.out.println(((NamingEvent) event).getServiceName());
                System.out.println(((NamingEvent) event).getInstances());
            }
        });
    }
    
    private void get_instance() throws NacosException {
        NamingService naming = NamingFactory.createNamingService(System.getProperty("serveAddr"));
        System.out.println(naming.getAllInstances("nacos.test.3"));
        System.out.println(naming.selectOneHealthyInstance("nacos.test.3"));
    }
    
    private void deregistry() throws NacosException {
        NamingService naming = NamingFactory.createNamingService(System.getProperty("serveAddr"));
        naming.deregisterInstance("nacos.test.3", "11.11.11.11", 8888, "DEFAULT");
    }
    
    
    private void pubsub() {
        // 维护 Publish，SubScribe 是注册到 Publish 中
        NotifyCenter.registerSubscriber(new Subscriber() {
            @Override
            public void onEvent(Event event) {
            
            }
            
            @Override
            public Class<? extends Event> subscribeType() {
                return SlowEvent.class;
            }
        });
        // 找到适配的 Publish 发布事件，其实就是遍历 Publish 中的  SubScribe 接收事件
        NotifyCenter.publishEvent(new Event() {
        });
    }
    
}