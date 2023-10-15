package com.example.common


import io.reactivex.rxjava3.core.Single
import org.bytedeco.javacpp.opencv_core
import org.bytedeco.javacpp.opencv_imgcodecs
import org.bytedeco.javacpp.opencv_imgproc
import org.bytedeco.javacpp.opencv_stitching
import java.io.File
import java.net.URI

actual class StitcherInput(val uris: List<URI>, val stitchMode: Int)

actual class ImageStitcher actual constructor(
    private val fileUtil: FileUtil
) {
    actual fun stitchImages(input: StitcherInput): Single<StitcherOutput> {
        return Single.fromCallable {
            val files = fileUtil.urisToFiles(input.uris)
            val vector = filesToMatVector(files)
            stitch(vector, input.stitchMode)
        }
    }

    private fun stitch(vector: opencv_core.MatVector, stitchMode: Int): StitcherOutput {
        val result = opencv_core.Mat()
        val stitcher = opencv_stitching.Stitcher.create(stitchMode)
        val status = stitcher.stitch(vector, result)

        fileUtil.cleanUpWorkingDirectory()
        return if (status == opencv_stitching.Stitcher.OK) {
            val resultFile = fileUtil.createResultFile()
            opencv_imgcodecs.imwrite(resultFile.absolutePath, result)
            StitcherOutput.Success(resultFile)
        } else {
            val e = RuntimeException("Can't stitch images: " + getStatusDescription(status))
            StitcherOutput.Failure(e)
        }
    }

    @Suppress("SpellCheckingInspection")
    private fun getStatusDescription(status: Int): String {
        return when (status) {
            opencv_stitching.Stitcher.ERR_NEED_MORE_IMGS -> "ERR_NEED_MORE_IMGS"
            opencv_stitching.Stitcher.ERR_HOMOGRAPHY_EST_FAIL -> "ERR_HOMOGRAPHY_EST_FAIL"
            opencv_stitching.Stitcher.ERR_CAMERA_PARAMS_ADJUST_FAIL -> "ERR_CAMERA_PARAMS_ADJUST_FAIL"
            else -> "UNKNOWN"
        }
    }

    private fun filesToMatVector(files: List<File>): opencv_core.MatVector {
        val images = opencv_core.MatVector(files.size.toLong())

        for (i in files.indices) {
            //normally
//            images.put(i.toLong(), opencv_imgcodecs.imread(files[i].absolutePath))

            //Clahe
//            val src = opencv_imgcodecs.imread(files[i].absolutePath, IMREAD_GRAYSCALE)
            val src = opencv_imgcodecs.imread(files[i].absolutePath)

//            val dst = opencv_core.Mat()

            val clahe = opencv_imgproc.createCLAHE(2.0, opencv_core.Size(8, 8))

//           resize(src,src,opencv_core.Size(500,600))

//            cvtColor(src, dst, CV_BGR2GRAY)
            opencv_imgproc.cvtColor(src, src, opencv_imgproc.CV_BGR2GRAY)

//            medianBlur(src, src, 3)

//            clahe.apply(src, dst)
            clahe.apply(src, src)

//            if (i==1){
//                opencv_highgui.imshow("clahe",src)
//            }

            opencv_imgproc.cvtColor(src, src, opencv_imgproc.CV_GRAY2BGR)

//            if (i==1){
//                opencv_highgui.imshow("clahe2",src)
//                opencv_highgui.waitKey(0)
//            }

            //            images.put(i.toLong(), dst)
            images.put(i.toLong(), src)
        }
        return images
    }

}
