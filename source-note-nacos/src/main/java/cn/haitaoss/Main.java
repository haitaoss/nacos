package cn.haitaoss;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Cluster;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.Service;
import com.alibaba.nacos.api.naming.pojo.healthcheck.impl.Http;
import com.alibaba.nacos.client.config.NacosConfigService;
import com.alibaba.nacos.client.env.NacosClientProperties;
import com.alibaba.nacos.client.naming.NacosNamingMaintainService;
import com.alibaba.nacos.client.naming.NacosNamingService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * @author haitao.chen
 * email haitaoss@aliyun.com
 * date 2023-06-12 17:24
 */
@SpringBootApplication(scanBasePackages = "com.alibaba.nacos")
public class Main {
    
    public static void main(String[] args) {
        System.setProperty("nacos.standalone", "true");
        SpringApplication.run(Main.class, args);
        //        extracted();
        /**
         * {@link NacosFactory}
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
         */
    }
    
    private static void extracted() {
        try {
            // 配置相关
            config();
            
            // 注册实例
            registry_instance();
            
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
        } catch (NacosException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private static void config() throws NacosException {
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
    
    private static void nacos_properties() {
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
    
    private static void deListener() throws NacosException {
        NamingService naming = NamingFactory.createNamingService(System.getProperty("serveAddr"));
        naming.unsubscribe("nacos.test.3", event -> {
        });
    }
    
    private static void listener() throws NacosException {
        NamingService naming = NamingFactory.createNamingService(System.getProperty("serveAddr"));
        naming.subscribe("nacos.test.3", event -> {
            if (event instanceof NamingEvent) {
                System.out.println(((NamingEvent) event).getServiceName());
                System.out.println(((NamingEvent) event).getInstances());
            }
        });
    }
    
    private static void get_instance() throws NacosException {
        NamingService naming = NamingFactory.createNamingService(System.getProperty("serveAddr"));
        System.out.println(naming.getAllInstances("nacos.test.3"));
        System.out.println(naming.selectOneHealthyInstance("nacos.test.3"));
    }
    
    private static void deregistry() throws NacosException {
        NamingService naming = NamingFactory.createNamingService(System.getProperty("serveAddr"));
        naming.deregisterInstance("nacos.test.3", "11.11.11.11", 8888, "DEFAULT");
    }
    
    private static void registry_instance() throws NacosException {
        NamingService naming = NamingFactory.createNamingService(System.getProperty("serveAddr"));
        naming.registerInstance("nacos.test.3", "11.11.11.11", 8888, "TEST1");
        
        Instance instance = new Instance();
        instance.setIp("55.55.55.55");
        instance.setPort(9999);
        instance.setHealthy(false);
        instance.setWeight(2.0);
        Map<String, String> instanceMeta = new HashMap<>();
        instanceMeta.put("site", "et2");
        instance.setMetadata(instanceMeta);
        
        Properties prop = new Properties();
        prop.setProperty(PropertyKeyConst.NAMESPACE, "public");
        prop.setProperty("serverAddr", "localhost");
        
        NacosNamingMaintainService nacosNamingMaintainService = new NacosNamingMaintainService(prop);
        nacosNamingMaintainService.createService("nacos.test.4", "CNCF", 0.8F);
        
        instance.setServiceName("nacos.test.4");
        
        Cluster cluster = new Cluster();
        cluster.setName("TEST5");
        Http healthChecker = new Http();
        healthChecker.setExpectedResponseCode(400);
        healthChecker.setPath("/xxx.html");
        cluster.setHealthChecker(healthChecker);
        Map<String, String> clusterMeta = new HashMap<>();
        clusterMeta.put("xxx", "yyyy");
        cluster.setMetadata(clusterMeta);
        
        instance.setClusterName("TEST5");
        
        // 向 server 注册实例
        naming.registerInstance("nacos.test.4", instance);
    }
}