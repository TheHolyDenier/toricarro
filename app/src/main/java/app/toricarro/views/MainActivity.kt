package app.toricarro.views

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import app.toricarro.databinding.ActivityMainBinding
import app.toricarro.models.ConnectBluetooth
import app.toricarro.utils.BroadcastService
import app.toricarro.views.launch.LaunchActivity
import app.toricarro.views.main.DeviceAdapter
import me.aflak.bluetooth.Bluetooth
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : AppCompatActivity() {
    private var adapter: BluetoothAdapter? = null
    private var broadcastRegistered = false
    private var receiver: BroadcastService? = null

    private lateinit var bluetooth: Bluetooth

    private lateinit var deviceAdapter: DeviceAdapter

    private lateinit var b: ActivityMainBinding

    private var opening = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        permissions.launch(
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )

        deviceAdapter = DeviceAdapter(this)
        b.rv.layoutManager = LinearLayoutManager(this)
        b.rv.adapter = deviceAdapter

        b.swipe.setOnRefreshListener { linkDevice() }

        bluetooth = Bluetooth(this)

    }

    private fun linkDevice() {

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            setBluetoothScan()
        } else {
            permissions.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    private fun setBluetoothScan() {
        if (adapter == null) adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter!!.isEnabled) {
            registerReceiver()
            startScanning()
        }
    }

    private fun registerReceiver() {
        if (!broadcastRegistered) {
            receiver = BroadcastService()
            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            registerReceiver(receiver, filter)
            broadcastRegistered = true
        }
    }

    private fun startScanning() {
        if (!broadcastRegistered) registerReceiver()
        adapter!!.startDiscovery()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(device: BluetoothDevice?) {
        deviceAdapter.addDevice(device!!)
        b.swipe.isRefreshing = false
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(connect: ConnectBluetooth) {
        opening = true
        if (opening) {
            bluetooth.pair(connect.device, "5467")
            val intent = Intent(this, LaunchActivity::class.java)
            intent.putExtra("MAC", connect.device)
            startActivity(intent)
        }
    }


    override fun onStart() {
        super.onStart()

        if (receiver == null) receiver = BroadcastService()
        linkDevice()
        bluetooth.onStart()

        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
        bluetooth.onStop()

        adapter!!.cancelDiscovery()

        broadcastRegistered = false
        try {
            unregisterReceiver(receiver)
        } catch (e: Exception) {

        }
    }

    private val permissions: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var changed = false
            permissions.entries.forEach {
                if (!changed && it.value) changed = true
            }
            if (changed) linkDevice()
        }
}