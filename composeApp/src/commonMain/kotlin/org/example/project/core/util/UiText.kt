package org.example.project.core.util

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

sealed class UiText {
    data class DynamicString(val value: String) : UiText()
    data class StringResourceId(
        val id: StringResource,
        val args: Array<Any> = arrayOf()
    ): UiText()

    @Composable
    fun asStringComposable(): String {
        return when(this) {
            is DynamicString -> value
            is StringResourceId -> stringResource(id, args)
        }
    }

    suspend fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is StringResourceId -> getString(id, args)
        }
    }
}