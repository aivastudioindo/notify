package com.example.data.scanner

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log

data class WifiScanItem(
    val ssid: String,
    val bssid: String,
    val signalDbm: Int,
    val signalPercent: Int,
    val frequencyMhz: Int,
    val capabilities: String
)

data class BluetoothScanItem(
    val name: String,
    val address: String,
    val isConnected: Boolean
)

class NetworkScanner(private val context: Context) {

    private val wifiManager: WifiManager? =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val bm = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            bm?.adapter
        } else {
            @Suppress("DEPRECATION")
            BluetoothAdapter.getDefaultAdapter()
        }
    }

    @SuppressLint("MissingPermission")
    fun scanWifiNetworks(): List<WifiScanItem> {
        val list = mutableListOf<WifiScanItem>()
        try {
            if (wifiManager == null) return list

            // Connected Wi-Fi info
            val info = wifiManager.connectionInfo
            if (info != null && info.networkId != -1) {
                val rawSsid = info.ssid?.replace("\"", "") ?: "Connected Wi-Fi"
                val bssid = info.bssid ?: "00:00:00:00:00:00"
                val rssi = info.rssi
                val level = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    wifiManager.calculateSignalLevel(rssi) * 25
                } else {
                    @Suppress("DEPRECATION")
                    WifiManager.calculateSignalLevel(rssi, 5) * 25
                }
                if (rawSsid != "<unknown ssid>") {
                    list.add(
                        WifiScanItem(
                            ssid = "$rawSsid [TERHUBUNG]",
                            bssid = bssid,
                            signalDbm = rssi,
                            signalPercent = level,
                            frequencyMhz = info.frequency,
                            capabilities = "Connected"
                        )
                    )
                }
            }

            // Scan Results
            val scanResults = wifiManager.scanResults ?: emptyList()
            for (res in scanResults) {
                val ssid = res.SSID ?: continue
                if (ssid.isBlank()) continue
                if (list.any { it.bssid.equals(res.BSSID, ignoreCase = true) }) continue

                val rssi = res.level
                val percent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    wifiManager.calculateSignalLevel(rssi) * 25
                } else {
                    @Suppress("DEPRECATION")
                    WifiManager.calculateSignalLevel(rssi, 5) * 25
                }

                list.add(
                    WifiScanItem(
                        ssid = ssid,
                        bssid = res.BSSID ?: "N/A",
                        signalDbm = rssi,
                        signalPercent = percent,
                        frequencyMhz = res.frequency,
                        capabilities = res.capabilities ?: ""
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("NetworkScanner", "Gagal memindai Wi-Fi: ${e.message}")
        }
        return list
    }

    @SuppressLint("MissingPermission")
    fun scanBluetoothDevices(): List<BluetoothScanItem> {
        val list = mutableListOf<BluetoothScanItem>()
        try {
            val adapter = bluetoothAdapter ?: return list
            if (!adapter.isEnabled) return list

            val bondedDevices = adapter.bondedDevices ?: emptySet()
            for (dev in bondedDevices) {
                val name = dev.name ?: "Unbound Device"
                val addr = dev.address ?: "00:00:00:00:00:00"
                list.add(
                    BluetoothScanItem(
                        name = name,
                        address = addr,
                        isConnected = true
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("NetworkScanner", "Gagal memindai Bluetooth: ${e.message}")
        }
        return list
    }
}
