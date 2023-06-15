package cn.haitaoss;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author haitao.chen
 * email haitaoss@aliyun.com
 * date 2023-06-12 17:24
 */
@SpringBootApplication(scanBasePackages = "com.alibaba.nacos")
public class NacosServer {
    
    public static void main(String[] args) {
        System.setProperty("nacos.standalone", "true");
        SpringApplication.run(NacosServer.class, args);
    }
}