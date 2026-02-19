package com.stackbenchmark.kmpcmp.config

actual fun loadProperties(): Map<String, String> {
    val properties = java.util.Properties()
    val stream = Thread.currentThread().contextClassLoader
        ?.getResourceAsStream("config.properties")
        ?: Config::class.java.classLoader?.getResourceAsStream("config.properties")
        ?: Config::class.java.getResourceAsStream("/config.properties")
        ?: return emptyMap()
    stream.use { properties.load(it) }
    return properties.entries.associate { it.key.toString() to it.value.toString() }
}
