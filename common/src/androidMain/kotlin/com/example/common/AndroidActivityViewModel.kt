package com.example.common

import androidx.activity.ComponentActivity
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class AndroidActivityViewModel(

) : ViewModel() {
    val activity: MutableState<ComponentActivity?> = mutableStateOf(null)
}