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
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.MatOfFloat
import org.opencv.core.MatOfInt
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.File
import kotlin.math.exp


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
                stitch(files[0], vector, input.stitchMode)
            }
        }
    }

    private fun stitch(firstFile: File, vector: MatVector, stitchMode: Int): StitcherOutput {
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
//            println(result)
            result.release()
            result.close()
//            val matchedImg =
//                histogramMatching2(Imgcodecs.imread(firstFile.absolutePath), Imgcodecs.imread(resultFile.absolutePath))
//            println("1"+matchedImg)
//            println("1"+firstFile)
//            println("1"+resultFile)
//            println("1"+resultFile.exists())
//            Imgcodecs.imwrite(resultFile.absolutePath, matchedImg)
//            Imgcodecs.imwrite(resultFile.parentFile!!.absolutePath+"/asds.png", matchedImg)
//            println("1"+resultFile.exists())
//            println("1"+resultFile.parentFile)
//            println("1"+resultFile.parentFile!!.absolutePath)

            /*val sourceImage = Imgcodecs.imread(firstFile.absolutePath)
            val array = mutableListOf<Double>()
            var index = 0
            for (row in 0 until sourceImage.rows()) {
                for (col in 0 until sourceImage.cols()) {
                    for (item in sourceImage.get(row, col)) {
                        array.add(index, item)
//                        println(item)
                        index++
                    }
                }
            }
            println(array)*/


            StitcherOutput.Success(resultFile)
//            StitcherOutput.Success(matchedImg)
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

                    //println(getHueHistogram(cvIplImage(src)))
//                    calHis(Imgcodecs.imread(files[0].absolutePath))
//                    calHis(Imgcodecs.imread(files[1].absolutePath))

                    /*val sourceImg = Imgcodecs.imread(files[0].absolutePath)
                    val targetImg = Imgcodecs.imread(files[1].absolutePath)
                    val matchedImg =histogramMatching(sourceImg,targetImg)
                    // Görüntüleri göster
                    val sourceWin = "Source Image"
                    val targetWin = "Target Image"
                    val matchedWin = "Matched Image"
                    Imgcodecs.imwrite("$matchedWin.jpg", matchedImg)
                    matchedImg.release()*/
//                    HighGui.imshow(sourceWin, sourceImg)
//                    HighGui.imshow(targetWin, targetImg)
//                    HighGui.imshow(matchedWin, matchedImg)
//                    HighGui.waitKey(0)
//                    HighGui.destroyAllWindows()

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

    /*private fun getHueHistogram(image: IplImage?): CvHistogram {
        if (image == null || image.nChannels() < 3) Exception("Error!")
        val hsvImage = cvCreateImage(image!!.cvSize(), image.depth(), 3)
        cvCvtColor(image, hsvImage, CV_BGR2HSV)
        // Split the 3 channels into 3 images
        val hsvChannels: IplImageArray = splitChannels(hsvImage)
        //bins and value-range
        val numberOfBins = 255
        val minRange = 0f
        val maxRange = 180f
        // Allocate histogram object
        val dims = 1
        val sizes = intArrayOf(numberOfBins)
        val histType = CV_HIST_ARRAY
        val minMax = floatArrayOf(minRange, maxRange)
        val ranges = arrayOf(minMax)
        val uniform = 1
        val hist = cvCreateHist(dims, sizes, histType, ranges, uniform)
        // Compute histogram
        val accumulate = 1
        val mask: IplImage? = null
        cvCalcHist(hsvChannels.position(0), hist, accumulate, null)
        return hist
    }
    private fun splitChannels(hsvImage: IplImage): IplImageArray {
        val size = hsvImage.cvSize()
        val depth = hsvImage.depth()
        val channel0 = cvCreateImage(size, depth, 1)
        val channel1 = cvCreateImage(size, depth, 1)
        val channel2 = cvCreateImage(size, depth, 1)
        cvSplit(hsvImage, channel0, channel1, channel2, null)
        return IplImageArray(channel0, channel1, channel2)
    }*/

    /*private fun calHis(src:org.opencv.core.Mat){
        val tempSrc=src
        val hbins = 30
        val sbins = 32
        val histSize = MatOfInt(hbins, sbins)
        val histRange = MatOfFloat(0f, 180f, 0f, 256f)
        val imageList: MutableList<org.opencv.core.Mat> = ArrayList()
        imageList.add(tempSrc)
        val hsvHist = org.opencv.core.Mat()
        Imgproc.calcHist(imageList, MatOfInt(0, 1), org.opencv.core.Mat(), hsvHist, histSize, histRange)
        println(tempSrc)
//        cumsum(tempSrc.get(1,2))
        tempSrc.release()
        imageList.clear()
    }*/

    /*private fun cumsum(numbers: DoubleArray){
        var sum = 0.0

        //Declaring the array
        // traverse through the array
        for (i in numbers.indices) {
            // find sum
            sum += numbers[i]

            // replace
            numbers[i] = sum
        }
        println(sum)
    }*/

    /*fun interp(x: Double, xp: DoubleArray, fp: DoubleArray): Double {
        // Eğer x, xp dizisinin dışındaysa, ilk veya son değeri döndür
        if (x < xp[0]) return fp[0]
        if (x > xp[xp.size - 1]) return fp[fp.size - 1]

        // x'in hangi aralıkta olduğunu bul
        var i = 0
        while (x > xp[i]) {
            i++
        }

        // Lineer interpolasyon hesapla
        val x0 = xp[i - 1]
        val x1 = xp[i]
        val y0 = fp[i - 1]
        val y1 = fp[i]

        return y0 + (y1 - y0) * (x - x0) / (x1 - x0)
    }*/

    fun histogramMatching(source: org.opencv.core.Mat, target: org.opencv.core.Mat): org.opencv.core.Mat {
//        val sourceBW = org.opencv.core.Mat()
//        val targetBW = org.opencv.core.Mat()
//        Imgproc.cvtColor(source, sourceBW, Imgproc.COLOR_BGR2GRAY)
//        Imgproc.cvtColor(target, targetBW, Imgproc.COLOR_BGR2GRAY)

        val sourceHist = org.opencv.core.Mat()
        val targetHist = org.opencv.core.Mat()
        val histSize = MatOfInt(256)
        val histRange = MatOfFloat(0f, 256f)
//        resize(src, src, Size(1000, 800))
        val tempSource = org.opencv.core.Mat()
        val tempTarget = org.opencv.core.Mat()

        Imgproc.resize(source, tempSource, org.opencv.core.Size(1000.0, 800.0))
        Imgproc.resize(target, tempTarget, org.opencv.core.Size(1000.0, 800.0))

//        Imgproc.calcHist(listOf(sourceBW), MatOfInt(0), org.opencv.core.Mat(), sourceHist, histSize, histRange)
        Imgproc.calcHist(listOf(tempSource), MatOfInt(0), org.opencv.core.Mat(), sourceHist, histSize, histRange)
//        Imgproc.calcHist(listOf(targetBW), MatOfInt(0), org.opencv.core.Mat(), targetHist, histSize, histRange)
        Imgproc.calcHist(listOf(tempTarget), MatOfInt(0), org.opencv.core.Mat(), targetHist, histSize, histRange)

        val sourceCdf = cumulativeSum(sourceHist)
        val targetCdf = cumulativeSum(targetHist)

        Core.normalize(sourceCdf, sourceCdf, 0.0, 1.0, Core.NORM_MINMAX)
        Core.normalize(targetCdf, targetCdf, 0.0, 1.0, Core.NORM_MINMAX)

        // Eşleme işlemi için interpolasyon yap
        val lut = org.opencv.core.Mat()
        Imgproc.resize(targetCdf, lut, sourceCdf.size())

        val matchedImage = org.opencv.core.Mat()
        Core.LUT(tempSource, lut, matchedImage)

        return matchedImage
    }

    fun cumulativeSum(input: org.opencv.core.Mat): org.opencv.core.Mat {
        val output = org.opencv.core.Mat(input.size(), input.type())
        // Her bir kanal için ayrı ayrı kumulatif toplam hesapla
        for (channel in 0 until input.channels()) {
            var sum = 0.0
            for (i in 0 until input.cols()) {
                for (j in 0 until input.rows()) {
                    val pixelValue = input.get(j, i)[channel]
                    sum += pixelValue
                    output.put(j, i, sum)
                }
            }
        }
        return output
    }

    fun histogramMatching2(sourceImage: org.opencv.core.Mat, targetImage: org.opencv.core.Mat): org.opencv.core.Mat {
        // Kaynak ve hedef resimlerin histogramlarını hesapla
        val sourceHist = org.opencv.core.Mat()
        val targetHist = org.opencv.core.Mat()
//        val sourceChannels = mutableListOf<org.opencv.core.Mat>()
//        Core.split(sourceImage,sourceChannels)
//        val targetChannels = mutableListOf<org.opencv.core.Mat>()
//        Core.split(targetImage,targetChannels)

        val hsvSource = org.opencv.core.Mat()
        val hsvTarget = org.opencv.core.Mat()
        Imgproc.cvtColor(sourceImage, hsvSource, Imgproc.COLOR_BGR2RGB)
        Imgproc.cvtColor(targetImage, hsvTarget, Imgproc.COLOR_BGR2RGB)

        Imgproc.calcHist(
            listOf(hsvSource),
            MatOfInt(0, 1, 2),
            org.opencv.core.Mat(),
            sourceHist,
            MatOfInt(256, 256, 256),
//            MatOfInt(31, 31, 31),
            MatOfFloat(0.0f, 255.0f, 0.0f, 255.0f, 0.0f, 255.0f),
            false
        )
        Imgproc.calcHist(
            listOf(hsvTarget),
            MatOfInt(0, 1, 2),
            org.opencv.core.Mat(),
            targetHist,
            MatOfInt(256, 256, 256),
//            MatOfInt(31, 31, 31),
            MatOfFloat(0.0f, 255.0f, 0.0f, 255.0f, 0.0f, 255.0f),
            false
        )

        // Her iki histogramın CDF'lerini hesapla
        val sourceCDF = calculateCDF(sourceHist)
        val targetCDF = calculateCDF(targetHist)

        // Histogram eşleme haritasını oluştur
        val mapping = createMapping(sourceCDF, targetCDF)
        // Yeni görüntüyü oluştur
        val resultImage = org.opencv.core.Mat(sourceImage.size(), sourceImage.type())
        Core.LUT(sourceImage, MatOfInt(*mapping), resultImage)

        // Kumulatif dağılım fonksiyonlarını hesapla
//        val sourceCDF = calculateCDF(sourceHist)
//        val targetCDF = calculateCDF(targetHist)

        /*// Eşleme fonksiyonunu oluştur
        val mapping = createMapping(sourceCDF, targetCDF)

        // Yeni resmi oluştur ve eşleme dönüşümünü uygula
        val resultImage = org.opencv.core.Mat(sourceImage.rows(), sourceImage.cols(), sourceImage.type())
        Core.LUT(sourceImage, MatOfInt(*mapping), resultImage)*/

//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME)

//        val blackMat = org.opencv.core.Mat.zeros(100, 100, CV_8UC1)
//        return blackMat
        return resultImage
//        println(targetHist)
//        return targetHist
    }

    /*fun flatten(mat:org.opencv.core.Mat):DoubleArray{

        }*/

    fun calculateLookup(srcCdf: DoubleArray, refCdf: DoubleArray): IntArray {
        val lookupTable = IntArray(256)
        var lookupVal = 0

        for (srcPixelVal in srcCdf.indices) {
            for (refPixelVal in refCdf.indices) {
                if (refCdf[refPixelVal] >= srcCdf[srcPixelVal]) {
                    lookupVal = refPixelVal
                    break
                }
            }
            lookupTable[srcPixelVal] = lookupVal
        }

        return lookupTable
    }

    fun flattenHistogram(histogram: Array<IntArray>): IntArray {
        val flattened = mutableListOf<Int>()
        for (arr in histogram) {
            flattened.addAll(arr.asList())
        }
        return flattened.toIntArray()
    }

    fun getCumulativeSum(arr: IntArray): IntArray {
        val prefixSum = IntArray(arr.size)
        prefixSum[0] = arr[0]
        for (i in 1 until arr.size) {
            prefixSum[i] = prefixSum[i - 1] + arr[i]
        }

        return prefixSum
    }

    fun getCumulativeSum2(arr: IntArray): IntArray {
        val prefixSum = IntArray(arr.size)
        for (i in arr.indices) {
            var prefix = 0
            for (j in 0..i) {
                prefix += arr[j]
            }
            prefixSum[i] = prefix
        }
        return prefixSum
    }

    fun CNDF(x: Double): Double {
        var x = x
        val neg = if ((x < 0.0)) 1 else 0
        if (neg == 1) x *= -1.0

        val k = (1.0 / (1.0 + 0.2316419 * x))
        var y = ((((1.330274429 * k - 1.821255978) * k + 1.781477937) *
                k - 0.356563782) * k + 0.319381530) * k
        y = 1.0 - 0.398942280401 * exp(-0.5 * x * x) * y

        return (1.0 - neg) * y + neg * (1.0 - y)
    }

    private fun cumulative_sum(num: Int): Int {
        var count = 0

        for (k in 1..num) {
            var number = k
            while (number > 0) {
                count += number % 10
                number /= 10
            }
        }
        return count
    }

    fun calculateCDF(histogram: org.opencv.core.Mat): DoubleArray {
        // Calculate the cumulative sum of the elements
        val cdf = org.opencv.core.Mat.zeros(histogram.size(), CvType.CV_64F)
        var sum = 0.0
        for (i in 0 until histogram.rows()) {
            for (j in 0 until histogram.cols()) {
                sum += histogram[i, j][0]
                cdf.put(i, j, sum)
            }
        }
        // Normalize the cdf
        val maxCdf = Core.minMaxLoc(cdf).maxVal
        cdf.convertTo(cdf, CvType.CV_64F, 1.0 / maxCdf)

        // Convert the result to a DoubleArray
        val cdfArray = DoubleArray(cdf.total().toInt())
        cdf.get(0, 0, cdfArray)
        return cdfArray
    }

    /*fun calculateCDF(hist: org.opencv.core.Mat): DoubleArray {
        val cdf = DoubleArray(256)
        var sum = 0.0
        for (i in 0 until hist.rows()) {
            sum += hist.get(i, 0)[0]
            cdf[i] = sum / (hist.rows() * hist.cols())
        }
        return cdf
    }*/

    /*fun calculateCDF(hist: org.opencv.core.Mat): DoubleArray {
        val cdf = DoubleArray(256)
        var sum = 0.0
        val totalPixels = hist.total() // Toplam piksel sayısı
        for (i in 0 until hist.rows()) {
            sum += hist.get(i, 0)[0]
            cdf[i] = sum / totalPixels // CDF'yi normalleştir
        }
        return cdf
    }*/

    /*fun calculateCDF(hist: org.opencv.core.Mat): DoubleArray {
        val cdf = DoubleArray(256)
        var sum = 0.0
        for (i in 0 until 256) {
            sum += hist.get(i, 0)[0]
            cdf[i] = sum
        }
        // Normalleştirme
        val total = hist.total()
        for (i in 0 until 256) {
            cdf[i] /= total.toDouble()
        }
        return cdf
    }*/

    /*fun createMapping(sourceCDF: DoubleArray, targetCDF: DoubleArray): IntArray {
        val mapping = IntArray(256)
        var sourceIndex = 0
        var targetIndex = 0

        while (sourceIndex < 256 && targetIndex < 256) {
            val diffSource = Math.abs(sourceCDF[sourceIndex] - targetCDF[targetIndex])
            val diffNextSource = if (sourceIndex < 255) Math.abs(sourceCDF[sourceIndex + 1] - targetCDF[targetIndex]) else Double.MAX_VALUE
            val diffPrevTarget = if (targetIndex > 0) Math.abs(sourceCDF[sourceIndex] - targetCDF[targetIndex - 1]) else Double.MAX_VALUE

            if (diffSource < diffNextSource && diffSource < diffPrevTarget) {
                mapping[sourceIndex] = targetIndex
                sourceIndex++
                targetIndex++
            } else if (diffNextSource < diffPrevTarget) {
                sourceIndex++
            } else {
                targetIndex++
            }
        }

        return mapping
    }*/

    fun createMapping(sourceCDF: DoubleArray, targetCDF: DoubleArray): IntArray {
        val mapping = IntArray(256)
        for (i in 0 until 256) {
            var bestMatch = 0
            var minDiff = Double.MAX_VALUE
            for (j in 0 until 256) {
                val diff = Math.abs(sourceCDF[i] - targetCDF[j])
                if (diff < minDiff) {
                    minDiff = diff
                    bestMatch = j
                }
            }
            mapping[i] = bestMatch
        }
        return mapping
    }
}