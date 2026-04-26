package io.github.pawelzielinski.catantimer

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.pawelzielinski.catantimer.di.initKoin

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "CatanTimer",
        ) {
            App()
        }
    }
}