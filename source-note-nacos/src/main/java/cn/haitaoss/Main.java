package cn.haitaoss;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Cluster;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * @author haitao.chen email haitaoss@aliyun.com date 2023-06-12 17:24
 */
public class Main {
    
    public static void main(String[] args) {
        //        System.setProperty("nacos.standalone", "true");
        System.out.println("Hello world!");
        extracted();
    }
    
    private static void extracted() {
        //        用于服务启动的时候从 Nacos 获取配置。
        try {
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
            
            // 注册实例
            registry_instance();
            
            // 注销实例
            deregistry();
            
            // 获取实例
            get_instance();
            
            // 看到 https://nacos.io/zh-cn/docs/v2/guide/user/sdk.html
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
    
    private static void get_instance() throws NacosException {
        NamingService naming = NamingFactory.createNamingService(System.getProperty("serveAddr"));
        System.out.println(naming.getAllInstances("nacos.test.3"));
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
        
        Service service = new Service("nacos.test.4");
        service.setApp("nacos-naming");
        service.sethealthCheckMode("server");
        service.setEnableHealthCheck(true);
        service.setProtectThreshold(0.8F);
        service.setGroup("CNCF");
        Map<String, String> serviceMeta = new HashMap<>();
        serviceMeta.put("symmetricCall", "true");
        service.setMetadata(serviceMeta);
        instance.setService(service);
        
        Cluster cluster = new Cluster();
        cluster.setName("TEST5");
        AbstractHealthChecker.Http healthChecker = new AbstractHealthChecker.Http();
        healthChecker.setExpectedResponseCode(400);
        healthChecker.setCurlHost("USer-Agent|Nacos");
        healthChecker.setCurlPath("/xxx.html");
        cluster.setHealthChecker(healthChecker);
        Map<String, String> clusterMeta = new HashMap<>();
        clusterMeta.put("xxx", "yyyy");
        cluster.setMetadata(clusterMeta);
        
        instance.setCluster(cluster);
        
        naming.registerInstance("nacos.test.4", instance);
    }
}