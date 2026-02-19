package com.stackbenchmark.kmpcmp.config

import platform.Foundation.NSBundle
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfFile

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual fun loadProperties(): Map<String, String> {
    val path = NSBundle.mainBundle.pathForResource("config", ofType = "properties")
        ?: return emptyMap()
    val content = NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null)
        ?: return emptyMap()
    val result = mutableMapOf<String, String>()
    content.toString().lines().forEach { line ->
        val trimmed = line.trim()
        if (trimmed.isNotEmpty() && !trimmed.startsWith("#") && '=' in trimmed) {
            val idx = trimmed.indexOf('=')
            result[trimmed.substring(0, idx).trim()] = trimmed.substring(idx + 1).trim()
        }
    }
    return result
}
