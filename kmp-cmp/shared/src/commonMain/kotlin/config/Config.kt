package com.stackbenchmark.kmpcmp.config

object Config {
    private val properties: Map<String, String> by lazy { loadProperties() }

    val apiKey: String get() = properties["tmdb_api_key"] ?: ""
    val baseUrl: String get() = properties["tmdb_base_url"] ?: "https://api.themoviedb.org/3"
    val imageBaseUrl: String get() = properties["tmdb_image_base_url"] ?: "https://image.tmdb.org/t/p"
}

expect fun loadProperties(): Map<String, String>
