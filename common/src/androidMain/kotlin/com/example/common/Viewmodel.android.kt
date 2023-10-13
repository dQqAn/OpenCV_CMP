package com.example.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import androidx.lifecycle.ViewModel as AndroidXViewModel
import androidx.lifecycle.viewModelScope as androidXViewModelScope

actual abstract class ViewModel actual constructor() : AndroidXViewModel() {
    actual val viewModelScope: CoroutineScope = androidXViewModelScope

    actual override fun onCleared() {
        super.onCleared()
    }

    actual open fun myRunBlocking(block: suspend CoroutineScope.() -> Unit) = runBlocking { block() }

}


