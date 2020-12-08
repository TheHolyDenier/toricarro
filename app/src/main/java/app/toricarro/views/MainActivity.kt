package app.toricarro.views

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import app.toricarro.databinding.ActivityMainBinding
import app.toricarro.views.launch.LaunchActivity
import com.ahmedabdelmeged.bluetoothmc.BluetoothMC
import com.ahmedabdelmeged.bluetoothmc.ui.BluetoothDevices
import com.ahmedabdelmeged.bluetoothmc.util.BluetoothStates


class MainActivity : AppCompatActivity() {
    private lateinit var b: ActivityMainBinding

    private val permissions: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var changed = false
            permissions.entries.forEach {
                AppUtils.log("${it.key} = ${it.value}", this)
                if (!changed && it.value) changed = true
            }
            if (changed) linkDevice()
        }

    private val searchDevice =
        registerForActivityResult(
            StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val mac = result.data?.getStringExtra(BluetoothStates.EXTRA_DEVICE_ADDRESS)
                if (mac != null && mac.isNotEmpty()) {
                    val i = Intent(this, LaunchActivity::class.java)
                    i.putExtra(BluetoothStates.EXTRA_DEVICE_ADDRESS, mac)
                    startActivity(i)
                    finish()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.btn.setOnClickListener { linkDevice() }

        permissions.launch(
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )

    }

    private fun linkDevice() {
        val bluetooth = BluetoothMC()
        if (!bluetooth.isBluetoothAvailable) {
            //do any action if the bluetooth is not available
        } else if (!bluetooth.isBluetoothEnabled) {
            bluetooth.enableBluetooth()
        } else {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.BLUETOOTH
                ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {

                searchDevice.launch(Intent(this, BluetoothDevices::class.java))
            } else {
                permissions.launch(
                    arrayOf(
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
            }
        }
    }
}