package app.toricarro.views

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import app.toricarro.R
import app.toricarro.views.launch.LaunchActivity
import com.ahmedabdelmeged.bluetoothmc.BluetoothMC
import com.ahmedabdelmeged.bluetoothmc.ui.BluetoothDevices
import com.ahmedabdelmeged.bluetoothmc.util.BluetoothStates


class MainActivity : AppCompatActivity() {
    private lateinit var bluetooth: BluetoothMC

    private val linkDevice =
        registerForActivityResult(
            StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val mac = result.data?.getStringExtra(BluetoothStates.EXTRA_DEVICE_ADDRESS)
                if (mac != null && mac.isNotEmpty()) {
                    val i = Intent(this, LaunchActivity::class.java)
                    i.putExtra(BluetoothStates.EXTRA_DEVICE_ADDRESS, result.data)
                    startActivity(Intent(this, LaunchActivity::class.java))
                    finish()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val bluetooth = BluetoothMC()
        if (!bluetooth.isBluetoothAvailable) {
            //do any action if the bluetooth is not available
        } else if (!bluetooth.isBluetoothEnabled) {
            bluetooth.enableBluetooth()
        } else {
            linkDevice.launch(Intent(this, BluetoothDevices::class.java))
        }
    }
}