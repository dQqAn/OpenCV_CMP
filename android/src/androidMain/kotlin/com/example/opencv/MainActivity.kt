package com.example.opencv

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalContext
import com.example.common.AndroidActivityViewModel
import com.example.common.CameraViewModel
import com.example.common.StitcherViewModel
import com.example.common.navigation_native.localContent
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class MainActivity : AppCompatActivity() {

    private val stitcherViewModel: StitcherViewModel by viewModel()
    private val androidActivityViewModel: AndroidActivityViewModel by viewModel()
    private val cameraViewModel: CameraViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            if (Build.VERSION.SDK_INT >= 30) { //check android api level chart
                val dexOutputDir: File = codeCacheDir
                dexOutputDir.setReadOnly()
            }

            val context = LocalContext.current
            MaterialTheme {
                androidActivityViewModel.activity.value = context.getActivity()?.apply {
                    localContent(stitcherViewModel, cameraViewModel)
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
