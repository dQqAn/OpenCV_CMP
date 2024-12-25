package com.example.opencv.koin

import android.app.Application
import android.content.Context
import com.example.common.*
import org.koin.dsl.module

class AndroidApp : Application() {
    override fun onCreate() {
        super.onCreate()

        initKoin(
            module {
                single<Context> { this@AndroidApp }

                single { AndroidActivityViewModel() }

                single { CameraViewModel() }

                single { ImageStitcher(context = get(), fileUtil = get()) }

                single {
                    FileUtil(
                        context = get(),
                        androidActivityViewModel = get()
                    )
                }
            }
        )
    }
}


