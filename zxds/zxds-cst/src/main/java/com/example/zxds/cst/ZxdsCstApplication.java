package com.example.zxds.cst;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@ServletComponentScan
@MapperScan("com.example.zxds.cst.db.dao")
@ComponentScan("com.example.*")
public class ZxdsCstApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZxdsCstApplication.class, args);
    }

}
