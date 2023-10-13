package com.example.opencv.koin

import android.app.Application
import android.content.Context
import com.example.common.AndroidActivityViewModel
import com.example.common.FileUtil
import com.example.common.initKoin
import org.koin.dsl.module

class AndroidApp : Application() {
    override fun onCreate() {
        super.onCreate()

        initKoin(
            module {
                single<Context> { this@AndroidApp }

                single { AndroidActivityViewModel() }

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


