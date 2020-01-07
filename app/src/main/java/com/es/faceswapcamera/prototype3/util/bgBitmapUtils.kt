package com.es.faceswapcamera.prototype3.util

import android.content.Context
import android.graphics.*
import android.util.Log
import android.util.Pair
import android.util.Size
import androidx.core.content.ContextCompat
import com.es.faceswapcamera.R
import com.es.faceswapcamera.prototype3.facedetection.FaceContourGraphic
import java.util.ArrayList
import kotlin.math.floor

object bgBitmapUtils {

    fun getFaceBitmap(originFaceBitmap: Bitmap,
                      context: Context,
                      points: ArrayList<FaceContourGraphic.FaceContourData>,
                      myFaceInfo: FaceContourGraphic.FaceDetectInfo): Bitmap? {
        var faceBitmap: Bitmap? = null
        val resizeBitmap = resizeImage(originFaceBitmap, myFaceInfo.canvasSize)

        resizeBitmap.let { origin ->
            val originSize = myFaceInfo.canvasSize

            val resultingImage = Bitmap.createBitmap(originSize.width,
                    originSize.height, Bitmap.Config.ARGB_8888
            )

            val canvas = Canvas(resultingImage)
            canvas.drawARGB(0, 0, 0, 0)

            val paint = Paint()
            paint.isAntiAlias = true

            val rect = Rect(0, 0, originSize.width, originSize.height)

            context?.let {
                val color = ContextCompat.getColor(it, R.color.contour_grey)
                paint.color = color
            }

            val path = Path()

            points.forEach {
                path.lineTo(it.px, it.py)
//                canvas.drawCircle(it.px, it.py, 6.0f, Paint(Color.BLUE))
            }

            canvas.drawPath(path, paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(origin/*bitmap2*/, rect, rect, paint)


            // resultingImage 얼굴은 잘 짤리지만 bitmap createBitmap resize 가 안됨

            // 얼굴크기로 bitmap resize 하기
//            myFaceInfo?.run {
            try {
                Log.i("log","resultingImage : ${resultingImage.width} ${resultingImage.height}")
                Log.i("log","myFaceInfo : $myFaceInfo ")
                val result = Bitmap.createBitmap(resultingImage, (myFaceInfo.left).toInt(), floor(myFaceInfo.top).toInt(), (myFaceInfo.rectWidth).toInt(), floor(myFaceInfo.rectHeight).toInt())
                Log.i("log","result : ${result.width} ${result.height}")

                faceBitmap = result

            } catch (e: Exception) {
                e.printStackTrace()
                faceBitmap = resultingImage
            }

//            if (!origin.isRecycled) origin.recycle()
//            if (!resultingImage.isRecycled) resultingImage.recycle()

            return faceBitmap

//                Glide.with(imageView).load(faceBitmap).into(imageView)
//            }

//            faceBitmap = resultingImage
//            Glide.with(imageView).load(resultingImage).into(imageView)

        }
        return faceBitmap
    }

    fun resizeFace(bgBitmap: Bitmap, faceBitmap: Bitmap, bgFaceInfo: FaceContourGraphic.FaceDetectInfo, myFaceInfo: FaceContourGraphic.FaceDetectInfo): Bitmap? {

        // 얼굴 face 사이즈에 맞게 bg resize
        val ratio = myFaceInfo.rectWidth / bgFaceInfo.rectWidth
        val bgSize = Size((bgBitmap.width * ratio).toInt(), (bgBitmap.height * ratio).toInt())


        val resizeFace = Bitmap.createScaledBitmap(bgBitmap, bgSize.width, bgSize.height, true)
        Log.i("changeFace","resize faceBitmap : ${resizeFace.width} ${resizeFace.height}")


        return resizeFace//resultImage
    }


    private fun resizeImage(selectBitmap: Bitmap, targetSize: Size): Bitmap {

        val targetedSize: Pair<Int, Int> = Pair(targetSize.width, targetSize.height)
        val targetWidth = targetedSize.first
        val maxHeight = targetedSize.second
        // Determine how much to scale down the image
        val scaleFactor = Math.max(
                selectBitmap.width.toFloat() / targetWidth.toFloat(),
                selectBitmap.height.toFloat() / maxHeight.toFloat()
        )
        val resizedBitmap = Bitmap.createScaledBitmap(
                selectBitmap,
                (selectBitmap.width / scaleFactor).toInt(),
                (selectBitmap.height / scaleFactor).toInt(),
                true
        )
        return resizedBitmap
    }
}