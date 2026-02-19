package com.stackbenchmark.kmpnative.data.remote

expect object ConfigReader {
    val apiKey: String
    val baseUrl: String
    val imageBaseUrl: String
}
