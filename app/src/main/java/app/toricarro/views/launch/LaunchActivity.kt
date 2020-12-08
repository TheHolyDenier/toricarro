package app.toricarro.views.launch

import android.bluetooth.BluetoothDevice
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import app.toricarro.R
import app.toricarro.databinding.ActivityLaunchBinding
import app.toricarro.utils.AppUtils
import kotlinx.coroutines.*
import me.aflak.bluetooth.Bluetooth
import me.aflak.bluetooth.interfaces.DeviceCallback


class LaunchActivity : AppCompatActivity(), JoystickView.JoystickListener {
    private lateinit var b: ActivityLaunchBinding

    private var controlId: Int = 0
    private var pointerActive: Boolean = false
    private var speedActive: Boolean = false

    private var retries = 0
    private var x: Float = 0f
    private var y: Float = 0f
    private var speed = arrayListOf(1000, 2000)

    private lateinit var device: BluetoothDevice
    private lateinit var bluetooth: Bluetooth

    private var sendingData = false
    private var activityOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLaunchBinding.inflate(layoutInflater)
        setContentView(b.root)

        device = intent.getParcelableExtra("MAC")!!

        bluetooth = Bluetooth(this)

        b.joystick.setZOrderOnTop(true)
        b.rg.children.forEach { it.setOnClickListener { view -> checkRBClick(view.id) } }

        b.firesGroup.children.forEach {
            if (it is ImageButton) {
                it.setOnTouchListener(OnTouchListener { v, event ->
                    if (event.action == MotionEvent.ACTION_DOWN)
                        it.imageTintList = resources.getColorStateList(
                            R.color.colorPrimary,
                            theme
                        ) else if (event.action == MotionEvent.ACTION_UP)
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


    }

    private fun setBluetooth() {
        AppUtils.log("${device.name} ${device.address}", this)
        bluetooth.connectToDevice(device)
        bluetooth.setDeviceCallback(object : DeviceCallback {
            override fun onDeviceConnected(device: BluetoothDevice) {
                AppUtils.log("onDeviceConnected", baseContext)
                retries = 0
                sendingData = true
                sendData()
            }

            override fun onDeviceDisconnected(device: BluetoothDevice, message: String) {
                AppUtils.log("onDeviceDisconnected", baseContext)
                startMain()

            }

            override fun onMessage(message: ByteArray) {
                AppUtils.log("onDeviceDisconnected", baseContext)

            }

            override fun onError(errorCode: Int) {
                AppUtils.log("error $errorCode", baseContext)
                startMain()
            }

            override fun onConnectError(device: BluetoothDevice, message: String) {
                AppUtils.log("onConnectError $message", baseContext)
                startMain()


            }
        })
    }

    private fun sendData() {
        GlobalScope.launch {
//            ARRAY 13 bytes: INITIALIZATION (0xABCD), x * 2000, y * 2000, singleshot (0/1),
//            burts, fullauto, laserpoint, blinkerleft, emergency, blinkerright
            val currentSpeed = speed[if (b.speed.isSelected) 1 else 0]
            val bytes =
                byteArrayOf(
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
            bluetooth.send(bytes)
            AppUtils.log("bytes $bytes ${bytes.size}", baseContext)
            delay(200)
            if (sendingData && activityOn) sendData()
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


    private fun startMain() {
        runOnUiThread {
            Toast.makeText(this, "Sin conexión con el dispositivo", Toast.LENGTH_LONG)
                .show()
        }
        retries++
        sendingData = false
        AppUtils.log("startMain $retries", baseContext)
        runBlocking {
            delay(1000)
            if (activityOn) setBluetooth()
        }
    }

    override fun onJoystickMoved(xPercent: Float, yPercent: Float, id: Int) {
        AppUtils.log("id$id: x$xPercent y$yPercent", this)
        x = xPercent
        y = yPercent
    }


    override fun onStart() {
        super.onStart()
        activityOn = true
        bluetooth.onStart()
        if (bluetooth.isEnabled) {
            setBluetooth()
        } else {
            bluetooth.showEnableDialog(this)
        }
    }

    override fun onStop() {
        activityOn = false
        super.onStop()
        if (bluetooth.isConnected) bluetooth.disconnect()
        bluetooth.onStop()
    }


}