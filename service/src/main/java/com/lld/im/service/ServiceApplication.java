package com.lld.im.service;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ClassName: Application
 * Package: com.lld.im.service
 * Description:
 *
 * @Author 南极星
 * @Create 2025/7/18 上午9:31
 * Version 1.0
 */
@SpringBootApplication(scanBasePackages = {"com.lld.im.service",
        "com.lld.im.common"})//扫描解决“能否识别 Spring组件注解等,除了默认路径，还要扫描这些包下的组件
@MapperScan("com.lld.im.service.*.dao.mapper")
public class ServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceApplication.class, args);
    }
}
