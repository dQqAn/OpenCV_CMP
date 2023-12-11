package com.example.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import io.reactivex.rxjava3.subjects.PublishSubject
import org.bytedeco.javacpp.opencv_stitching
import java.awt.image.BufferedImage
import java.io.*
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*
import javax.imageio.ImageIO
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter


actual class FileUtil(

) {
    actual val stitcherInputRelay = PublishSubject.create<StitcherInput>()

    @Composable
    actual fun chooseImages(
        openGallery: MutableState<Boolean>,
        isScansChecked: MutableState<Boolean>,
        outputFile: MutableState<File?>
    ) {

        LaunchedEffect(openGallery.value) {
            if (openGallery.value) {

                //https://www.geeksforgeeks.org/java-swing-jfilechooser/
                val fc = JFileChooser()
//                val fc = JFileChooser("c:")
//                val fc = JFileChooser(FileSystemView.getFileSystemView())
//                val fc = JFileChooser(FileSystemView.getFileSystemView().homeDirectory)

//                fc.currentDirectory = File("C://Program Files//")
//                fc.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY;

                fc.isAcceptAllFileFilterUsed = false;
                fc.isMultiSelectionEnabled = true

                fc.dialogTitle = "Select .jpeg, .png or .jpg files"
                val restrict = FileNameExtensionFilter(".jpeg, .png or .jpg files", "jpeg", "png", "jpg")
                fc.addChoosableFileFilter(restrict)

//                val result: Int = fc.showOpenDialog(null)
                val result: Int = fc.showSaveDialog(null)

                if (result == JFileChooser.APPROVE_OPTION) {
//                    val selectedFile: File? = fc.selectedFile
                    val selectedFiles: Array<File?> = fc.selectedFiles
                    val fileList: MutableList<URI> = mutableListOf()
                    selectedFiles.forEachIndexed { index, file ->
                        file?.let {
                            fileList.add(it.toURI())
                        }
                    }
//                    images.value = fileList.toList()
                    openGallery.value = false
                    outputFile.value = null
                    processImages(fileList.toList(), isScansChecked.value)
                } else {
                    openGallery.value = false
                }
            }
        }
    }

    actual fun showImage(
        file: File,
        imageBitmap: MutableState<ImageBitmap?>
    ) {
        val img: BufferedImage? = ImageIO.read(file)
//        val icon = ImageIcon(img)
//        Image(img!!.toComposeImageBitmap(), "")
        imageBitmap.value = img!!.toComposeImageBitmap()
    }

    actual fun processImages(uris: List<Any?>, isScansChecked: Boolean) {
        val stitchMode = if (isScansChecked) opencv_stitching.Stitcher.SCANS else opencv_stitching.Stitcher.PANORAMA
        if (uris.filterIsInstance<URI>().isNotEmpty()) {
//            println(uris.filterIsInstance<Uri>())
            stitcherInputRelay.onNext(StitcherInput(uris.filterIsInstance<URI>(), stitchMode))
        }
    }

    @Throws(IOException::class)
    fun urisToFiles(uris: List<URI>): List<File> {
        val files = ArrayList<File>(uris.size)
        for (uri in uris) {
            val file = createTempFile(requireTemporaryDirectory())
            writeUriToFile(uri, file)
            files.add(file)
        }
        return files
    }

    fun createResultFile(): File {
        val picturesPath = "C://temp"
        return createTempFile(File(picturesPath, RESULT_DIRECTORY_NAME))
    }

    fun cleanUpWorkingDirectory() {
        requireTemporaryDirectory().remove()
    }

    @Throws(IOException::class)
    private fun createTempFile(root: File): File {
        root.mkdirs() // make sure that the directory exists
        val date = SimpleDateFormat(DATE_FORMAT_TEMPLATE, Locale.getDefault()).format(Date())
        val filePrefix = IMAGE_NAME_TEMPLATE.format(date)
        return File.createTempFile(filePrefix, JPG_EXTENSION, root)
    }

    @Throws(IOException::class)
    private fun writeUriToFile(target: URI, destination: File) {
        val inputStream = FileInputStream(File(target))
        val outputStream = FileOutputStream(destination)
        inputStream.use { input ->
            outputStream.use { out ->
                input.copyTo(out)
            }
        }
    }

    private fun requireTemporaryDirectory(): File {
        val picturesPath = "C://temp"
        return File(picturesPath, TEMPORARY_DIRECTORY_NAME)
    }

    // there is no build in function for deleting folders <3
    private fun File.remove() {
        if (isDirectory) {
            val entries = listFiles()
            if (entries != null) {
                for (entry in entries) {
                    entry.remove()
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
    }

    actual fun PyTorchTexts(
        outputFile: File?,
        maxFirstScoreText: MutableState<String?>,
        maxSecondScoreText: MutableState<String?>,
        classNameFirstText: MutableState<String?>,
        classNameSecondText: MutableState<String?>
    ) {

    }
}
