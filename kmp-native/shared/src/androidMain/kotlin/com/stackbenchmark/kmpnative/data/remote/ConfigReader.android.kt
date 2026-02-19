package com.stackbenchmark.kmpnative.data.remote

import java.util.Properties

actual object ConfigReader {
    private val props: Properties by lazy {
        Properties().apply {
            val stream = Thread.currentThread().contextClassLoader
                ?.getResourceAsStream("config.properties")
                ?: ConfigReader::class.java.classLoader
                    ?.getResourceAsStream("config.properties")
                ?: ConfigReader::class.java.getResourceAsStream("/config.properties")
            if (stream != null) {
                load(stream)
                stream.close()
            }
        }
    }

    actual val apiKey: String get() = props.getProperty("tmdb_api_key", "")
    actual val baseUrl: String get() = props.getProperty("tmdb_base_url", "https://api.themoviedb.org/3")
    actual val imageBaseUrl: String get() = props.getProperty("tmdb_image_base_url", "https://image.tmdb.org/t/p")
}
