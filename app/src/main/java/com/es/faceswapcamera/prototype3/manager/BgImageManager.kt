package com.es.faceswapcamera.prototype3.manager

import android.graphics.Bitmap
import android.util.Log
import android.util.Pair
import android.util.Size
import com.es.faceswapcamera.common.GraphicOverlay
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.es.faceswapcamera.prototype3.facedetection.FaceContourGraphic
import java.util.ArrayList

class BgImageManager(/*private val previewSize: Size,*/
                     private val graphicOverlay: GraphicOverlay
) {

    /**
     * 1. object 인식 후 point 얻어오고 자르기
     * 2. 얼굴인식 -> 얼굴 정보 얻어오기 (+ 추가 턱끝 정보 알아보기)
     * 3. 가공 (턱 밑 얼굴 자르기)
     * 4. 내보내기
     */
    fun startForBg(bgViewSize: Size, bitmap: Bitmap, faceImage: FaceImage) {

        val resizeBitmap = resizeImage(bgViewSize, bitmap)
//        originBitmapForBg = resizeBitmap

        getVisionPoints(FirebaseVisionImage.fromBitmap(resizeBitmap), object :
            CallbackGraphic {
            override fun getPoints(points: ArrayList<FaceContourGraphic.FaceContourData>,
                                   faceInfo: FaceContourGraphic.FaceDetectInfo) {
//                bgFaceInfo = faceInfo
//                val result = getBgBitmap(points)


                // 얼굴정보 얻기 성공
                // 얼굴 밑으로 자르기
                val croppedImage = splitImage(resizeBitmap, faceInfo)
                // 내보내기
                faceImage.bringResultInfo(faceInfo, croppedImage)
            }
        })
    }

    private fun splitImage(targetBitmap: Bitmap, faceInfo: FaceContourGraphic.FaceDetectInfo): Bitmap {

        return Bitmap.createBitmap(targetBitmap, 0, faceInfo.chinBottomPos.py.toInt(), targetBitmap.width, targetBitmap.height - faceInfo.chinBottomPos.py.toInt())
    }

    interface FaceImage{
        fun bringResultInfo(faceInfo: FaceContourGraphic.FaceDetectInfo, resizeBitmap: Bitmap)
    }

    private fun getVisionPoints(firebaseVisionImage: FirebaseVisionImage, callback: CallbackGraphic) {

        val visionImage = firebaseVisionImage //FirebaseVisionImage.fromBitmap(bitmap)

        runFaceContourDetectionForImage(visionImage, object :
            Callback {

            override fun getVisionFaces(faces: List<FirebaseVisionFace>) {
                processFaceContourDetectionResultForImage(faces, object :
                    CallbackGraphic {
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