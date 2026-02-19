package com.stackbenchmark.kmpcmp

import androidx.compose.ui.window.ComposeUIViewController
import com.stackbenchmark.kmpcmp.di.initKoin

fun MainViewController() = run {
    initKoin()
    ComposeUIViewController { SharedApp() }
}
