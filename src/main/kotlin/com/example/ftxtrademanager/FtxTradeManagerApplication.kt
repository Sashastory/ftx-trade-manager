package com.example.ftxtrademanager

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestTemplate

@ConfigurationPropertiesScan("com.example.ftxtrademanager.property")
@SpringBootApplication
class FtxTradeManagerApplication {
    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplateBuilder()
                .build()
    }
}

fun main(args: Array<String>) {
    runApplication<FtxTradeManagerApplication>(*args)
}
