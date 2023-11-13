package com.koflox.currency_exchanger._di

import org.koin.core.module.Module

internal val koinModules: List<Module> = listOf(
    remoteDataSourceModule,
    localDataSourceModule,
    repositoryModule,
    useCaseModule,
    viewModelModule,
    utilModule,
)
