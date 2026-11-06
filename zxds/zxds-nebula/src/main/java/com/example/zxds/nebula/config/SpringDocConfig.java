package com.example.zxds.nebula.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "zxds-nebula",
                description = "智行代驾nebula子系统",
                version = "1.0"
        )
)
@Configuration
public class SpringDocConfig {
}
