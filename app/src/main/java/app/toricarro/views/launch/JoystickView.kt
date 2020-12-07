package app.toricarro.views.launch

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import app.toricarro.views.AppUtils
import kotlin.math.min

class JoystickView : SurfaceView, SurfaceHolder.Callback {
    private var cx: Float = 0f
    private var cy: Float = 0f
    private var baseRadius: Float = 0f
    private var hatRadius: Float = 0f

    constructor(context: Context) : super(context) {
        holder.addCallback(this)
    }

    constructor(
        context: Context,
        attributes: AttributeSet,
        style: Int
    ) : super(context, attributes, style) {
        holder.addCallback(this)
    }

    constructor(
        context: Context,
        attributes: AttributeSet,
    ) : super(context, attributes) {
        holder.addCallback(this)
    }

    private fun setupDimensions() {
        cx = width / 2f
        cy = height / 2f
        baseRadius = min(width, height) / 3f
        hatRadius = min(width, height) / 5f
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
        setupDimensions()
        drawJoystick(cx, cy)

    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }


}