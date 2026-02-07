package com.waterworks;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 水务管理系统启动类
 * 
 * @author 水务管理系统
 * @version 1.0.0
 */
@SpringBootApplication
@MapperScan("com.waterworks.mapper")
@EnableScheduling
public class WaterManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(WaterManagementApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("水务管理系统启动成功！");
        System.out.println("接口文档地址: http://localhost:8080/api/swagger-ui.html");
        System.out.println("API文档JSON: http://localhost:8080/api/api-docs");
        System.out.println("========================================\n");
    }
}


