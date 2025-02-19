package com.example.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File

class StitcherViewModel(
    private val fileUtil: FileUtil,
    private val imageStitcher: ImageStitcher,
) : ViewModel() {

    //    private lateinit var disposable: Disposable
    private var compositeDisposables: CompositeDisposable = CompositeDisposable()

    internal var imageBitmap: MutableState<ImageBitmap?> = mutableStateOf(null)

    internal var isScansChecked = mutableStateOf(true)

//    internal var images: MutableState<Any?> = mutableStateOf(null)

    internal var isOpenGallery = mutableStateOf(false)

    internal var outputFile: MutableState<File?> = mutableStateOf(null)

    internal val claheState: MutableState<Boolean> = mutableStateOf(false)
    internal fun changeClaheState(state: Boolean) {
        claheState.value = state
    }

    internal val pyTorchState: MutableState<Boolean> = mutableStateOf(false)
    internal fun changePtTorchState(state: Boolean) {
        pyTorchState.value = state
    }

    internal val maxFirstScoreText: MutableState<String?> = mutableStateOf(null)
    internal val maxSecondScoreText: MutableState<String?> = mutableStateOf(null)
    internal val classNameFirstText: MutableState<String?> = mutableStateOf(null)
    internal val classNameSecondText: MutableState<String?> = mutableStateOf(null)

    init {
        //https://answers.opencv.org/question/129623/hello-trying-to-create-a-cvmat-got-insufficient-memory/
//        setUpStitcher()
//        chooseImages()
    }

    fun setUpStitcher() {
        /*disposable = fileUtil.stitcherInputRelay.switchMapSingle { stitcherInput ->
            imageStitcher.stitchImages(stitcherInput, claheState.value)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnSubscribe {
                    RxJavaPlugins.setErrorHandler { th ->
                        println(th.localizedMessage)
                    }
                }
                .doOnSuccess {

                }
        }
            .subscribe({
                processResult(it)
            }, {
                processError(it)
            })*/

        isOpenGallery.value = true
        outputFile.value = null
        imageBitmap.value = null
        maxFirstScoreText.value = null
        classNameFirstText.value = null
        maxSecondScoreText.value = null
        classNameSecondText.value = null

        compositeDisposables.clear()
        compositeDisposables.add(
            fileUtil.stitcherInputRelay.switchMapSingle { stitcherInput ->
                imageStitcher.stitchImages(stitcherInput, claheState.value)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .doOnSubscribe {
                        RxJavaPlugins.setErrorHandler { th ->
                            println(th.localizedMessage)
                        }
                    }.doOnSuccess {

                    }.doOnError {

                    }
            }
                .subscribe({
                    processResult(it)
//                    compositeDisposables.clear()
                }, {
                    processError(it)
//                    compositeDisposables.clear()
                }, {

                })
        )

        //todo(set disposable into gallery manager)
        //todo(set low bitmap size)
        //todo(set try catch panorama & scans)
        //todo(set null to imageBitmap)
        //todo(clear cache in every stitch array)
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
                showImage(output.file, imageBitmap)
                if (pyTorchState.value) {
                    fileUtil.PyTorchTexts(
                        output.file,
                        maxFirstScoreText,
                        maxSecondScoreText,
                        classNameFirstText,
                        classNameSecondText
                    )
                }
            }

            is StitcherOutput.Failure -> {
                processError(output.e)
            }
        }
    }

    private fun showImage(
        file: File,
        imageBitmap: MutableState<ImageBitmap?>,
    ) {
        fileUtil.showImage(file, imageBitmap)
        outputFile.value = null
    }

    override fun onCleared() {
//        disposable.dispose()
        compositeDisposables.dispose()
    }
}