package com.example.ftxtrademanager

import com.example.ftxtrademanager.client.FtxClient
import com.example.ftxtrademanager.model.ApiResponse
import com.example.ftxtrademanager.model.Order
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@ConfigurationPropertiesScan("com.example.ftxtrademanager.property")
@SpringBootApplication
class FtxTradeManagerApplication {
    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplateBuilder()
            .build()
    }

    @Bean
    fun init(ftxClient: FtxClient) = CommandLineRunner {
        val uriVariables = mutableMapOf<String, String>()
        uriVariables.apply {
            put("market", "BTC-PERP")
        }
        val response = ftxClient.doGetRequest(
            "/api/orders/history",
            uriVariables,
            typeReference<ApiResponse<Order>>()
        )
        response.body?.let { calculateWinrate(it.result) }
    }

    fun calculateWinrate(orders: List<Order>) {
        var winning = 0
        var losing = 0
        var tradeCount = 0
        var size = 0.0
        var openPrice = 0.0
        var openCount = 0
        var closePrice = 0.0
        var closeCount = 0
        var isLong = false
        val filteredOrders = orders.filter { it.filledSize != 0.0 }
        val sortedOrders = filteredOrders
            .sortedBy { LocalDateTime.parse(it.createdAt, DateTimeFormatter.ISO_DATE_TIME) }
        println(ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(sortedOrders))
        // TODO: Had an order without reduceOnly=true, it breaks everything?
        for (order in sortedOrders) {
            if (!order.reduceOnly) {
                size += order.filledSize
                println("Position size after adding: $size")
                openPrice += order.avgFillPrice!!
                openCount++
                isLong = "buy" == order.side
            } else {
                size -= order.size
                println("Position size after reducing: $size")
                closePrice += order.avgFillPrice!!
                closeCount++
                if (size.equals(0.0)) {
                    openPrice /= openCount
                    closePrice /= closeCount
                    if (isLong) {
                        if (closePrice > openPrice) {
                            println("Bought at: $openPrice, sold at: $closePrice for a win")
                            winning++
                        } else {
                            losing++
                            println("Bought at: $openPrice, sold at: $closePrice for a loss")
                        }
                    } else {
                        if (closePrice < openPrice) {
                            winning++
                            println("Sold at: $openPrice, bought at: $closePrice for a win")
                        } else {
                            losing++
                            println("Sold at: $openPrice, bought at: $closePrice for a loss")
                        }
                    }
                    tradeCount++
                    openCount = 0
                    openPrice = 0.0
                    closePrice = 0.0
                    closeCount = 0
                    println()
                }
            }
        }
        println(
            """
                Total trades: $tradeCount
                winning: $winning
                losing: $losing
                win %: ${(winning.toDouble() / tradeCount) * 100}
            """.trimIndent()
        )
    }
}

fun main(args: Array<String>) {
    runApplication<FtxTradeManagerApplication>(*args)
}

inline fun <reified T> typeReference() = object : ParameterizedTypeReference<T>() {}
