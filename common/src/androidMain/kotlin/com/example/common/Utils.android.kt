package com.example.common

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable

@RequiresApi(Build.VERSION_CODES.P)
@Composable
actual fun CameraPageContent(
    stitcherViewModel: StitcherViewModel,
    cameraViewModel: CameraViewModel,
    onBackClick: () -> Unit
) {
    CameraAndroidPage(cameraViewModel, onBackClick)
}

actual typealias CameraViewModel = AndroidCameraViewModel