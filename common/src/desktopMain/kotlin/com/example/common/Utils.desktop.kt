package com.example.common

import androidx.compose.runtime.Composable

@Composable
actual fun CameraPage(stitcherViewModel: StitcherViewModel, cameraViewModel: CameraViewModel) {
    MainPageContent(stitcherViewModel)
}

actual class CameraViewModel : ViewModel()