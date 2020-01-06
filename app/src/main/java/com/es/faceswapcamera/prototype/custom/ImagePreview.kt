package com.es.faceswapcamera.prototype.custom

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.RelativeLayout
import com.es.faceswapcamera.prototype.facedetection.FaceContourGraphic


class ImagePreview(context: Context?, attrs: AttributeSet?) : SurfaceView(context, attrs), SurfaceHolder.Callback{

    var viewHolder : SurfaceHolder = holder

    var bgInfo : FaceContourGraphic.FaceDetectInfo? = null

    var originBitmapForBg : Bitmap? = null

    var deltaPx: Int = 0

    var deltaPy: Int = 0


    init {

        viewHolder.addCallback(this)
        setZOrderOnTop(true)

        // 겹쳐서 배경이 보일수 있도록 투명
        viewHolder.setFormat(PixelFormat.TRANSPARENT)

    }

    fun updatePreviewInfo(bgInfo : FaceContourGraphic.FaceDetectInfo, originBitmapForBg : Bitmap) {
        this.bgInfo = bgInfo
        this.originBitmapForBg = originBitmapForBg

        this.layoutParams = RelativeLayout.LayoutParams(originBitmapForBg.width, originBitmapForBg.height).apply {
            addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            addRule(RelativeLayout.ALIGN_PARENT_END)
        }

        viewHolder.setFixedSize(originBitmapForBg.width, originBitmapForBg.height)

        deltaPx = deltaPx(originBitmapForBg.width)
        deltaPy = deltaPy(originBitmapForBg.height)
        postInvalidate()
    }

    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
    }

    override fun surfaceDestroyed(p0: SurfaceHolder?) {
    }

    override fun surfaceCreated(p0: SurfaceHolder?) {
    }

    fun run(bitmap: Bitmap) {

        bgInfo?.let {

            originBitmapForBg?.let {originBg ->
                val canvas = viewHolder.lockCanvas()

                // 화면에 맞게 resize
//            val dimension = getSquareCropDimensionForBitmap(bitmap)
//            val resizeBitmap = ThumbnailUtils.extractThumbnail(bitmap, dimension, dimension, ThumbnailUtils.OPTIONS_RECYCLE_INPUT)

//                val resizeBitmap = bitmap//Bitmap.createScaledBitmap(bitmap, translatePx(bitmap.width), translatePy(bitmap.height), true) //resize(bitmap, canvas)//
                val resizeBitmap = bitmap//Bitmap.createScaledBitmap(bitmap, translatePx(bitmap.width)+4, translatePy(bitmap.height), false)

                // 새로그려지면 안쌓이게, Clear
                canvas.drawColor(0, PorterDuff.Mode.CLEAR)

                try {
                    Log.i("ImagePreview","top / left ${it.top} ${it.left}")

//                    canvas.drawBitmap(resizeBitmap, translatePx(it.left.toInt()).toFloat(), translatePy(it.top.toInt()).toFloat(), null)
                    canvas.drawBitmap(resizeBitmap, it.left, it.top, null)
//                    canvas.drawBitmap(resizeBitmap, 0.0f, 0.0f, null)
                } finally {
                    viewHolder.unlockCanvasAndPost(canvas)
                }
            }
            // buffer lock

        }

    }

    private fun deltaPx(targetWidth: Int): Int {
        return viewHolder.surfaceFrame.width()- targetWidth
    }

    private fun deltaPy(targetHeight: Int):Int {
        return viewHolder.surfaceFrame.height() - targetHeight
    }

    private fun translatePx(targetPx: Int): Int{
        return targetPx + deltaPx
    }

    private fun translatePy(targetPy: Int): Int{
        return targetPy + deltaPy
    }

    private fun getSquareCropDimensionForBitmap(bitmap: Bitmap): Int { //use the smallest dimension of the image to crop to
        return Math.min(bitmap.width, bitmap.height)
    }
}