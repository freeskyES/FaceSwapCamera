package com.es.faceswapcamera.prototype.manager

import android.graphics.Bitmap
import android.util.Log
import android.util.Pair
import android.util.Size
import com.es.faceswapcamera.common.GraphicOverlay
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.es.faceswapcamera.prototype.facedetection.FaceContourGraphic
import java.util.ArrayList

class BgImageManager(/*private val previewSize: Size,*/
                     private val graphicOverlay: GraphicOverlay
) {

    fun startForBg(bgViewSize: Size, bitmap: Bitmap, faceImage: FaceImage) {

//        originBitmapForBg?.let { if (!it.isRecycled) it.recycle() }
//        bgBitmap?.let { if (!it.isRecycled) it.recycle() }

        val resizeBitmap = resizeImage(bgViewSize, bitmap)
//        originBitmapForBg = resizeBitmap

        getVisionPoints(FirebaseVisionImage.fromBitmap(resizeBitmap), object : CallbackGraphic {
            override fun getPoints(points: ArrayList<FaceContourGraphic.FaceContourData>,
                                   faceInfo: FaceContourGraphic.FaceDetectInfo) {
//                bgFaceInfo = faceInfo
//                val result = getBgBitmap(points)

//                result?.let {
                    faceImage.bringResultInfo(faceInfo, resizeBitmap)
//                }
            }
        })
    }

    interface FaceImage{
        fun bringResultInfo(faceInfo: FaceContourGraphic.FaceDetectInfo, resizeBitmap: Bitmap)
    }

    private fun getVisionPoints(firebaseVisionImage: FirebaseVisionImage, callback: CallbackGraphic) {

        val visionImage = firebaseVisionImage//FirebaseVisionImage.fromBitmap(bitmap)

        runFaceContourDetectionForImage(visionImage, object : Callback {

            override fun getVisionFaces(faces: List<FirebaseVisionFace>) {
                processFaceContourDetectionResultForImage(faces, object : CallbackGraphic {
                    override fun getPoints(points: ArrayList<FaceContourGraphic.FaceContourData>,
                                           faceInfo: FaceContourGraphic.FaceDetectInfo) {
                        callback.getPoints(points, faceInfo)
                    }
                })
            }
        })
    }

    private fun resizeImage(targetSize: Size, selectBitmap: Bitmap): Bitmap {

        val targetedSize: Pair<Int, Int> = Pair(targetSize.width, targetSize.height)
        val targetWidth = targetedSize.first
        val maxHeight = targetedSize.second
        // Determine how much to scale down the image
        val scaleFactor = Math.max(
                targetSize.width.toFloat() / targetWidth.toFloat(),
                targetSize.height.toFloat() / maxHeight.toFloat()
        )
        val resizedBitmap = Bitmap.createScaledBitmap(
                selectBitmap,
                (targetSize.width / scaleFactor).toInt(),
                (targetSize.height / scaleFactor).toInt(),
                true
        )
        return resizedBitmap
    }

    private fun runFaceContourDetectionForImage(firebaseVisionImage: FirebaseVisionImage, callback: Callback) {
        try {
            val image = firebaseVisionImage
            val options = FirebaseVisionFaceDetectorOptions.Builder()
                    .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                    .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                    .build()
            val detector =
                    FirebaseVision.getInstance().getVisionFaceDetector(options)
            detector.detectInImage(image)
                    .addOnSuccessListener { faces ->
                        callback.getVisionFaces(faces)
//                    processFaceContourDetectionResultForImage(faces, firebaseVisionImage)
                    }
                    .addOnFailureListener { e ->
                        // Task failed with an exception
                        e.printStackTrace()
                    }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    interface Callback {
        fun getVisionFaces(faces: List<FirebaseVisionFace>)
    }

    interface CallbackGraphic {
        fun getPoints(points: ArrayList<FaceContourGraphic.FaceContourData>,
                      faceInfo: FaceContourGraphic.FaceDetectInfo)
    }

    private fun processFaceContourDetectionResultForImage(faces: List<FirebaseVisionFace>, callback: CallbackGraphic) {
        if (faces.isEmpty()) {
            Log.i("detect","face empty!!")
            graphicOverlay.clear()
            return
        }
        graphicOverlay.clear()
        for (i in faces.indices) {
            val face = faces[i]
            Log.i("detect","!!")

            val faceGraphic = FaceContourGraphic(graphicOverlay, face) { points, faceInfo ->
//                originBitmapForBg = firebaseVisionImage.bitmap
//                getBgBitmap(it)
                Log.i("detect","point"+ faceInfo)
                callback.getPoints(points, faceInfo)
            }

            graphicOverlay.add(faceGraphic)
//            faceGraphic.updateFace(face)
        }
    }
}