package io.github.pawelzielinski.catantimer

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import catantimer.composeapp.generated.resources.Res
import catantimer.composeapp.generated.resources.icon
import catantimer.composeapp.generated.resources.icon_light
import io.github.pawelzielinski.catantimer.di.initKoin
import org.jetbrains.compose.resources.painterResource

fun main() {
    initKoin()
    application {
        val isDark = isSystemInDarkTheme()
        Window(
            onCloseRequest = ::exitApplication,
            title = "CatanTimer",
            icon = painterResource(if (isDark) Res.drawable.icon else Res.drawable.icon_light),
        ) {
            App()
        }
    }
}