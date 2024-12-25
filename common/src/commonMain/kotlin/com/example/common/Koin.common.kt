package com.example.common

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

fun initKoin(appModule: Module): KoinApplication = startKoin { modules(coreModule, appModule) }

private val coreModule = module {

//    single { ImageStitcher(fileUtil = get()) }
    single {
        StitcherViewModel(
            fileUtil = get(),
            imageStitcher = get()
        )
    }
}
