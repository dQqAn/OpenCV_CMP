package com.example.common

import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.net.Uri
import io.reactivex.rxjava3.core.Single
import org.bytedeco.javacpp.Loader
import org.bytedeco.opencv.global.opencv_core.merge
import org.bytedeco.opencv.global.opencv_core.split
import org.bytedeco.opencv.global.opencv_imgcodecs.imread
import org.bytedeco.opencv.global.opencv_imgcodecs.imwrite
import org.bytedeco.opencv.global.opencv_imgproc.*
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.MatVector
import org.bytedeco.opencv.opencv_core.Size
import org.bytedeco.opencv.opencv_java
import org.bytedeco.opencv.opencv_stitching.Stitcher
import org.bytedeco.opencv.opencv_stitching.Stitcher.*
import java.io.File

actual class StitcherInput(val uris: List<Uri>, val stitchMode: Int)

actual class ImageStitcher(
    private val context: Context,
    private val fileUtil: FileUtil,
) {

    actual fun stitchImages(input: StitcherInput, claheState: Boolean): Single<StitcherOutput> {
        return Single.fromCallable {
            if (getAvailableMemory().lowMemory) {
                val e = RuntimeException("Can't stitch images: Low memory.")
                StitcherOutput.Failure(e)
            } else {
                val files = fileUtil.urisToFiles(input.uris)
                val vector = filesToMatVector(files, claheState)
                stitch(vector, input.stitchMode)
            }
        }
    }

    private fun stitch(vector: MatVector, stitchMode: Int): StitcherOutput {
        val result = Mat()
        val stitcher = Stitcher.create(stitchMode)

        val status = if (!getAvailableMemory().lowMemory) {
            stitcher.stitch(vector, result)
        } else {
            Stitcher.ERR_CAMERA_PARAMS_ADJUST_FAIL
        }

        fileUtil.cleanUpWorkingDirectory()

        return if (status == Stitcher.OK) {
//            println(getAvailableMemory().availMem.div(1024).div(1024))
//            println(getAvailableMemory().totalMem.div(1024).div(1024))
//            println(getAvailableMemory().threshold.div(1024).div(1024))

            // vector.deallocate(true)
            // vector.setNull()
            vector.clear()
            vector.close()

            val resultFile = fileUtil.createResultFile()
            imwrite(resultFile.absolutePath, result)
            result.release()
            result.close()
            StitcherOutput.Success(resultFile)
        } else {
            // vector.deallocate(true)
            // vector.setNull()
            vector.clear()
            vector.close()
            result.release()
            result.close()
            val e = RuntimeException("Can't stitch images: " + getStatusDescription(status))
            StitcherOutput.Failure(e)
        }
    }

    @Suppress("SpellCheckingInspection")
    private fun getStatusDescription(status: Int): String {
        return when (status) {
            ERR_NEED_MORE_IMGS -> "ERR_NEED_MORE_IMGS"
            ERR_HOMOGRAPHY_EST_FAIL -> "ERR_HOMOGRAPHY_EST_FAIL"
            ERR_CAMERA_PARAMS_ADJUST_FAIL -> "ERR_CAMERA_PARAMS_ADJUST_FAIL"
            else -> "UNKNOWN"
        }
    }

    private fun filesToMatVector(files: List<File>, claheState: Boolean): MatVector {
        Loader.load(opencv_java::class.java)
        val images = MatVector(files.size.toLong())

        if (claheState) {
            for (i in files.indices) {
                // Before doing something that requires a lot of memory,
                // check whether the device is in a low memory state.
                if (!getAvailableMemory().lowMemory) {
                    // Do memory intensive work.

                    //Clahe
//            val src = opencv_imgcodecs.imread(files[i].absolutePath, IMREAD_GRAYSCALE)
                    val src = imread(files[i].absolutePath)

//            val dst = opencv_core.Mat()

                    val clahe = createCLAHE(2.0, Size(8, 8))

                    resize(src, src, Size(1000, 800))

                    val newMat = Mat()
                    val newVector = MatVector(newMat)
//            cvtColor(src, dst, CV_BGR2GRAY)
                    cvtColor(src, src, CV_BGR2Lab)
                    split(src, newVector)

//            medianBlur(src, src, 3)

//            clahe.apply(src, dst)
                    clahe.apply(newVector[0], newVector[0])
                    merge(newVector, src)

//            if (i==1){
//                opencv_highgui.imshow("clahe",src)
//            }

//                opencv_imgproc.cvtColor(src, src, opencv_imgproc.CV_GRAY2BGR)
                    cvtColor(src, src, CV_Lab2BGR)

//            if (i==1){
//                opencv_highgui.imshow("clahe2",src)
//                opencv_highgui.waitKey(0)
//            }

                    //            images.put(i.toLong(), dst)
                    images.put(i.toLong(), src)

                    src.release()
//                    src.deallocate(true)
//                src.setNull()
                    src.close()

                    newMat.release()
//                    newMat.deallocate(true)
//                newMat.setNull()
                    newMat.close()

                    newVector.clear()
//                    newVector.deallocate(true)
//                newVector.setNull()
                    newVector.close()

                    clahe.collectGarbage()
                    clahe.clear()
//                    clahe.deallocate(true)
//                clahe.setNull()
                    clahe.close()
                } else {
                    println("Low Memory...")
                }
            }
        } else {
            for (i in files.indices) {
                if (!getAvailableMemory().lowMemory) {
                    // Do memory intensive work.
                    //normally
                    val src = imread(files[i].absolutePath)
                    resize(src, src, Size(1000, 800))
                    images.put(i.toLong(), src)
                    src.release()
                    src.close()
                } else {
                    println("Low Memory...")
                }
            }
        }
        return images
    }

    // Get a MemoryInfo object for the device's current memory status.
    private fun getAvailableMemory(): ActivityManager.MemoryInfo {
        val activityManager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        return ActivityManager.MemoryInfo().also { memoryInfo ->
            activityManager.getMemoryInfo(memoryInfo)
        }
    }
}