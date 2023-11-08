package com.example.common

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.common.koin.DesktopApp
import org.koin.core.Koin

private lateinit var stitcherViewModel: ViewModel
private lateinit var cameraViewModel: ViewModel
private lateinit var koin: Koin

fun main() = application {
    koin = DesktopApp().initKoin.koin.apply {
        stitcherViewModel = this.get<StitcherViewModel>()
        cameraViewModel = this.get<CameraViewModel>()
    }
    Window(onCloseRequest = ::exitApplication) {
        MainPage(
            stitcherViewModel as StitcherViewModel,
            cameraViewModel as CameraViewModel,
        )
    }
}
