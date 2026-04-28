package io.github.pawelzielinski.catantimer.catanCompanion.presentation.service

import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType

actual object HapticService {
    actual fun vibrateOnce() {
        UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleLight)
            .impactOccurred()
    }

    actual fun vibrateOnceHeavy() {
        UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy)
            .impactOccurred()
    }

    actual fun vibrateMultiple() {
        UINotificationFeedbackGenerator().notificationOccurred(
            UINotificationFeedbackType.UINotificationFeedbackTypeSuccess
        )
    }
}
