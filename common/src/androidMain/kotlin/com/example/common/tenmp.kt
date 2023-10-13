/*
package com.example.common

import org.bytedeco.javacpp.opencv_core.Mat
import org.bytedeco.javacpp.opencv_core.MatVector
import org.bytedeco.javacpp.opencv_imgcodecs.imread
import org.bytedeco.javacpp.opencv_imgcodecs.imwrite
import org.bytedeco.javacpp.opencv_stitching.Stitcher
import org.bytedeco.javacpp.opencv_stitching.createStitcher


object Stitching {
    var try_use_gpu = false
    var imgs = MatVector()
    var result_name = "result.jpg"

    @JvmStatic
    fun main(args: Array<String>) {
        val retval = parseCmdArgs(args)
        if (retval != 0) {
            System.exit(-1)
        }
        val pano = Mat()
        val stitcher: Stitcher = createStitcher(try_use_gpu)
        val status: Int = stitcher.stitch(imgs, pano)
        if (status != Stitcher.OK) {
            println("Can't stitch images, error code = $status")
            System.exit(-1)
        }
        imwrite(result_name, pano)
        println("Images stitched together to make " + result_name)
    }

    fun printUsage() {
        println(
            "Rotation model images stitcher.\n\n"
                    + "stitching img1 img2 [...imgN]\n\n"
                    + "Flags:\n"
                    + "  --try_use_gpu (yes|no)\n"
                    + "      Try to use GPU. The default value is 'no'. All default values\n"
                    + "      are for CPU mode.\n"
                    + "  --output <result_img>\n"
                    + "      The default is 'result.jpg'."
        )
    }

    fun parseCmdArgs(args: Array<String>): Int {
        if (args.size == 0) {
            printUsage()
            return -1
        }
        var i = 0
        while (i < args.size) {
            if ((args[i] == "--help") || (args[i] == "/?")) {
                printUsage()
                return -1
            } else if ((args[i] == "--try_use_gpu")) {
                if ((args[i + 1] == "no")) {
                    try_use_gpu = false
                } else if ((args[i + 1] == "yes")) {
                    try_use_gpu = true
                } else {
                    println("Bad --try_use_gpu flag value")
                    return -1
                }
                i++
            } else if ((args[i] == "--output")) {
                result_name = args[i + 1]
                i++
            } else {
                val img: Mat = imread(args[i])
                if (img.empty()) {
                    println("Can't read image '" + args[i] + "'")
                    return -1
                }
                imgs.resize(imgs.size() + 1)
                imgs.put(imgs.size() - 1, img)
            }
            i++
        }
        return 0
    }
}*/
