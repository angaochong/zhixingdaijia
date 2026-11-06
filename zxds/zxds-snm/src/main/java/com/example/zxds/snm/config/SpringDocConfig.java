package com.example.zxds.snm.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "zxds-snm",
                description = "智行代驾消息子系统",
                version = "1.0"
        )
)
@Configuration
public class SpringDocConfig {

}
