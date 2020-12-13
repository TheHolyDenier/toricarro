package app.toricarro.views.launch

import android.bluetooth.BluetoothDevice
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
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

    private lateinit var device: BluetoothDevice
    private lateinit var bluetooth: Bluetooth

    private var sendingData = false
    private var activityOn = false

    private var fires = arrayOf(false, false, false)

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
                    fire(event, it, v)
                    return@OnTouchListener true
                })
            }
        }

        b.pointer.setOnClickListener {
            if (it is CheckBox) {
                pointerActive = !pointerActive
                it.background =
                    ResourcesCompat.getDrawable(
                        resources,
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


    private fun fire(
        event: MotionEvent,
        it: ImageButton,
        v: View
    ) {
        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE)
            it.imageTintList = resources.getColorStateList(
                R.color.colorPrimary,
                theme
            ) else if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL)
            it.imageTintList = resources.getColorStateList(
                R.color.light_gray,
                theme
            )
        firePressed(
            if (v.id == b.fireSingle.id) 0 else if (v.id == b.fireBurst.id) 1 else 2,
            event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE
        )
    }

    private fun firePressed(pos: Int, ev: Boolean): Boolean {
        for ((i, _) in fires.withIndex()) {
            fires[i] = i == pos && ev
        }
        return true
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
                checkLink()
            }

            override fun onDeviceDisconnected(device: BluetoothDevice, message: String) {
                AppUtils.log("onDeviceDisconnected", baseContext)
                startMain()
                checkLink()
            }

            override fun onMessage(message: ByteArray) {
                AppUtils.log("onDeviceDisconnected", baseContext)

            }

            override fun onError(errorCode: Int) {
                AppUtils.log("error $errorCode", baseContext)
                startMain()
                checkLink()
            }

            override fun onConnectError(device: BluetoothDevice, message: String) {
                AppUtils.log("onConnectError $message", baseContext)
                startMain()
                checkLink()

            }
        })
    }

    private fun checkLink() {
        b.linked.setImageResource(if (bluetooth != null && bluetooth.isConnected) R.drawable.ic_power else R.drawable.ic_power_off)
        b.linked.setColorFilter(getColor(if (bluetooth != null && bluetooth.isConnected) R.color.colorPrimary else R.color.light_gray))
    }

    private fun sendData() {
        GlobalScope.launch {
//            ARRAY 13 bytes: INITIALIZATION (0xABCD), x * 2000, y * 2000, singleshot (0/1),
//            burts, fullauto, laserpoint, blinkerleft, emergency, blinkerright
            val controls: Int = (
                    (if (fires[0]) 1 else 0)
                            + (if (fires[1]) 2 else 0)
                            + (if (fires[2]) 4 else 0)
                            + (if (pointerActive) 8 else 0)
                            + (if (b.blinkerLeft.isSelected) 16 else 0)
                            + (if (b.emergencyLight.isSelected) 32 else 0)
                            + (if (b.blinkerRight.isSelected) 64 else 0)
                            + if (b.speed.isSelected) 128 else 0
                    )


            val bytes =
                byteArrayOf(
                    171.toByte(),
                    205.toByte(),
                    x.toInt().toByte(),
                    y.toInt().toByte(),
                    controls.toByte()
                )

            bluetooth.send(bytes)

            AppUtils.log(
                "bytes ${bytes.joinToString(prefix = "[", postfix = "]")}} ${bytes.size}",
                baseContext
            )

            delay(1000 / 10)
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
        x = xPercent * 127
        y = yPercent * 127 * -1
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