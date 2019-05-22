package com.crislmfroes.ariacamera

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
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

    fun onClickText() {
        val config = CameraConfiguration(
            frameProcessor = {
                readText(it)
            }
        )
        apparat!!.updateConfiguration(config)
    }
}
