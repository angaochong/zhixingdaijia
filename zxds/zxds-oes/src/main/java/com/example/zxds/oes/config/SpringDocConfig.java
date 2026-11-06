package com.example.zxds.oes.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "zxds-oes",
                description = "智行代驾订单执行子系统",
                version = "1.0"
        )
)
@Configuration
public class SpringDocConfig {


}
