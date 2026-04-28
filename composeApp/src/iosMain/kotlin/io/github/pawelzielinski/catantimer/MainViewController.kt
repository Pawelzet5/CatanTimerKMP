package io.github.pawelzielinski.catantimer

import androidx.compose.ui.window.ComposeUIViewController
import io.github.pawelzielinski.catantimer.di.initKoin

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
    }
) { App() }