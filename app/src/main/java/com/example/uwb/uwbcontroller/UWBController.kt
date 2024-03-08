package com.example.uwb.uwbcontroller

import android.content.Context
import androidx.core.uwb.RangingPosition
import androidx.core.uwb.RangingResult
import androidx.core.uwb.UwbAddress
import kotlinx.coroutines.flow.Flow

interface UWBController {
    suspend fun initializeUWB(context: Context, address: String) : Flow<RangingResult>
}