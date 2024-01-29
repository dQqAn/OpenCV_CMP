package com.example.common

import androidx.compose.runtime.Composable

@Composable
actual fun CameraPageContent(
    stitcherViewModel: StitcherViewModel,
    cameraViewModel: CameraViewModel,
    onBackClick: () -> Unit,
) {
    MainPageContent(stitcherViewModel, onBackClick)
}

actual class CameraViewModel : ViewModel()