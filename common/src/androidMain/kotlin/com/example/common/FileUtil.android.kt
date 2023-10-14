package com.example.common

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment.DIRECTORY_PICTURES
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import io.reactivex.rxjava3.subjects.PublishSubject
import org.bytedeco.javacpp.opencv_stitching
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


actual class FileUtil(
    private val context: Context,
    private val androidActivityViewModel: AndroidActivityViewModel,
) {
    actual val stitcherInputRelay = PublishSubject.create<StitcherInput>()

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    actual fun chooseImages(
        openGallery: MutableState<Boolean>,
        isScansChecked: MutableState<Boolean>,
        outputFile: MutableState<File?>
    ) {
        //https://github.com/bytedeco/javacv/issues/1127#issuecomment-619118652

        val multiplePermissionsState = rememberMultiplePermissionsState(
            listOf(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
            )
        )

        val launcher =
            rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) {//ActivityResultContracts.TakePicturePreview()
                openGallery.value = false
                outputFile.value = null
                processImages(it, isScansChecked.value)
            }

        LaunchedEffect(openGallery.value) {
            if (openGallery.value) {
                if (multiplePermissionsState.allPermissionsGranted) {
                    launcher.launch("image/*")
                } else {
                    multiplePermissionsState.launchMultiplePermissionRequest()
                    openGallery.value = false
                }
            }
        }

        LaunchedEffect(multiplePermissionsState.allPermissionsGranted) {
            if (multiplePermissionsState.allPermissionsGranted && openGallery.value) {
                launcher.launch("image/*")
            }
        }
    }

    actual fun showImage(
        file: File,
        imageBitmap: MutableState<ImageBitmap?>
    ) {
        imageBitmap.value = BitmapFactory.decodeFile(file.absolutePath).asImageBitmap()
    }

    actual fun processImages(uris: List<Any?>, isScansChecked: Boolean) {
        val stitchMode = if (isScansChecked) opencv_stitching.Stitcher.SCANS else opencv_stitching.Stitcher.PANORAMA
        if (uris.filterIsInstance<Uri>().isNotEmpty()) {
            stitcherInputRelay.onNext(StitcherInput(uris.filterIsInstance<Uri>(), stitchMode))
        }
    }

    @Throws(IOException::class)
    fun urisToFiles(uris: List<Uri>): List<File> {
        val files = ArrayList<File>(uris.size)
        for (uri in uris) {
            val file = createTempFile(requireTemporaryDirectory())
            writeUriToFile(uri, file)
            files.add(file)
        }
        return files
    }

    fun createResultFile(): File {
        val pictures = context.getExternalFilesDir(DIRECTORY_PICTURES)!!
//            val pictures = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!
        println("result: " + pictures)
//        println("DIRECTORY_PICTURES: "+DIRECTORY_PICTURES)
        return createTempFile(File(pictures, RESULT_DIRECTORY_NAME))
    }

    fun cleanUpWorkingDirectory() {
        requireTemporaryDirectory().Remove()
    }

    @Throws(IOException::class)
    private fun createTempFile(root: File): File {
        root.mkdirs() // make sure that the directory exists
        val date = SimpleDateFormat(DATE_FORMAT_TEMPLATE, Locale.getDefault()).format(Date())
        val filePrefix = IMAGE_NAME_TEMPLATE.format(date)
//        println(root)
        return File.createTempFile(filePrefix, JPG_EXTENSION, root)
    }

    @Throws(IOException::class)
    private fun writeUriToFile(target: Uri, destination: File) {
        val inputStream = context.contentResolver.openInputStream(target)!!
        val outputStream = FileOutputStream(destination)
        inputStream.use { input ->
            outputStream.use { out ->
                input.copyTo(out)
            }
        }
    }

    private fun requireTemporaryDirectory(): File {
        // don't need to read / write permission for this directory starting from android 19
        val pictures = context.getExternalFilesDir(DIRECTORY_PICTURES)!!
        return File(pictures, TEMPORARY_DIRECTORY_NAME)
    }

    // there is no build in function for deleting folders <3
    private fun File.Remove() {
        if (isDirectory) {
            val entries = listFiles()
            if (entries != null) {
                for (entry in entries) {
                    entry.Remove()
                }
            }
        }
        delete()
    }

    companion object {
        private const val TEMPORARY_DIRECTORY_NAME = "Temporary"
        private const val RESULT_DIRECTORY_NAME = "Results"
        private const val DATE_FORMAT_TEMPLATE = "yyyyMMdd_HHmmss"
        private const val IMAGE_NAME_TEMPLATE = "IMG_%s_"
        private const val JPG_EXTENSION = ".jpg"

        private const val EXTRA_ALLOW_MULTIPLE = "android.intent.extra.ALLOW_MULTIPLE"
        private const val INTENT_IMAGE_TYPE = "image/*"
        private const val CHOOSE_IMAGES = 777
    }
}
