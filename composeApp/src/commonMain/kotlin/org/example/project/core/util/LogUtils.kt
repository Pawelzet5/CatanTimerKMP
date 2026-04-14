package org.example.project.core.util

import co.touchlab.kermit.Logger

object LogUtils {
    private const val DEFAULT_TAG = "CatanTimerApp"

    fun debug(message: String, tag: String = DEFAULT_TAG) {
        Logger.d(messageString = message, tag = tag)
    }

    fun info(message: String, tag: String = DEFAULT_TAG) {
        Logger.i(messageString = message, tag = tag)
    }

    fun warn(message: String, tag: String = DEFAULT_TAG) {
        Logger.w(messageString = message, tag = tag)
    }

    fun error(message: String, tag: String = DEFAULT_TAG, throwable: Throwable? = null) {
        Logger.e(messageString = message, throwable = throwable, tag = tag)
    }
} 