package com.back.team11.global.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.springframework.context.annotation.Configuration

@Configuration
@OpenAPIDefinition(
    // 리팩토링: annotation argument 포맷을 Kotlin 스타일로 정리, 유지보수 용이성을 위해 trailing comma 적용
    info = Info(
        title = "Cafe Study API",
        description = "Cafe Study 프로젝트 API 명세서",
        version = "v1.0.0",
    )
)
class SwaggerConfig
