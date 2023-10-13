package com.example.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment.DIRECTORY_PICTURES
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import io.reactivex.rxjava3.subjects.PublishSubject
import org.bytedeco.javacpp.opencv_stitching
import org.jetbrains.compose.resources.ExperimentalResourceApi
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

    @OptIn(ExperimentalPermissionsApi::class, ExperimentalResourceApi::class)
    @Composable
    actual fun chooseImages(
        openGallery: MutableState<Boolean>,
        isScansChecked: MutableState<Boolean>,

        //https://github.com/bytedeco/javacv/issues/1127#issuecomment-619118652
        outputFile: MutableState<File?>
    ) {

        val multiplePermissionsState = rememberMultiplePermissionsState(
            listOf(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
            )
        )
        if (multiplePermissionsState.allPermissionsGranted) {
            Text("Storage permissions Granted! Thank you!")
        } else {
            Column {
                Text(
                    getTextToShowGivenPermissions(
                        multiplePermissionsState.revokedPermissions,
                        multiplePermissionsState.shouldShowRationale
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { multiplePermissionsState.launchMultiplePermissionRequest() }) {
                    Text("Request permissions")
                }
            }
        }

//        var selectImages by remember { mutableStateOf(listOf<Uri>()) }
//        images.value = remember { mutableStateOf<Uri?>(null) }
        val launcher =
            rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) {//ActivityResultContracts.TakePicturePreview()
//                images.value = it
                openGallery.value = false
                outputFile.value = null
                processImages(it, isScansChecked.value)
            }

        LaunchedEffect(openGallery.value) {
            if (openGallery.value) {
                launcher.launch("image/*")
            }
        }

        //https://answers.opencv.org/question/129623/hello-trying-to-create-a-cvmat-got-insufficient-memory/
        outputFile.value?.let {
//            val bitmap = resource(it.absolutePath).rememberImageBitmap()
//            println(bitmap)
//            println("o: " + it)
            Image(
                rememberAsyncImagePainter(it),
                contentDescription = ""
            )
//            resource(it.absolutePath).rememberImageBitmap()
        }
    }

    @Composable
    actual fun showImage(
        file: File,
        imageBitmap: MutableState<ImageBitmap?>
    ) {
//        Image(
//            rememberAsyncImagePainter(file),
//            contentDescription = ""
//        )
    }

    actual fun processImages(uris: List<Any?>, isScansChecked: Boolean) {
        val stitchMode = if (isScansChecked) opencv_stitching.Stitcher.SCANS else opencv_stitching.Stitcher.PANORAMA
        if (uris.filterIsInstance<Uri>().isNotEmpty()) {
//            println(uris.filterIsInstance<Uri>())
//            println(stitchMode)
//            println(isScansChecked)
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

actual fun ByteArray.toImageBitmap(): ImageBitmap = toAndroidBitmap().asImageBitmap()

fun ByteArray.toAndroidBitmap(): Bitmap {
    return BitmapFactory.decodeByteArray(this, 0, size)
}

@OptIn(ExperimentalPermissionsApi::class)
private fun getTextToShowGivenPermissions(
    permissions: List<PermissionState>,
    shouldShowRationale: Boolean
): String {
    val revokedPermissionsSize = permissions.size
    if (revokedPermissionsSize == 0) return ""

    val textToShow = StringBuilder().apply {
        append("The ")
    }

    for (i in permissions.indices) {
        textToShow.append(permissions[i].permission)
        when {
            revokedPermissionsSize > 1 && i == revokedPermissionsSize - 2 -> {
                textToShow.append(", and ")
            }

            i == revokedPermissionsSize - 1 -> {
                textToShow.append(" ")
            }

            else -> {
                textToShow.append(", ")
            }
        }
    }
    textToShow.append(if (revokedPermissionsSize == 1) "permission is" else "permissions are")
    textToShow.append(
        if (shouldShowRationale) {
            " important. Please grant all of them for the app to function properly."
        } else {
            " denied. The app cannot function without them."
        }
    )
    return textToShow.toString()
}