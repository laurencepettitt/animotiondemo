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

    private var _viroView: ViroViewScene? = null

    private val bind: AnimotionViewBinding = DataBindingUtil.inflate(
        LayoutInflater.from(context),
        R.layout.animotion_view, this, true
    )

    private var moodValue: Float by Delegates.observable(1.0F) { _, _, newValue ->
        _modelObject3D?.setMorphTargetWeight("0", newValue)
    }

    private var _modelObject3D: Object3D? = null

    val spotlight by lazy {
        Spotlight().apply {
            position = Vector(-3.5, 5.0, 3.0)
            direction = Vector(0.0, 0.0, -1.0)
            attenuationStartDistance = 5f
            attenuationEndDistance = 10f
            innerAngle = 5f
            outerAngle = 20f
            color = Color.GRAY.toLong()
            intensity = 700f
        }
    }

    private var _modelUri: Uri = Uri.EMPTY

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.AnimotionView,
            0, 0
        ).apply {
            try {
                _modelUri = Uri.parse(getString(R.styleable.AnimotionView_modelSrc))
            } finally {
                recycle()
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        onStartupViroViewScene()

        _viroView?.let { viroView ->
            viroView.id = generateViewId()
            bind.animotionContainer.addView(_viroView)

            ConstraintSet().apply {
                clone(bind.animotionContainer)
                connect(
                    viroView.id,
                    ConstraintSet.START,
                    bind.animotionContainer.id,
                    ConstraintSet.START
                )
                connect(
                    viroView.id,
                    ConstraintSet.END,
                    bind.animotionContainer.id,
                    ConstraintSet.END
                )
                connect(
                    viroView.id,
                    ConstraintSet.BOTTOM,
                    bind.animotionSlider.id,
                    ConstraintSet.TOP
                )
                applyTo(bind.animotionContainer)
            }
        }

        bind.animotionSlider.addOnChangeListener { _, value, _ ->
            moodValue = value
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        _viroView?.dispose()
        _viroView = null
    }

    private fun onStartupViroViewScene() {
        _viroView = ViroViewScene(this.context, object : ViroViewScene.StartupListener {
            override fun onSuccess() {
                _viroView?.scene = getScene()
            }

            override fun onFailure(error: ViroViewScene.StartupError, errorMessage: String) {
            }
        })
    }

    private fun getScene(): Scene {
        // Create a new Scene and get its root Node
        val scene = Scene()

        val avatar = Object3D().apply {
            loadModel(
                _viroView?.viroContext,
                _modelUri,
//                Uri.parse("file:///android_asset/boy-full.glb"),
                Object3D.Type.GLB,
                object : AsyncObject3DListener {

                    override fun onObject3DFailed(p0: String?) {
                        Timber.w("Failed to load the model");

                    }

                    override fun onObject3DLoaded(p0: Object3D?, p1: Object3D.Type?) {
                        Timber.i("Successfully loaded the model!");
                        p0?.let {
                            it.setMorphTargetWeight("0", 1.0F)
                            _modelObject3D = p0
                        }
                    }
                })
            setPosition(Vector(0.0, 0.0, 0.0))
//            setRotation(Vector(0.0, -1.0, 0.0))
        }


        val ambientLight = AmbientLight(Color.WHITE.toLong(), 200F)

        val cameraNode = Node().apply {
            setPosition(Vector(0.0, 1.4, 2.5))
            setRotation(Vector(0.0, 0.0, 0.0))
            camera = Camera()
        }

        _viroView?.setPointOfView(cameraNode)

        scene.rootNode.apply {
            addChildNode(avatar)
            addChildNode(cameraNode)
            addLight(ambientLight)
            addLight(spotlight)
        }

        val colorPrimaryValue = TypedValue()
        context.theme.resolveAttribute(R.attr.colorSurface, colorPrimaryValue, true)
        scene.setBackgroundCubeWithColor(colorPrimaryValue.data.toLong())

        // Display the scene
        return scene
    }
}
