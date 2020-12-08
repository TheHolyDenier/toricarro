package app.toricarro.views.launch

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

class JoystickView : SurfaceView, SurfaceHolder.Callback {
    private var cx: Float = 0f
    private var cy: Float = 0f
    private var baseRadius: Float = 0f
    private var hatRadius: Float = 0f
    private lateinit var joystickCallback: JoystickListener

    constructor(context: Context) : super(context) {
        startJoystick(context)
    }

    constructor(
        context: Context,
        attributes: AttributeSet,
        style: Int
    ) : super(context, attributes, style) {
        startJoystick(context)
    }

    constructor(
        context: Context,
        attributes: AttributeSet,
    ) : super(context, attributes) {
        startJoystick(context)
    }

    private fun startJoystick(context: Context) {
        if (context is JoystickListener) joystickCallback = context
        holder.addCallback(this)
        setOnTouchListener(this::onTouch)
    }

    private fun setupDimensions() {
        cx = width / 2f
        cy = height / 2f
        baseRadius = min(width, height) / 3f
        hatRadius = min(width, height) / 7f
    }

    private fun drawJoystick(x: Float, y: Float) {
        if (holder.surface.isValid) {
            val canvas: Canvas = this.holder.lockCanvas()
            val colors = Paint()
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            colors.setARGB(255, 50, 50, 50)
            canvas.drawCircle(cx, cy, baseRadius, colors)
            colors.setARGB(255, 0, 0, 255)
            canvas.drawCircle(x, y, hatRadius, colors)
            holder.unlockCanvasAndPost(canvas)
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        holder.setFormat(PixelFormat.TRANSPARENT)
        setupDimensions()
        drawJoystick(cx, cy)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }


    private fun onTouch(v: View, e: MotionEvent): Boolean {
        if (v == this) {
            if (e.action != MotionEvent.ACTION_UP) calculateJoystickMovement(e)
            else {
                drawJoystick(cx, cy)
                joystickCallback.onJoystickMoved(0f, 0f, id)
            }
        }
        return true
    }

    private fun calculateJoystickMovement(e: MotionEvent) {
        val displacement = sqrt(((e.x - cx).pow(2)) + ((e.y - cy).pow(2)))
        if (displacement < baseRadius) {
            drawJoystick(e.x, e.y)
            joystickCallback.onJoystickMoved((e.x-cx)/baseRadius, (e.y-cy)/baseRadius, id)
        }
        else {
            val ratio = baseRadius / displacement
            val constrainedX = cx + (e.x - cx) * ratio
            val constrainedY = cy + (e.y - cy) * ratio
            drawJoystick(constrainedX, constrainedY)
            joystickCallback.onJoystickMoved((constrainedX-cx)/baseRadius, (constrainedY-cy)/baseRadius, id)
        }
    }

    interface JoystickListener {
        fun onJoystickMoved(xPercent: Float, yPercent: Float, id: Int)
    }
}