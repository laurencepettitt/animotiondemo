package com.qusion.vos.animotion

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import com.viro.core.*
import timber.log.Timber

class AnimotionView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private lateinit var _ViroView: ViroViewScene

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        onStartupViroViewScene()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        _ViroView.dispose()
    }

    private fun onStartupViroViewScene() {
        _ViroView = ViroViewScene(this.context, object : ViroViewScene.StartupListener {
            override fun onSuccess() {
                // Start building your scene here! We provide a sample "Hello World" scene
                onCreateViewScene()
            }
            override fun onFailure(error: ViroViewScene.StartupError, errorMessage: String) {
                // Fail as you wish!
            }
        })
    }

    private fun onCreateViewScene() {
        // Create a new Scene and get its root Node
        val scene = Scene()

        val avatar = Object3D().also {
            it.loadModel(
                _ViroView.viroContext,
                Uri.parse("file:///android_asset/boy-full.glb"),
                Object3D.Type.GLB,
                object : AsyncObject3DListener {

                    override fun onObject3DFailed(p0: String?) {
                        Timber.w("Failed to load the model");
                    }

                    override fun onObject3DLoaded(p0: Object3D?, p1: Object3D.Type?) {
                        Timber.i("Successfully loaded the model!");
                        p0?.morphTargetKeys?.let {
                            Timber.d("SHEN: morph target keys empty?: ${it.isEmpty()}")
                            Timber.d("SHEN: $it")
                        }
                        p0?.setMorphTargetWeight("0", 0.5F)
                    }
                })
            it.setPosition(Vector(0.0, 0.0, -3.0))
//            it.setRotation(Vector(0.0, -1.0, 0.0))
        }



        val spotlight = Spotlight().also {
            it.position = Vector(-1.0, 1.0, 1.0)
            it.direction = Vector(1.0, 0.0, 0.0)
            it.attenuationStartDistance = 5f
            it.attenuationEndDistance = 10f
            it.innerAngle = 5f
            it.outerAngle = 20f
            it.color = Color.GRAY.toLong()
            it.intensity = 800f
        }

        val ambientLight = AmbientLight(Color.WHITE.toLong(), 100F)


        val cameraNode = Node().also {
            it.setPosition(Vector(0.0, 0.5, 0.5))
            it.setRotation(Vector(0.0, 0.0, 0.0))
            it.camera = Camera()
        }

        _ViroView.setPointOfView(cameraNode)

        scene.rootNode.also {
            it.addChildNode(avatar)
            it.addChildNode(cameraNode)
            it.addLight(ambientLight)
            it.addLight(spotlight)
        }

        // Display the scene
        _ViroView.setScene(scene)
    }

}
