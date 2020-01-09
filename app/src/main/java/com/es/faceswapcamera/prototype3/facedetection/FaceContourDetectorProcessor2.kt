package com.es.faceswapcamera.prototype3.facedetection

import android.graphics.Bitmap
import android.util.Log
import android.util.Size
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import com.es.faceswapcamera.R
import com.es.faceswapcamera.common.CameraImageGraphic
import com.es.faceswapcamera.common.FrameMetadata
import com.es.faceswapcamera.common.GraphicOverlay
import com.es.faceswapcamera.common.VisionProcessorBase
import com.es.faceswapcamera.prototype3.custom.ImagePreview
import com.es.faceswapcamera.prototype3.manager.BgImageManager
import com.es.faceswapcamera.prototype3.util.bgBitmapUtils
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import kotlinx.coroutines.*
import java.io.IOException
import kotlin.coroutines.CoroutineContext


class FaceContourDetectorProcessor2(val bgImageView: ImageView,
                                    val overlay: GraphicOverlay,
                                    val bgOverlay: GraphicOverlay,
                                    val bgImagePreview: ImagePreview
) : VisionProcessorBase<List<FirebaseVisionFace>>(), CoroutineScope {

    private val detector: FirebaseVisionFaceDetector

    var job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private lateinit var originBitmapForBg : Bitmap
    private var bgFaceInfo: FaceContourGraphic.FaceDetectInfo? = null

    private lateinit var bgImageManager: BgImageManager

//    private var image: FirebaseVisionImage? = null


    init {
        val options = FirebaseVisionFaceDetectorOptions.Builder()
                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                .build()

        detector = FirebaseVision.getInstance().getVisionFaceDetector(options)

        bgImageManager =
            BgImageManager(bgOverlay)
        // bg face info
        overlay.context.resources.getDrawable(R.drawable.test_model_4).run { // TODO 바꾸기
            originBitmapForBg = this.toBitmap()
        }
    }

    override fun stop() {
        try {
            detector.close()
        } catch (e: IOException) {
            Log.e(TAG, "Exception thrown while trying to close Face Contour Detector: $e")
        }
    }

    override fun detectInImage(image: FirebaseVisionImage): Task<List<FirebaseVisionFace>> {
//        this.image = image
        return detector.detectInImage(image)
    }

    override fun onSuccess(
        originalCameraImage: Bitmap?,
        results: List<FirebaseVisionFace>,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay
    ) {
        graphicOverlay.clear()

        originalCameraImage?.let {
            val imageGraphic =
                CameraImageGraphic(
                    graphicOverlay,
                    it
                )
            graphicOverlay.add(imageGraphic)
        }

        if (bgFaceInfo == null) {
            CoroutineScope(Dispatchers.IO+ job).launch{
                bgImageManager.startForBg(Size(bgImageView.width, bgImageView.height), originBitmapForBg, object : BgImageManager.FaceImage {
                    override fun bringResultInfo(faceInfo: FaceContourGraphic.FaceDetectInfo, resizeBitmap: Bitmap) {
                        bgFaceInfo = faceInfo
                        originBitmapForBg = resizeBitmap
                        bgImagePreview.updatePreviewInfo(faceInfo, Size(overlay.width, overlay.height) /*resizeBitmap*/)
                    }
                })
            }
        }

        results.forEach {
            val faceGraphic = FaceContourGraphic(
                    graphicOverlay,
                    it,
                    object : ContourListener {
                        override fun invoke(points: ArrayList<FaceContourGraphic.FaceContourData>, faceInfo: FaceContourGraphic.FaceDetectInfo) {
                            originalCameraImage?.let { originFace -> face(originFace, points, faceInfo) }
                        }
                    }
            )
            graphicOverlay.add(faceGraphic)
        }

        graphicOverlay.postInvalidate()

    }

    /**
     * face 정보 얻음
     * bg face 정보에 맞게 resize
     */
    private fun face(originalCameraImage: Bitmap, points: ArrayList<FaceContourGraphic.FaceContourData>, faceInfo: FaceContourGraphic.FaceDetectInfo) {
        CoroutineScope(coroutineContext).launch{

            withContext(Dispatchers.IO) {

                bgFaceInfo?.let {bgFaceInfo ->

                    bgBitmapUtils.resizeFace(originBitmapForBg, originalCameraImage, bgFaceInfo, faceInfo)?.let { resizeBitmap ->
                        bgImagePreview.run(resizeBitmap, faceInfo)
                    }
                }

            }
            // main
        }
    }


    override fun onFailure(e: Exception) {
        Log.e(TAG, "Face detection failed $e")
    }

    companion object {
        private const val TAG = "FaceContourDetectorProc"

    }


}