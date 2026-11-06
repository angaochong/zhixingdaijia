package com.example.zxds.odr;

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
@MapperScan({"com.example.zxds.odr.db.dao"})
@ComponentScan({"com.example.*"})
public class ZxdsOdrApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZxdsOdrApplication.class, args);
    }

}
