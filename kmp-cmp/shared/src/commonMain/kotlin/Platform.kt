package com.stackbenchmark.kmpcmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
