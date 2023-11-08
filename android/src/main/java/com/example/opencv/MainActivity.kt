package com.example.opencv

import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalContext
import com.example.common.AndroidActivityViewModel
import com.example.common.CameraViewModel
import com.example.common.MainPage
import com.example.common.StitcherViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val stitcherViewModel: StitcherViewModel by viewModel()
    private val androidActivityViewModel: AndroidActivityViewModel by viewModel()
    private val cameraViewModel: CameraViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            MaterialTheme {
                androidActivityViewModel.activity.value = context.getActivity()?.apply {
                    MainPage(stitcherViewModel, cameraViewModel)
                }
            }
        }
    }

    private fun Context.getActivity(): ComponentActivity? = when (this) {
        is ComponentActivity -> this
        is ContextWrapper -> baseContext.getActivity()
        else -> null
    }

}
