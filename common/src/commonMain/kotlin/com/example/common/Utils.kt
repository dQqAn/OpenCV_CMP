package com.example.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.ImageBitmap
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.PublishSubject
import java.io.File

expect class FileUtil {
    @Composable
    fun chooseImages(
        openGallery: MutableState<Boolean>,
        isScansChecked: MutableState<Boolean>,
        outputFile: MutableState<File?>
    )

    fun showImage(
        file: File,
        imageBitmap: MutableState<ImageBitmap?>
    )

    fun processImages(uris: List<Any?>, isScansChecked: Boolean)
    val stitcherInputRelay: PublishSubject<StitcherInput>
}

expect class ImageStitcher(fileUtil: FileUtil) {
    fun stitchImages(input: StitcherInput, claheState: Boolean): Single<StitcherOutput>
}

expect class StitcherInput

sealed class StitcherOutput {
    class Success(val file: File) : StitcherOutput()
    class Failure(val e: Exception) : StitcherOutput()
}
