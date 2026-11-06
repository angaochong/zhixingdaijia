package com.example.zxds.vhr;

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
@MapperScan("com.example.zxds.vhr.db.dao")
@ComponentScan("com.example.*")
public class ZxdsVhrApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZxdsVhrApplication.class, args);
    }

}
