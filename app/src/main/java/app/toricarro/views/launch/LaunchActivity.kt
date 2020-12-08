package app.toricarro.views.launch

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import app.toricarro.R
import app.toricarro.databinding.ActivityLaunchBinding
import app.toricarro.views.AppUtils
import app.toricarro.views.MainActivity
import com.ahmedabdelmeged.bluetoothmc.BluetoothMC
import com.ahmedabdelmeged.bluetoothmc.BluetoothMC.BluetoothConnectionListener
import com.ahmedabdelmeged.bluetoothmc.util.BluetoothStates
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking


class LaunchActivity : AppCompatActivity(), JoystickView.JoystickListener {
    private lateinit var b: ActivityLaunchBinding
    private lateinit var bluetooth: BluetoothMC

    private var controlId: Int = 0
    private var pointerActive: Boolean = false
    private var speedActive: Boolean = false

    private var retries = 0
    private var x: Float = 0f
    private var y: Float = 0f
    private var speed = arrayListOf(1000, 2000)

    private lateinit var i: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLaunchBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.joystick.setZOrderOnTop(true)
        b.rg.children.forEach { it.setOnClickListener { view -> checkRBClick(view.id) } }

        b.firesGroup.children.forEach {
            if (it is ImageButton) {
                it.setOnTouchListener(OnTouchListener { v, event ->
                    if (event.action == MotionEvent.ACTION_DOWN)
                        it.imageTintList = resources.getColorStateList(
                            R.color.colorPrimary,
                            theme
                        ) else if (event.getAction() == MotionEvent.ACTION_UP)
                        it.imageTintList = resources.getColorStateList(
                            R.color.light_gray,
                            theme
                        )
                    return@OnTouchListener true
                })
            }
        }

        b.pointer.setOnClickListener {
            if (it is CheckBox) {
                pointerActive = !pointerActive
                it.background =
                    resources.getDrawable(
                        if (pointerActive) R.drawable.ic_pointer_on else R.drawable.ic_pointer_off,
                        theme
                    )
                setCBColor(it, pointerActive)
            }
        }

        b.speed.setOnClickListener {
            if (it is CheckBox) {
                speedActive = !speedActive
                setCBColor(it, speedActive)
            }
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        i = intent
        bluetooth = BluetoothMC()
        setBluetooth()

    }

    private fun sendData() {
        runBlocking {
//            ARRAY 13 bytes: INITIALIZATION (0xABCD), x * 2000, y * 2000, singleshot (0/1),
//            burts, fullauto, laserpoint, blinkerleft, emergency, blinkerright
            val currentSpeed = speed[if (b.speed.isSelected) 1 else 0]
            val bytes: Array<Byte> =
                arrayOf(
                    171.toByte(),
                    205.toByte(),
                    (x * currentSpeed / 256).toInt().toByte(),
                    (x * currentSpeed % 256).toInt().toByte(),
                    (y * currentSpeed / 256).toInt().toByte(),
                    (y * currentSpeed % 256).toInt().toByte(),
                    (if (b.fireSingle.isSelected) 1 else 0).toByte(),
                    (if (b.fireBurst.isSelected) 1 else 0).toByte(),
                    (if (b.fireAutomatic.isSelected) 1 else 0).toByte(),
                    (if (b.pointer.isSelected) 1 else 0).toByte(),
                    (if (b.blinkerLeft.isSelected) 1 else 0).toByte(),
                    (if (b.emergencyLight.isSelected) 1 else 0).toByte(),
                    (if (b.blinkerRight.isSelected) 1 else 0).toByte(),
                )
            AppUtils.log("bytes $bytes ${bytes.size}", baseContext)
//            bluetooth.send()
            delay(200)
            sendData()
        }
    }

    private fun checkRBClick(checkedId: Int) {
        controlId = if (checkedId == controlId) 0 else checkedId
        b.rg.children.forEach { cb ->
            if (cb is CheckBox) {
                if (controlId == cb.id) {
                    cb.isSelected = !cb.isSelected
                } else {
                    cb.isSelected = false
                }
                setCBColor(cb, cb.isSelected)

            }
        }
        b.rg.refreshDrawableState()
    }

    private fun setCBColor(checkBox: CheckBox, boolean: Boolean) {
        checkBox.backgroundTintList = resources.getColorStateList(
            if (boolean) R.color.colorPrimary else R.color.light_gray,
            theme
        )
    }


    private fun setBluetooth() {
        bluetooth.disconnect()
        bluetooth.setOnBluetoothConnectionListener(object : BluetoothConnectionListener {
            override fun onDeviceConnecting() {
                AppUtils.log("onDeviceConnecting", baseContext)
                //this method triggered during the connection processes
            }

            override fun onDeviceConnected() {
                //this method triggered if the connection success
                AppUtils.log("onDeviceConnected", baseContext)

                retries = 0
                sendData()
            }

            override fun onDeviceDisconnected() {
                AppUtils.log("onDeviceDisconnected  $retries", baseContext)

                //this method triggered if the device disconnected
                startMain()
            }

            override fun onDeviceConnectionFailed() {
                AppUtils.log("onDeviceConnectionFailed $retries", baseContext)

                //this method triggered if the connection failed
                startMain()
            }
        })
        bluetooth.connect(i)
        AppUtils.log(
            "${intent.getStringExtra(BluetoothStates.EXTRA_DEVICE_ADDRESS)}", baseContext
        )
    }

    private fun startMain() {
        retries++
        if (retries > 3) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            setBluetooth()
        }
    }

    override fun onJoystickMoved(xPercent: Float, yPercent: Float, id: Int) {
        AppUtils.log("id$id: x$xPercent y$yPercent", this)
        x = xPercent
        y = yPercent
    }


    override fun onStop() {
        super.onStop()
        bluetooth.disconnect()
    }
}