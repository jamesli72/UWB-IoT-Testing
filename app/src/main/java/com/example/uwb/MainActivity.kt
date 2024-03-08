package com.example.uwb

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.uwb.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uwb.Utils.deviceSupportCheck
import com.example.uwb.adapter.DeviceAdapter
import com.example.uwb.blescanner.BleScanCallback
import com.example.uwb.blescanner.BleScanManager
import com.example.uwb.databinding.ActivityMainBinding
import com.example.uwb.model.DeviceModel
import com.example.uwb.uwbcontroller.UWBControllerImp
import com.example.uwb.viewmodel.DeviceViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val REQUEST_BLUETOOTH_PERMISSION = 1001
    private val REQUIRED_PERMISSIONS = arrayOf(
        "android.permission.BLUETOOTH",
        "android.permission.BLUETOOTH_ADMIN",
        "android.permission.BLUETOOTH_SCAN",
        "android.permission.BLUETOOTH_ADVERTISE",
        "android.permission.BLUETOOTH_CONNECT",
        "android.permission.UWB_RANGING",
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.ACCESS_COARSE_LOCATION"
    )

    private lateinit var btManager: BluetoothManager
    private lateinit var bleScanManager: BleScanManager

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: DeviceAdapter
    private val viewModel: DeviceViewModel by viewModels()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        deviceSupportCheck(this)

        //Implement Bluetooth LE Scanner
        btManager = getSystemService(BluetoothManager::class.java)
        bleScanManager =
            BleScanManager(btManager, 5000, scanCallback = BleScanCallback({ scanResult ->
                val name = if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return@BleScanCallback
                } else {
                    scanResult?.device?.name      //get device name
                }
                val address = scanResult?.device?.address  //get device address

                //Implement scan until find out a device
                if (name.isNullOrBlank() || address.isNullOrBlank()) return@BleScanCallback

                //get the device uuids
                val uuids = scanResult?.device?.uuids
                val uuidStrings = uuids?.map { it?.toString() ?: "" }?.toTypedArray()
                Log.d("UWB-UID", "UUIDs: ${uuidStrings?.contentToString()}")
                if (uuidStrings != null) {
                    viewModel.getUuidArray(uuidStrings)
                    binding.tvDistance.text = "UUIDs - recognized"
                }
                Log.d("UWB-VM","${viewModel.uuidOfUWB}")
                //get device record for the UWB address, complex channel,...
                val scanRecord = scanResult.scanRecord?.getServiceData(uuids?.get(0))
                Log.d("Record", "address: $scanRecord")
                viewModel.handleScanResult(DeviceModel(name, address))

            }))

        //Handle the scanning callback
        bleScanManager.beforeScanActions.add {
            binding.apply {
                btScan.text = "Discovering..."
                btScan.isEnabled = false
            }
        }
        bleScanManager.afterScanActions.add {
            binding.apply {
                btScan.isEnabled = true
                btScan.text = "Start Discovery"
                if (viewModel.scannedDeviceLiveData.value == null) {
                    tvDistance.text = "No device found..."
                } else {
                    viewModel.scannedDeviceLiveData.value!!.size.let {
                        if (it < 2) {
                            tvDistance.text = "Has $it device founded"
                        } else {
                            tvDistance.text = "Have $it devices founded"
                        }
                    }
                }
            }
        }

        //Adapter instance for list adapter
        adapter = DeviceAdapter()
        binding.apply {
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        }

        //Enable all features requirement
        binding.btScan.setOnClickListener {
            viewModel.resetResult()
            startScan()
        }

        //observe change
        viewModel.scannedDeviceLiveData.observe(this) {
            adapter.submitList(it)
        }
//        viewModel.uuidOfUWB.observe(this) {
//            binding.tvDistance.text = viewModel.uuidOfUWB.toString()
//        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sessionFlow =
                    UWBControllerImp.getInstance().initializeUWB(this@MainActivity, "FF:FF")
                sessionFlow.collect {
                    when (it) {
                        is RangingResult.RangingResultPosition -> doSomethingWithPosition(it.position)
                    }
                }
            } catch (e: Exception) {
                Log.d("UWB", "An error occurred: $e")
            }
        }

    }


    private fun doSomethingWithPosition(position: RangingPosition) {
        // Construct the position information string
        val positionInfo =
            "X: ${position.distance}, Y: ${position.azimuth}, Z: ${position.elevation}"

        // Set the text of the TextView to the position information
        binding.tvDistance.text = positionInfo
    }

    private fun checkPermission(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            val permissionName = permission.substring(permission.lastIndexOf('.') + 1)
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(
                    this, "$permissionName is required",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
        }
        return true
    }

    private fun startScan() {
        if (checkPermission()) {
            bleScanManager.scanBleDevices(this@MainActivity)
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_BLUETOOTH_PERMISSION
            )
            binding.btScan.text = "Start Discovery"
        }
    }


}

