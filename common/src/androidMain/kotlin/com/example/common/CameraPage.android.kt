package com.example.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.webkit.MimeTypeMap
import android.widget.ImageView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.FlipCameraAndroid
import androidx.compose.material.icons.sharp.Lens
import androidx.compose.material.icons.sharp.Menu
import androidx.compose.material.icons.sharp.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ColorMatrixColorFilter
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bytedeco.javacpp.Loader
import org.bytedeco.javacpp.opencv_imgproc
import org.bytedeco.javacpp.opencv_java
import org.opencv.android.Utils.bitmapToMat
import org.opencv.android.Utils.matToBitmap
import org.opencv.core.Core.merge
import org.opencv.core.Core.split
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc.createCLAHE
import org.opencv.imgproc.Imgproc.cvtColor
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


@RequiresApi(Build.VERSION_CODES.P)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraAndroidPage(
    cameraViewModel: CameraViewModel,
    onBackClick: () -> Unit
) {

    val multiplePermissionsState = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
        )
    )
    if (multiplePermissionsState.allPermissionsGranted) {
        CameraContent(cameraViewModel, onBackClick)
    } else {
        Button(
            onClick = {
                multiplePermissionsState.launchMultiplePermissionRequest()
            }
        ) {
            Text("Take permissions")
        }
    }
}

@androidx.annotation.OptIn(ExperimentalCamera2Interop::class)
@RequiresApi(Build.VERSION_CODES.P)
@Composable
private fun CameraContent(
    cameraViewModel: CameraViewModel,
    onBackClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Button(
            onClick = {
                onBackClick()
            }
        ) {
            Text("Main Page")
        }

        CameraView(onImageCaptured = { uri, fromGallery ->

        }, onError = { imageCaptureException ->
            println(imageCaptureException)
        },
            cameraViewModel = cameraViewModel
        )
    }
}

@Composable
fun CameraView(
    onImageCaptured: (Uri, Boolean) -> Unit,
    onError: (ImageCaptureException) -> Unit,
    cameraViewModel: CameraViewModel
) {

    val context = LocalContext.current
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_FRONT) }
    val imageCapture: ImageCapture = remember {
        ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) onImageCaptured(uri, true)
    }

    CameraPreviewView(
        imageCapture,
        lensFacing,
        cameraViewModel
    ) { cameraUIAction ->
        when (cameraUIAction) {
            is CameraUIAction.OnCameraClick -> {
                imageCapture.takePicture(context, lensFacing, onImageCaptured, onError, cameraViewModel)
            }

            is CameraUIAction.OnSwitchCameraClick -> {
                lensFacing =
                    if (lensFacing == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT
                    else
                        CameraSelector.LENS_FACING_BACK
            }

            is CameraUIAction.OnGalleryViewClick -> {
                if (true == context.getOutputDirectory().listFiles()?.isNotEmpty()) {
                    galleryLauncher.launch("image/*")
                }
            }
        }
    }
}

sealed class CameraUIAction {
    data object OnCameraClick : CameraUIAction()
    data object OnGalleryViewClick : CameraUIAction()
    data object OnSwitchCameraClick : CameraUIAction()
}

private fun toGrayscale(bmpOriginal: Bitmap): Bitmap {
    val height: Int = bmpOriginal.height
    val width: Int = bmpOriginal.width
    val bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmpGrayscale)
    val paint = Paint()
    val cm = ColorMatrix()
    cm.setToSaturation(0f)
    val f = ColorMatrixColorFilter(cm)
    paint.colorFilter = f
    c.drawBitmap(bmpOriginal, 0f, 0f, paint.asFrameworkPaint())
    return bmpGrayscale
}

private fun Bitmap.rotate(degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

@Composable
private fun CameraPreviewView(
    imageCapture: ImageCapture,
    lensFacing: Int = CameraSelector.LENS_FACING_FRONT,
    cameraViewModel: CameraViewModel,
    cameraUIAction: (CameraUIAction) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val preview = Preview.Builder().build()
    val cameraSelector = CameraSelector.Builder()
        .requireLensFacing(lensFacing)
        .build()

    val imageView = remember { mutableStateOf(ImageView(context)) }
//    imageView.value.visibility = View.INVISIBLE

    val imageAnalysis = ImageAnalysis.Builder()
        // enable the following line if RGBA output is needed.
        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
//        .setTargetResolution(Size(1080, 1920))
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .setOutputImageRotationEnabled(true)
        .build()

    val previewView = remember { PreviewView(context) }
//    previewView.scaleType = PreviewView.ScaleType.FIT_CENTER

    /*val orientationEventListener = object : OrientationEventListener(context) {
        override fun onOrientationChanged(orientation: Int) {
            // Monitors orientation values to determine the target rotation value
            val rotation = when (orientation) {
                in 45 until 135 -> android.view.Surface.ROTATION_270
                in 135 until 225 -> android.view.Surface.ROTATION_180
                in 225 until 315 -> android.view.Surface.ROTATION_90
                else -> android.view.Surface.ROTATION_0
            }

            imageAnalysis.targetRotation = rotation
            imageCapture.targetRotation = rotation
        }
    }
    orientationEventListener.enable()*/

//    val filterActive=cameraViewModel.filterActive
    imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
        //        imageView.scaleType = ImageView.ScaleType.FIT_XY
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees.toFloat()
        val localBitmap = imageProxy.toBitmap()
        val filterNumber = cameraViewModel.filterNumber.value

        imageView.value.setImageBitmap(gelFilteredBitmap(localBitmap, filterNumber, rotationDegrees))

        imageProxy.close()
    }

    LaunchedEffect(lensFacing) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture,
            imageAnalysis,
        )
        preview.setSurfaceProvider(previewView.surfaceProvider)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            AndroidView({ imageView.value }, modifier = Modifier.fillMaxSize()) {

            }
            AndroidView({ previewView }, modifier = Modifier.fillMaxSize()) {

            }
        }
        Column(
            modifier = Modifier.align(Alignment.BottomCenter),
            verticalArrangement = Arrangement.Bottom
        ) {
            CameraControls(cameraUIAction, imageView, cameraViewModel)
        }
    }
}

private fun gelFilteredBitmap(bitmap: Bitmap, filterNumber: Int, rotationDegrees: Float): Bitmap {
    return when (filterNumber) {
        0 -> {
//            toGrayscale(bitmap.rotate(rotationDegrees))
            toGrayscale(bitmap)
        }

        1 -> {
            Loader.load(opencv_java::class.java)
            val mat = Mat()
            bitmapToMat(bitmap, mat)
            matToBitmap(clahe(mat), bitmap)
            bitmap
        }

        else -> {
            bitmap
        }
    }
}

suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { cameraProvider ->
        cameraProvider.addListener({
            continuation.resume(cameraProvider.get())
        }, ContextCompat.getMainExecutor(this))
    }
}

private fun clahe(mat: Mat): Mat {
    val clahe = createCLAHE(2.0, Size(8.0, 8.0))
    val vectors = mutableListOf<Mat>()
//    Converters.Mat_to_vector_Mat(mat, vectors)
    cvtColor(mat, mat, opencv_imgproc.CV_BGR2Lab)
    split(mat, vectors)
    clahe.apply(vectors[0], vectors[0])
    merge(vectors, mat)
    cvtColor(mat, mat, opencv_imgproc.CV_Lab2BGR)
    return mat
}

@Composable
fun CameraControls(
    cameraUIAction: (CameraUIAction) -> Unit,
    imageView: MutableState<ImageView>,
    cameraViewModel: CameraViewModel,
) {

    var expanded by remember { mutableStateOf(false) }
    val listItems = arrayOf("Gray", "CLAHE", "Normally")

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CameraControl(
                Icons.Sharp.Menu,
                2,
                modifier = Modifier.size(64.dp),
                onClick = { expanded = true }
            )

            CameraControl(
                Icons.Sharp.FlipCameraAndroid,
                2,
                modifier = Modifier.size(64.dp),
                onClick = { cameraUIAction(CameraUIAction.OnSwitchCameraClick) }
            )

            CameraControl(
                Icons.Sharp.Lens,
                2,
                modifier = Modifier
                    .size(64.dp)
                    .padding(1.dp)
                    .border(1.dp, Color.White, CircleShape),
                onClick = { cameraUIAction(CameraUIAction.OnCameraClick) }
            )

            CameraControl(
                Icons.Sharp.PhotoLibrary,
                2,
                modifier = Modifier.size(64.dp),
                onClick = { cameraUIAction(CameraUIAction.OnGalleryViewClick) }
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
//            properties = PopupProperties(usePlatformDefaultWidth = false),
//            modifier = Modifier.align(Alignment.BottomEnd), //doesnt work
//            offset = DpOffset(x = (-100).dp, y = (0).dp) //works
        ) {
            val filterNumber = cameraViewModel.filterNumber
//            val filterActive=cameraViewModel.filterActive
            listItems.forEachIndexed { itemIndex, itemValue ->
                DropdownMenuItem(
                    text = { Text(itemValue) },
                    onClick = {
                        expanded = false
                        when (itemIndex) {
                            0 -> {
//                                imageView.value.visibility = View.VISIBLE
//                                filterActive.value=true
                                filterNumber.value = 0
                            }

                            1 -> {
//                                imageView.value.visibility = View.VISIBLE
//                                filterActive.value=true
                                filterNumber.value = 1
                            }

                            2 -> {
//                                imageView.value.visibility = View.VISIBLE
//                                imageView.value.visibility = View.INVISIBLE
//                                filterActive.value=false
                                filterNumber.value = 2
                            }
                        }
                    },
                )
            }
        }
    }
}


@Composable
fun CameraControl(
    imageVector: ImageVector,
    contentDescId: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector,
            contentDescription = "$contentDescId",
            modifier = modifier,
            tint = Color.White
        )
    }

}

fun ImageCapture.takePicture(
    context: Context,
    lensFacing: Int,
    onImageCaptured: (Uri, Boolean) -> Unit,
    onError: (ImageCaptureException) -> Unit,
    cameraViewModel: CameraViewModel
) {
    val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
    val PHOTO_EXTENSION = ".png"
    val outputDirectory = context.getOutputDirectory()
    // Create output file to hold the image
    val photoFile = createFile(outputDirectory, FILENAME, PHOTO_EXTENSION)
//    val outputFileOptions = getOutputFileOptions(lensFacing, photoFile)

    //OutputFileOptions can be added for output image inside
    this.takePicture(ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(image: ImageProxy) {
            super.onCaptureSuccess(image)

            val rotationDegrees = image.imageInfo.rotationDegrees.toFloat()
            val bitmap = gelFilteredBitmap(image.toBitmap(), cameraViewModel.filterNumber.value, rotationDegrees)

            //way 1
            /*val os: OutputStream = BufferedOutputStream(FileOutputStream(photoFile))
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
            os.flush()
            os.close()*/

            //way 2
            cameraViewModel.viewModelScope.launch(Dispatchers.IO) {
                val fos = FileOutputStream(photoFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos) //https://stackoverflow.com/a/47971423
                fos.flush()
                fos.close()

                val savedUri = Uri.fromFile(photoFile)

                //only way 5
//            val savedUri = Uri.fromFile(outputUri?.path?.let { File(it) })

                //close comments below for way 4 & 5

                // If the folder selected is an external media directory, this is
                // unnecessary but otherwise other apps will not be able to access our
                // images unless we scan them using [MediaScannerConnection]
                val mimeType = MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(savedUri.toFile().extension)

                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(savedUri.toFile().absolutePath),
                    arrayOf(mimeType)
                ) { _, uri ->

                }

                onImageCaptured(savedUri, false)
            }


            //way 3
            /*//Convert bitmap to byte array
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos)
            val bitmapData = bos.toByteArray()

            //write the bytes in file
            val fos: FileOutputStream = FileOutputStream(photoFile)
            fos.write(bitmapData)
            fos.flush()
            fos.close()*/

            //way 4
            /*var fos: OutputStream? = null
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, photoFile.nameWithoutExtension)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
            val outputUri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            fos = outputUri?.let { context.contentResolver.openOutputStream(it) }
            fos?.use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
            contentValues.clear()
            contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
            outputUri?.let {
                context.contentResolver.update(it, contentValues, null, null)
            }*/

            //way 5
            /*MediaStore.Images.Media.insertImage(
                context.contentResolver,
                bitmap,
                photoFile.nameWithoutExtension,
                photoFile.nameWithoutExtension
            )*/

            //save image
            //except way 5
            /*val savedUri = Uri.fromFile(photoFile)

            //only way 5
//            val savedUri = Uri.fromFile(outputUri?.path?.let { File(it) })

            //close comments below for way 4 & 5

            // If the folder selected is an external media directory, this is
            // unnecessary but otherwise other apps will not be able to access our
            // images unless we scan them using [MediaScannerConnection]
            val mimeType = MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(savedUri.toFile().extension)

            MediaScannerConnection.scanFile(
                context,
                arrayOf(savedUri.toFile().absolutePath),
                arrayOf(mimeType)
            ) { _, uri ->

            }

            onImageCaptured(savedUri, false)*/
        }

        override fun onError(exception: ImageCaptureException) {
            super.onError(exception)
            onError(exception)
        }
    })

    /*this.takePicture(
        outputFileOptions,
        Executors.newSingleThreadExecutor(),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                // If the folder selected is an external media directory, this is
                // unnecessary but otherwise other apps will not be able to access our
                // images unless we scan them using [MediaScannerConnection]
                val mimeType = MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(savedUri.toFile().extension)
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(savedUri.toFile().absolutePath),
                    arrayOf(mimeType)
                ) { _, uri ->

                }
                onImageCaptured(savedUri, false)
            }

            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        })*/
}


fun getOutputFileOptions(
    lensFacing: Int,
    photoFile: File
): ImageCapture.OutputFileOptions {

    // Setup image capture metadata
    val metadata = ImageCapture.Metadata().apply {
        // Mirror image when using the front camera
        isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
    }
    // Create output options object which contains file + metadata
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
        .setMetadata(metadata)
        .build()

    return outputOptions
}

fun createFile(baseFolder: File, format: String, extension: String) =
    File(
        baseFolder, SimpleDateFormat(format, Locale.US)
            .format(System.currentTimeMillis()) + extension
    )


fun Context.getOutputDirectory(): File {
    val mediaDir = this.externalMediaDirs.firstOrNull()?.let {
        File(it, "demo").apply { mkdirs() }
    }
    return if (mediaDir != null && mediaDir.exists())
        mediaDir else this.filesDir
}
