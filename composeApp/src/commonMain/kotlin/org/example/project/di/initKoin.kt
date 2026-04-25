package io.github.pawelzielinski.catantimer.di

import io.github.pawelzielinski.catantimer.catan_companion_feature.di.catanCompanionModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(coreModule, platformModule, catanCompanionModule)
    }
}