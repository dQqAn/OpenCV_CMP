package com.example.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking

actual abstract class ViewModel {

    actual val viewModelScope: CoroutineScope =
        CoroutineScope(Dispatchers.IO) //    actual val viewModelScope: CoroutineScope = MainScope()

    protected actual open fun onCleared() {
        viewModelScope.cancel()
    }

    actual open fun myRunBlocking(block: suspend CoroutineScope.() -> Unit) = runBlocking { block() }
}