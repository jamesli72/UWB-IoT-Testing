package com.example.uwb.blescanner

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast

class BleScanManager(
    btManager: BluetoothManager,
    private val scanPeriod: Long = DEFAULT_SCAN_PERIOD,
    private val scanCallback: BleScanCallback = BleScanCallback()
) {
    private val btAdapter = btManager.adapter
    private val bleScanner = btAdapter.bluetoothLeScanner

    var beforeScanActions: MutableList<() -> Unit> = mutableListOf()
    var afterScanActions: MutableList<() -> Unit> = mutableListOf()

    /** True when the manager is performing the scan */
    private var scanning = false

    private val handler = Handler(Looper.getMainLooper())

    val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER) // Set the scan mode (e.g., SCAN_MODE_LOW_LATENCY)
        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES) // Set the callback type (e.g., CALLBACK_TYPE_ALL_MATCHES)
        .setMatchMode(ScanSettings.MATCH_MODE_STICKY) // Set the match mode (e.g., MATCH_MODE_STICKY)
        .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT) // Set the number of matches (e.g., MATCH_NUM_MAX_ADVERTISEMENT)
        .setReportDelay(0) // Set the report delay (0 for no delay)
        .build()

    val scanFilterBuilder = ScanFilter.Builder()
        .setDeviceName("Redmi Note 7")
        //.setDeviceAddress("FE:F9:1A:C4:94:8F")
        .build()
    val scanFilters = mutableListOf<ScanFilter>()

    /**
     * Scans for Bluetooth LE devices and stops the scan after [scanPeriod] seconds.
     * Does not checks the required permissions are granted, check must be done beforehand.
     */
    @SuppressLint("MissingPermission")
    fun scanBleDevices(context : Context) {
        fun stopScan() {
            scanning = false
            bleScanner.stopScan(scanCallback)

            // execute all the functions to execute after scanning
            executeAfterScanActions()
        }

        // scans for bluetooth LE devices
        if (scanning) {
            stopScan()
        } else {
            // stops scanning after scanPeriod millis
            handler.postDelayed({ stopScan() }, scanPeriod)

            executeBeforeScanActions()
            // starts scanning
            scanning = true
            scanFilters.add(scanFilterBuilder)
            if (bleScanner != null) {
                bleScanner.startScan(null, scanSettings, scanCallback)
            }
            else
            {
                Toast.makeText(context,"Your device don't support BLE",Toast.LENGTH_SHORT).show()
                (context as? Activity)?.finish()
            }
        }
    }

    companion object {
        const val DEFAULT_SCAN_PERIOD: Long = 10000

        private fun executeListOfFunctions(toExecute: List<() -> Unit>) {
            toExecute.forEach {
                it()
            }
        }
    }

    private fun executeBeforeScanActions() {
        executeListOfFunctions(beforeScanActions)
    }

    private fun executeAfterScanActions() {
        executeListOfFunctions(afterScanActions)
    }
}