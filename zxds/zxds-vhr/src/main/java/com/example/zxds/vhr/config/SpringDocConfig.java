package com.example.zxds.vhr.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "zxds-vhr",
                description = "智行代驾代金券子系统",
                version = "1.0"
        )
)
@Configuration
public class SpringDocConfig {


}
