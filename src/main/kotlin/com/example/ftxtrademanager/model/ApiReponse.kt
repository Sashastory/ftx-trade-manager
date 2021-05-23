package com.example.ftxtrademanager.model

data class ApiResponse<R>(val success: Boolean, val result: List<R>)
