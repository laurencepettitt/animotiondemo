package com.qusion.vos.animotion

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
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

    private var _moodValue: Float by Delegates.observable(1.0F) { _, _, newValue ->
        _modelObject3D?.setMorphTargetWeight("0", newValue)
    }

    private val MOODS = listOf(
        0.026149364,
        0.2905569,
        0.065936305,
        0.1989207,
        0.9895666,
        0.9895666,
        0.9914598,
        0.9914598,
        0.9910811,
        0.21929683,
        0.26047203,
        0.7746653,
        0.8453749,
        0.35873604,
        0.9910811,
        0.011907466,
        0.029783526,
        0.06494977,
        0.38200837,
        0.38200837,
        0.13751231,
        0.10806591,
        0.9960033,
        0.9956247,
        0.9693333,
        0.9601184,
        0.98880935
    )

    private var _modelObject3D: Object3D? = null
    private var _modelUri: Uri = Uri.EMPTY

    private val spotLight by lazy {
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

    private val ambientLight by lazy {
        AmbientLight(Color.WHITE.toLong(), 200F)
    }


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

//        GlobalScope.launch {
//            val queue = CustomQueue { value: Float -> _moodValue = value }
//            var i = 0
//            val sz = MOODS.size
//            while (true) {
//                queue.insert(MOODS[i.rem(sz)].toFloat())
//                delay(200)
//                i++
//            }
//        }

        bind.animotionSlider.addOnChangeListener { _, value, _ ->
            _moodValue = value
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        _viroView?.dispose()
        _viroView = null
    }

    private var acc: Float = 0.0f

    fun setMoodPercentage(smilePercent: Float) {
        Timber.d("smilePercent: $smilePercent")
        acc = acc * 0.75f + smilePercent * 0.25f
        bind.animotionSlider.value = acc
        Timber.d("acc: $acc")
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

        val avatar = Object3D().apply {
            loadModel(
                _viroView?.viroContext,
                _modelUri,
                Object3D.Type.GLB,
                object : AsyncObject3DListener {
                    override fun onObject3DFailed(reason: String?) {
                        Timber.w("Failed to load the model")
                    }

                    override fun onObject3DLoaded(model: Object3D?, type: Object3D.Type?) {
                        Timber.i("Successfully loaded the model!")
                        _modelObject3D = model
                    }
                })
            setPosition(Vector(0.0, 0.0, 0.0))
        }

        val cameraNode = Node().apply {
            setPosition(Vector(0.0, 1.4, 2.5))
            setRotation(Vector(0.0, 0.0, 0.0))
            camera = Camera()
        }

        _viroView?.setPointOfView(cameraNode)

        return Scene().apply {
            rootNode.apply {
                addChildNode(avatar)
                addChildNode(cameraNode)
                addLight(ambientLight)
                addLight(spotLight)
                setBackgroundCubeWithColor(context.getColorFromAttr(R.attr.colorPrimary).toLong())
            }
        }
    }
}

//class CustomQueue(val listener: (Float) -> Unit) {
//
//    init {
//
//    }
//
//    private var time: Long? = null
//
//    private var acc: Float = 0.0f
//
//    fun insert(value: Float) {
////        val newTime = System.currentTimeMillis()
////        time?.let {
////            time = newTime
////        }
////        val timeDelta = newTime - (time ?: 0)
//        acc = acc * 0.75f + value * 0.25f
//
//        listener(value)
//    }
//
//}

@ColorInt
private fun Context.getColorFromAttr(
    @AttrRes attrColor: Int,
    typedValue: TypedValue = TypedValue(),
    resolveRefs: Boolean = true
): Int {
    theme.resolveAttribute(attrColor, typedValue, resolveRefs)
    return typedValue.data
}

