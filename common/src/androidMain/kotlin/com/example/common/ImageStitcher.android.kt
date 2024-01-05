package com.example.common

import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.net.Uri
import io.reactivex.rxjava3.core.Single
import org.bytedeco.javacpp.opencv_core
import org.bytedeco.javacpp.opencv_core.Mat
import org.bytedeco.javacpp.opencv_core.MatVector
import org.bytedeco.javacpp.opencv_imgcodecs
import org.bytedeco.javacpp.opencv_imgcodecs.imwrite
import org.bytedeco.javacpp.opencv_imgproc
import org.bytedeco.javacpp.opencv_imgproc.resize
import org.bytedeco.javacpp.opencv_stitching.Stitcher
import org.bytedeco.javacpp.opencv_stitching.Stitcher.*
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

//            vector.deallocate(true)
//            vector.setNull()
        vector.clear()
        vector.close()

        fileUtil.cleanUpWorkingDirectory()

        return if (status == Stitcher.OK) {
//            println(getAvailableMemory().availMem.div(1024).div(1024))
//            println(getAvailableMemory().totalMem.div(1024).div(1024))
//            println(getAvailableMemory().threshold.div(1024).div(1024))

            val resultFile = fileUtil.createResultFile()
            imwrite(resultFile.absolutePath, result)
            result.release()
            result.close()
            StitcherOutput.Success(resultFile)
        } else {
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
        val images = MatVector(files.size.toLong())

        if (claheState) {
            for (i in files.indices) {
                // Before doing something that requires a lot of memory,
                // check whether the device is in a low memory state.
                if (!getAvailableMemory().lowMemory) {
                    // Do memory intensive work.

                    //Clahe
//            val src = opencv_imgcodecs.imread(files[i].absolutePath, IMREAD_GRAYSCALE)
                    val src = opencv_imgcodecs.imread(files[i].absolutePath)

//            val dst = opencv_core.Mat()

                    val clahe = opencv_imgproc.createCLAHE(2.0, opencv_core.Size(8, 8))

                    resize(src, src, opencv_core.Size(1000, 800))

                    val newMat = opencv_core.Mat()
                    val newVector = MatVector(newMat)
//            cvtColor(src, dst, CV_BGR2GRAY)
                    opencv_imgproc.cvtColor(src, src, opencv_imgproc.CV_BGR2Lab)
                    opencv_core.split(src, newVector)


//            medianBlur(src, src, 3)

//            clahe.apply(src, dst)
                    clahe.apply(newVector[0], newVector[0])
                    opencv_core.merge(newVector, src)

//            if (i==1){
//                opencv_highgui.imshow("clahe",src)
//            }

//                opencv_imgproc.cvtColor(src, src, opencv_imgproc.CV_GRAY2BGR)
                    opencv_imgproc.cvtColor(src, src, opencv_imgproc.CV_Lab2BGR)

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
                    val src = opencv_imgcodecs.imread(files[i].absolutePath)
                    resize(src, src, opencv_core.Size(1000, 800))
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