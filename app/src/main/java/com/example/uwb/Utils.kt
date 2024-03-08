package com.example.uwb

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings

object Utils {
    fun deviceSupportCheck(context: Context) {
        val packageManager: PackageManager = context.packageManager
        val deviceSupportsUwb = packageManager.hasSystemFeature("android.hardware.uwb")
        if (deviceSupportsUwb) {
            showUwbEnableDialog(
                context,
                "Your device has UWB support. Please enable it or cancel to continue",
                "Cancel",
                "Go to setting",
                true
            )
        } else {
            showUwbEnableDialog(
                context,
                "Your device don't has UWB support. Continue or quit the app",
                "Quit",
                "Continue",
                false
            )
        }
    }

    private fun showUwbEnableDialog(
        context: Context,
        message: String,
        positiveMessage: String,
        negativeMessage: String,
        hasUWB: Boolean
    ) {
        val dialog = AlertDialog.Builder(context)
            .setTitle("Enable UWB")
            .setMessage(message)
            .setPositiveButton(positiveMessage) { dialog, _ ->
                if (hasUWB) {
                    dialog.dismiss()
                } else {
                    (context as? Activity)?.finish()
                }
            }.setNegativeButton(negativeMessage) { dialog, _ ->
                if (hasUWB) {
                    val intent = Intent(Settings.ACTION_SETTINGS)
                    context.startActivity(intent)
                } else {
                    dialog.dismiss()
                }
            }.create()
        dialog.show()
    }
}