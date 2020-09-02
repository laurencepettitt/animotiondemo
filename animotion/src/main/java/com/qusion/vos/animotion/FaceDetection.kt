package com.qusion.vos.animotion

import android.net.Uri
import com.google.android.gms.tasks.OnSuccessListener
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.IOException

class FaceDetector() {
    val opts: FaceDetectorOptions = FaceDetectorOptions.Builder()
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .build()

    // TODO: resource management!
    val detector = FaceDetection.getClient(opts)

    fun process(image: InputImage, successListener: (Float) -> Unit) {
        detector.process(image)
            .addOnSuccessListener { faces ->
                // TODO: get most central face
                val smilingProb = faces.firstOrNull()?.smilingProbability
                smilingProb?.let { successListener(it) }
            }
    }

    fun processFromUri(imageUri: Uri) {
}
    }
}