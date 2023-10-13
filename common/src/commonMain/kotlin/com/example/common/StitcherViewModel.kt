package com.example.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.Schedulers
import org.jetbrains.compose.resources.ExperimentalResourceApi
import java.io.File

class StitcherViewModel(
    private val fileUtil: FileUtil,
    private val imageStitcher: ImageStitcher
) : ViewModel() {

    private lateinit var disposable: Disposable

//    internal var imageBitmap: MutableState<ImageBitmap?> = mutableStateOf(null)

    internal var isScansChecked = mutableStateOf(true)

//    internal var images: MutableState<Any?> = mutableStateOf(null)

    internal var isOpenGallery = mutableStateOf(false)

    internal var outputFile: MutableState<File?> = mutableStateOf(null)

    init {
        setUpStitcher()
//        chooseImages()
    }

    fun setUpStitcher() {
        disposable = fileUtil.stitcherInputRelay.switchMapSingle { stitcherInput ->
            imageStitcher.stitchImages(stitcherInput)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnSubscribe {
                    RxJavaPlugins.setErrorHandler { th ->
                        println(th.localizedMessage)
                    }
                }
                .doOnSuccess {
//                    println("2: "+ it)
//                    println(imageBitmap.value)
                }
        }
            .subscribe({
//                println("3: "+ it)
//                println(imageBitmap.value)
//                println(images.value)

//                println(it)
                processResult(it)
            }, {
                processError(it)
            })
    }

    @Composable
    fun chooseImages() {
        fileUtil.chooseImages(isOpenGallery, isScansChecked, outputFile)
    }

    private fun processError(e: Throwable) {
        println("processError " + e.localizedMessage)
    }

    private fun processResult(
        output: StitcherOutput,
    ) {
        when (output) {
            is StitcherOutput.Success -> {

                outputFile.value = output.file

                showImage(output.file)
            }

            is StitcherOutput.Failure -> {
                processError(output.e)
            }
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    private fun showImage(
        file: File,
    ) {

    }
}