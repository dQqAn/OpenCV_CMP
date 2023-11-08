package com.example.common

import android.graphics.Bitmap
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

class AndroidCameraViewModel : ViewModel() {

    internal val filterNumber = mutableStateOf(2)
    internal val filterActive = mutableStateOf(false)
    internal val filteredBitmap: MutableState<Bitmap?> = mutableStateOf(null)
}