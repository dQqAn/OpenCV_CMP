package com.example.common

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable

@RequiresApi(Build.VERSION_CODES.P)
@Composable
actual fun CameraPage(stitcherViewModel: StitcherViewModel, cameraViewModel: CameraViewModel) {
    CameraAndroidPage(cameraViewModel)
}

actual typealias CameraViewModel = AndroidCameraViewModel