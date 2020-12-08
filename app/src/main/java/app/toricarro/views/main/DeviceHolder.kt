package app.toricarro.views.main

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.toricarro.R
import org.greenrobot.eventbus.EventBus

class DeviceHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.item_device, parent, false)) {
    private var nameTv: TextView? = null
    private var macTv: TextView? = null
    private var linearLayout: LinearLayout? = null


    init {
        linearLayout = itemView.findViewById(R.id.item)
        nameTv = itemView.findViewById(R.id.name_tv)
        macTv = itemView.findViewById(R.id.mac_tv)
    }

    fun bind(device: BluetoothDevice) {
        nameTv?.text = device.name
        macTv?.text = device.address
        linearLayout!!.setOnClickListener { EventBus.getDefault().post(device.address) }
    }

}