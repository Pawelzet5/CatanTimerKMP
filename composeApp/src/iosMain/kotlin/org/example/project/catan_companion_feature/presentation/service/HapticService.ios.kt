package org.example.project.catan_companion_feature.presentation.service

import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType

actual class HapticService {
    actual fun vibrateTimerEnd() {
        UINotificationFeedbackGenerator().notificationOccurred(
            UINotificationFeedbackType.UINotificationFeedbackTypeSuccess
        )
    }
}
