package com.example.common.koin

import com.example.common.CameraViewModel
import com.example.common.FileUtil
import com.example.common.initKoin
import org.koin.dsl.module

class DesktopApp {
    val initKoin = initKoin(
        module {
            single {
                FileUtil()
            }

            single { CameraViewModel() }
        }
    )
}
