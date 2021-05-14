package com.example.ftxtrademanager.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "ftx")
data class FtxProperties(val url: String, val apiKey: String)
