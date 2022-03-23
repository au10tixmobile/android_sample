package com.au10tix.sampleapp.views.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.shapes.PathShape
import android.graphics.drawable.shapes.Shape
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.camera.core.CameraSelector
import com.au10tix.sdk.protocol.Quad
import com.au10tix.selfie.detector.face.Au10Face

class OverlayView : FrameLayout {
    private var faces: List<Au10Face>? = null
    private var shape: Shape? = null
    private var quad: Quad? = null
    private var paint: Paint? = null
    private var mPreviewWidth = 0
    private var mWidthScaleFactor = 1.0f
    private var mPreviewHeight = 0
    private var mHeightScaleFactor = 1.0f
    private var facing = CameraSelector.LENS_FACING_BACK

    constructor(context: Context?) : super(context!!) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr) {
        init()
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int,
    ) : super(
        context!!, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    private fun init() {
        paint = Paint()
        paint!!.color = Color.BLUE
        paint!!.style = Paint.Style.STROKE
        paint!!.strokeWidth = 5f
    }

    fun setFaces(faces: List<Au10Face>?) {
        this.faces = faces
        invalidate()
    }

    fun setShape(shape: Shape?) {
        this.shape = shape
        invalidate()
    }

    fun setQuad(quad: Quad?) {
        this.quad = quad
        invalidate()
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (mPreviewWidth != 0 && mPreviewHeight != 0) {
            mWidthScaleFactor = width.toFloat() / mPreviewWidth.toFloat()
            mHeightScaleFactor = height.toFloat() / mPreviewHeight.toFloat()
        }
        if (faces != null) {
            try {
                for (face in faces!!) {
                    val x = translateX(face.position.x + face.width / 2)
                    val y = translateY(face.position.y + face.height / 2)

                    // Draws a bounding box around the face.
                    val xOffset = scaleX(face.width / 2.0f)
                    val yOffset = scaleY(face.height / 2.0f)
                    val left = x - xOffset
                    val top = y - yOffset
                    val right = x + xOffset
                    val bottom = y + yOffset
                    canvas.drawRect(left, top, right, bottom, paint!!)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        } else if (shape != null) {
            shape!!.resize(width.toFloat(), height.toFloat())
            shape!!.draw(canvas, paint)
        } else if (quad != null) {

            //actual frame
            val path = Path()
            if (quad!!.topLeft != null) {
                path.moveTo(scaleX(quad!!.topLeft.x), scaleY(quad!!.topLeft.y))
            } else {
                path.moveTo(0f, 0f)
            }
            if (quad!!.topRight != null) {
                path.lineTo(scaleX(quad!!.topRight.x), scaleY(quad!!.topRight.y))
            } else {
                path.lineTo(width.toFloat(), 0f)
            }
            if (quad!!.bottomRight != null) {
                path.lineTo(scaleX(quad!!.bottomRight.x), scaleY(quad!!.bottomRight.y))
            } else {
                path.lineTo(width.toFloat(), height.toFloat())
            }
            if (quad!!.bottomLeft != null) {
                path.lineTo(scaleX(quad!!.bottomLeft.x), scaleY(quad!!.bottomLeft.y))
            } else {
                path.lineTo(0f, height.toFloat())
            }
            path.close()
            val shape = PathShape(path, width.toFloat(), height.toFloat())
            shape.resize(width.toFloat(), height.toFloat())
            shape.draw(canvas, paint)
        }
    }

    fun setCameraInfo(previewWidth: Int, previewHeight: Int) {
        mPreviewWidth = previewWidth
        mPreviewHeight = previewHeight
    }

    fun setFacing(facing: Int) {
        this.facing = facing
    }

    /**
     * Adjusts a horizontal value of the supplied value from the preview scale to the view
     * scale.
     */
    fun scaleX(horizontal: Float): Float {
        return horizontal * mWidthScaleFactor
    }

    /**
     * Adjusts a vertical value of the supplied value from the preview scale to the view scale.
     */
    fun scaleY(vertical: Float): Float {
        return vertical * mHeightScaleFactor
    }

    /**
     * Adjusts the x coordinate from the preview's coordinate system to the view coordinate
     * system.
     */
    fun translateX(x: Float): Float {
        return if (facing == CameraSelector.LENS_FACING_BACK) {
            scaleX(x)
        } else {
            width - scaleX(x)
        }
    }

    /**
     * Adjusts the y coordinate from the preview's coordinate system to the view coordinate
     * system.
     */
    fun translateY(y: Float): Float {
        return scaleY(y)
    }
}