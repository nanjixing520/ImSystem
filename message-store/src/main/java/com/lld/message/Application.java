package com.lld.message;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ClassName: Application
 * Package: com.lld.message
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/18 上午10:39
 * Version 1.0
 */
@SpringBootApplication
@MapperScan("com.lld.message.dao.mapper")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


}
