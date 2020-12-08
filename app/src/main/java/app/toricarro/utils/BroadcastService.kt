package app.toricarro.utils

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.toricarro.models.BTEventType
import org.greenrobot.eventbus.EventBus


class BroadcastService : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
        when (intent.action) {
            BluetoothDevice.ACTION_FOUND -> {
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                EventBus.getDefault().post(device)
            }
            BluetoothDevice.ACTION_BOND_STATE_CHANGED -> actionBondStateChanged(
                intent.getIntExtra(
                    BluetoothDevice.ACTION_BOND_STATE_CHANGED,
                    BluetoothAdapter.ERROR
                )
            )
            BluetoothAdapter.ACTION_STATE_CHANGED -> actionStateChanged(
                intent.getIntExtra(
                    BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR
                )
            )
        }
    }

    private fun actionBondStateChanged(state: Int) {
        when (state) {
            BluetoothDevice.BOND_BONDED -> EventBus.getDefault().post(BluetoothDevice.BOND_BONDED)
            BluetoothDevice.BOND_BONDING -> {
            }
            BluetoothDevice.BOND_NONE -> {
            }
        }
    }

    private fun actionStateChanged(state: Int) {
        when (state) {
            BluetoothAdapter.STATE_OFF -> EventBus.getDefault().post(BTEventType.BT_DISABLED)
            BluetoothAdapter.STATE_TURNING_OFF -> {
            }
            BluetoothAdapter.STATE_ON -> EventBus.getDefault().post(BTEventType.BT_START)
            BluetoothAdapter.STATE_TURNING_ON -> {
            }
        }
    }
}
