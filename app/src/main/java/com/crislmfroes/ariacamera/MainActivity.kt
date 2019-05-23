package com.crislmfroes.ariacamera

import android.content.Context
import android.hardware.camera2.CameraManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.SparseIntArray
import android.view.Surface
import android.view.View
import android.widget.TextView
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import io.fotoapparat.Fotoapparat
import io.fotoapparat.configuration.CameraConfiguration
import io.fotoapparat.preview.Frame
import io.fotoapparat.view.CameraView

class MainActivity : AppCompatActivity() {

    private var apparat : Fotoapparat? = null

    private var textView : TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val cameraView = findViewById<CameraView>(R.id.camera_view)
        apparat = Fotoapparat(
            this.applicationContext,
            cameraView
        )
        textView = findViewById(R.id.textDescription)
    }

    override fun onStart() {
        apparat!!.start()
        super.onStart()
    }

    override fun onStop() {
        apparat!!.stop()
        super.onStop()
    }

    private fun readText(frame : Frame) {
        val metadata = FirebaseVisionImageMetadata.Builder()
            .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
            .setRotation(FirebaseVisionImageMetadata.ROTATION_0)
            .setWidth(frame.size.width)
            .setHeight(frame.size.height)
            .build()
        val fireImage = FirebaseVisionImage.fromByteArray(frame.image, metadata)
        val detector = FirebaseVision.getInstance().onDeviceTextRecognizer
        val result = detector.processImage(fireImage)
            .addOnSuccessListener {
                val resultText = it.text
                Log.d("Detected text", resultText)
                textView!!.text = resultText
            }
            .addOnFailureListener {
                Log.e("Error", "Error on readText", it)
            }
    }

    private fun detectFaces(frame : Frame) {
        val metadata = FirebaseVisionImageMetadata.Builder()
            .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
            .setRotation(FirebaseVisionImageMetadata.ROTATION_0)
            .setWidth(frame.size.width)
            .setHeight(frame.size.height)
            .build()
        val fireImage = FirebaseVisionImage.fromByteArray(frame.image, metadata)
        val detectorOptions = FirebaseVisionFaceDetectorOptions.Builder()
            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
            .build()
        val detector = FirebaseVision.getInstance().getVisionFaceDetector(detectorOptions)
        val result = detector.detectInImage(fireImage)
            .addOnSuccessListener {
                val faces = it
                val faceCount = faces.size
                var smileCount = 0
                for (face in faces) {
                    if (face.smilingProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                        if (face.smilingProbability >= 0.5) {
                            smileCount += 1
                        }
                    }
                }
                textView!!.text = "%i rostos na camera, %i estão sorrindo.".format(faceCount, smileCount)
            }
            .addOnFailureListener {
                Log.e("Error", "Error on detectFaces", it)
            }
    }

    fun onClickText(v : View) {
        val config = CameraConfiguration(
            frameProcessor = {
                readText(it)
            }
        )
        apparat!!.updateConfiguration(config)
    }

    fun onClickFaces(v : View) {
        val config = CameraConfiguration(
            frameProcessor = {
                detectFaces(it)
            }
        )
        apparat!!.updateConfiguration(config)
    }
}
