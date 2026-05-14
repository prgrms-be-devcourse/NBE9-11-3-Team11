package com.back.team11.global.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.springframework.context.annotation.Configuration

@Configuration
@OpenAPIDefinition(
    info = Info( // = @Info 에서 = Info로 변경
        title = "Cafe Study API",
        description = "Cafe Study 프로젝트 API 명세서",
        version = "v1.0.0")
)
class SwaggerConfig 