package com.qusion.vos.animotion

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.DataBindingUtil
import com.qusion.vos.animotion.databinding.AnimotionViewBinding
import com.viro.core.*
import timber.log.Timber
import kotlin.properties.Delegates

class AnimotionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    ConstraintLayout(context, attrs, defStyleAttr) {

    private lateinit var _viroView: ViroViewScene

    private var moodValue: Float by Delegates.observable(1.0F) { property, oldValue, newValue ->
        object3D?.setMorphTargetWeight("0", newValue)
    }

    private var object3D: Object3D? = null

    private val bind: AnimotionViewBinding = DataBindingUtil.inflate(
        LayoutInflater.from(context),
        R.layout.animotion_view, this, true
    )

    // TODO: Resource management
    private val faceDetector = FaceDetector()

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        onStartupViroViewScene()

        _viroView.id = generateViewId()
        bind.animotionContainer.addView(_viroView)

        ConstraintSet().also {
            it.clone(bind.animotionContainer)
            it.connect(
                _viroView.id,
                ConstraintSet.START,
                bind.animotionContainer.id,
                ConstraintSet.START
            )
            it.connect(
                _viroView.id,
                ConstraintSet.END,
                bind.animotionContainer.id,
                ConstraintSet.END
            )
            it.connect(
                _viroView.id,
                ConstraintSet.BOTTOM,
                bind.animotionSlider.id,
                ConstraintSet.TOP
            )
            it.applyTo(bind.animotionContainer)
        }

        bind.animotionSlider.addOnChangeListener { slider, value, fromUser ->
            moodValue = value
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        _viroView.dispose()
    }

    private fun onStartupViroViewScene() {
        _viroView = ViroViewScene(this.context, object : ViroViewScene.StartupListener {
            override fun onSuccess() {
                onCreateViewScene()
            }

            override fun onFailure(error: ViroViewScene.StartupError, errorMessage: String) {
            }
        })
    }

    private fun onCreateViewScene() {
        // Create a new Scene and get its root Node
        val scene = Scene()

        val avatar = Object3D().also {
            it.loadModel(
                _viroView.viroContext,
                Uri.parse("file:///android_asset/boy-full.glb"),
                Object3D.Type.GLB,
                object : AsyncObject3DListener {

                    override fun onObject3DFailed(p0: String?) {
                        Timber.w("Failed to load the model");

                    }

                    override fun onObject3DLoaded(p0: Object3D?, p1: Object3D.Type?) {
                        Timber.i("Successfully loaded the model!");
                        p0?.let {
                            it.setMorphTargetWeight("0", 1.0F)
                            object3D = p0
                        }
                    }
                })
            it.setPosition(Vector(0.0, 0.0, 0.0))
//            it.setRotation(Vector(0.0, -1.0, 0.0))
        }


        val spotlight = Spotlight().also {
            it.position = Vector(-3.5, 5.0, 3.0)
            it.direction = Vector(0.0, 0.0, -1.0)
            it.attenuationStartDistance = 5f
            it.attenuationEndDistance = 10f
            it.innerAngle = 5f
            it.outerAngle = 20f
            it.color = Color.GRAY.toLong()
            it.intensity = 700f
        }

        val ambientLight = AmbientLight(Color.WHITE.toLong(), 200F)

        val cameraNode = Node().also {
            it.setPosition(Vector(0.0, 1.4, 2.5))
            it.setRotation(Vector(0.0, 0.0, 0.0))
            it.camera = Camera()
        }

        _viroView.setPointOfView(cameraNode)

        scene.rootNode.also {
            it.addChildNode(avatar)
            it.addChildNode(cameraNode)
            it.addLight(ambientLight)
            it.addLight(spotlight)
        }

        val colorPrimaryValue = TypedValue();
        context.theme.resolveAttribute(R.attr.colorPrimarySurface, colorPrimaryValue, true)
        scene.setBackgroundCubeWithColor(colorPrimaryValue.data.toLong())

        // Display the scene
        _viroView.setScene(scene)
    }

}
