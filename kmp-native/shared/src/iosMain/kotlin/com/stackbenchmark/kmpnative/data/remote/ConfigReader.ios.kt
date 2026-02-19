package com.stackbenchmark.kmpnative.data.remote

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.Foundation.NSString
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.NSUTF8StringEncoding

@OptIn(ExperimentalForeignApi::class)
actual object ConfigReader {
    private val properties: Map<String, String> by lazy {
        val path = NSBundle.mainBundle.pathForResource("config", ofType = "properties")
        if (path != null) {
            val content = NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null) ?: ""
            content.toString().lines()
                .filter { it.contains("=") && !it.trimStart().startsWith("#") }
                .associate {
                    val (key, value) = it.split("=", limit = 2)
                    key.trim() to value.trim()
                }
        } else {
            emptyMap()
        }
    }

    actual val apiKey: String get() = properties["tmdb_api_key"] ?: ""
    actual val baseUrl: String get() = properties["tmdb_base_url"] ?: "https://api.themoviedb.org/3"
    actual val imageBaseUrl: String get() = properties["tmdb_image_base_url"] ?: "https://image.tmdb.org/t/p"
}
