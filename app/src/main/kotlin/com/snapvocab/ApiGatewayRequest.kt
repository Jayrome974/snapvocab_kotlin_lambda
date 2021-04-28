package com.snapvocab

data class ApiGatewayRequest(val body: String, val pathParameters: Map<String, String>)
