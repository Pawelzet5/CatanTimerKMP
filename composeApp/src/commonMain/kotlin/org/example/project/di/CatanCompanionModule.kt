package org.example.project.di

import org.koin.dsl.module

val catanCompanionModule = module {
    includes(
        databaseModule,
        repositoryModule,
        sessionModule,
        useCaseModule
    )
}
