package com.es.faceswapcamera.prototype3.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.RelativeLayout
import com.es.faceswapcamera.prototype3.facedetection.FaceContourGraphic


class ImagePreview(context: Context?, attrs: AttributeSet?) : SurfaceView(context, attrs), SurfaceHolder.Callback{

    var viewHolder : SurfaceHolder = holder

    var bgInfo : FaceContourGraphic.FaceDetectInfo? = null

//    var originBitmapForBg : Bitmap? = null

    var deltaPx: Int = 0

    var deltaPy: Int = 0


    init {

        viewHolder.addCallback(this)
        setZOrderOnTop(true)

        // 겹쳐서 배경이 보일수 있도록 투명
        viewHolder.setFormat(PixelFormat.TRANSPARENT)

    }

    fun updatePreviewInfo(bgInfo : FaceContourGraphic.FaceDetectInfo, previewSize: Size/*, originBitmapForBg : Bitmap*/) {
        this.bgInfo = bgInfo
//        this.originBitmapForBg = originBitmapForBg

        this.layoutParams = RelativeLayout.LayoutParams(previewSize.width, previewSize.height).apply {
//            addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
//            addRule(RelativeLayout.ALIGN_PARENT_END)
        }

        viewHolder.setFixedSize(previewSize.width, previewSize.height)

        deltaPx = deltaPx(previewSize.width)
        deltaPy = deltaPy(previewSize.height)
        postInvalidate()
    }

    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
    }

    override fun surfaceDestroyed(p0: SurfaceHolder?) {
    }

    override fun surfaceCreated(p0: SurfaceHolder?) {
    }

    fun run(bgBitmap: Bitmap, faceInfo: FaceContourGraphic.FaceDetectInfo) {

        bgInfo?.let { bgface ->

            val canvas = viewHolder.lockCanvas()

            // 새로그려지면 안쌓이게, Clear
            canvas.drawColor(0, PorterDuff.Mode.CLEAR)

            val px = faceInfo.centerPx - bgBitmap.width / 2
            val py = faceInfo.chinBottomPos.py

            val ratio = faceInfo.rectWidth / bgface.rectWidth

            val realPx = px + ( bgBitmap.width / 2 -  bgface.centerPx * ratio)


            try {
                val paint = blur(canvas)
                Log.i("ImagePreview","px / py $realPx $py")

                canvas.drawBitmap(bgBitmap, realPx, py, paint)
//                    canvas.drawBitmap(resizeBitmap, 0.0f, 0.0f, null)
            } finally {
                viewHolder.unlockCanvasAndPost(canvas)
            }
        }
    }

    private fun blur(canvas: Canvas): Paint {
        val paint = Paint()
        paint.isAntiAlias = true

        val blur = BlurMaskFilter(20.0f, BlurMaskFilter.Blur.NORMAL)
        canvas.drawColor(Color.TRANSPARENT)
        paint.maskFilter = blur

        return paint
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