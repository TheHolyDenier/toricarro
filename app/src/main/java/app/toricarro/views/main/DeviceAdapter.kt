package app.toricarro.views.main

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class DeviceAdapter(
    private val context: Context,
    private val devices: ArrayList<BluetoothDevice> = ArrayList()
) :
    RecyclerView.Adapter<DeviceHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceHolder {
        val inflater = LayoutInflater.from(parent.context)
        return DeviceHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: DeviceHolder, position: Int) {
        holder.bind(devices[position])
    }

    override fun getItemCount(): Int {
        return devices.size
    }

    fun addDevice(device: BluetoothDevice) {
        if (!devices.contains(device)) {
            devices.add(device)
            notifyDataSetChanged()
        }
    }
}