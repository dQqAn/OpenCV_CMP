package com.example.common

import kotlinx.coroutines.CoroutineScope

expect abstract class ViewModel() {
    val viewModelScope: CoroutineScope
    protected open fun onCleared()

    open fun myRunBlocking(block: suspend CoroutineScope.() -> Unit)
}
