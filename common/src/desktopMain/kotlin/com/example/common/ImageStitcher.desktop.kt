package com.example.common


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
import java.io.File
import java.net.URI

actual class StitcherInput(val uris: List<URI>, val stitchMode: Int)

actual class ImageStitcher(
    private val fileUtil: FileUtil,
) {
    actual fun stitchImages(input: StitcherInput, claheState: Boolean): Single<StitcherOutput> {
        return Single.fromCallable {
            val files = fileUtil.urisToFiles(input.uris)
            val vector = filesToMatVector(files, claheState)
            stitch(vector, input.stitchMode)
        }
    }

    private fun stitch(vector: MatVector, stitchMode: Int): StitcherOutput {
        val result = Mat()
        val stitcher = Stitcher.create(stitchMode)
        val status = stitcher.stitch(vector, result)

        fileUtil.cleanUpWorkingDirectory()
        return if (status == Stitcher.OK) {
            val resultFile = fileUtil.createResultFile()
            imwrite(resultFile.absolutePath, result)
            StitcherOutput.Success(resultFile)
        } else {
            val e = RuntimeException("Can't stitch images: " + getStatusDescription(status))
            StitcherOutput.Failure(e)
        }
    }

    @Suppress("SpellCheckingInspection")
    private fun getStatusDescription(status: Int): String {
        return when (status) {
            Stitcher.ERR_NEED_MORE_IMGS -> "ERR_NEED_MORE_IMGS"
            Stitcher.ERR_HOMOGRAPHY_EST_FAIL -> "ERR_HOMOGRAPHY_EST_FAIL"
            Stitcher.ERR_CAMERA_PARAMS_ADJUST_FAIL -> "ERR_CAMERA_PARAMS_ADJUST_FAIL"
            else -> "UNKNOWN"
        }
    }

    private fun filesToMatVector(files: List<File>, claheState: Boolean): MatVector {
        Loader.load(opencv_java::class.java)
        val images = MatVector(files.size.toLong())

        if (claheState) {
            for (i in files.indices) {
                //Clahe
//            val src = opencv_imgcodecs.imread(files[i].absolutePath, IMREAD_GRAYSCALE)
                val src = imread(files[i].absolutePath)

//            val dst = opencv_core.Mat()

                val clahe = createCLAHE(2.0, Size(8, 8))

//           resize(src,src,opencv_core.Size(500,600))

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
            }
        } else {
            for (i in files.indices) {
                //normally
                images.put(i.toLong(), imread(files[i].absolutePath))
            }
        }
        return images
    }

}
