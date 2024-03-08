package com.example.uwb.uwbcontroller

import android.content.Context
import androidx.core.uwb.*
import kotlinx.coroutines.flow.Flow
import kotlin.random.Random

class UWBControllerImp : UWBController {



    override suspend fun initializeUWB(context: Context, address: String): Flow<RangingResult> {

        //uwbDevice with the need UWB address from the anchor or controllee devices
        val uwbDevice = UwbDevice.createForAddress(address)
        val uwbAddress = UwbAddress(address)
        val UUID = "0xDECA7F83C8B24C92"
        val uwbChannel = UwbComplexChannel(5,9)

        val uwbManager = UwbManager.createInstance(context)
        val controllerSession = uwbManager.controllerSessionScope()
        //controllerSession.addControlee(uwbAddress)
        //val controlleeSession = uwbManager.controleeSessionScope()

        val rangingParams = RangingParameters(
            uwbConfigType = RangingParameters.CONFIG_UNICAST_DS_TWR,
            sessionId = Random.nextInt(),
            subSessionId = Random.nextInt(),
            subSessionKeyInfo = ByteArray(16).apply { Random.nextBytes(this) },
            sessionKeyInfo = Random.nextBytes(8),
            complexChannel = uwbChannel,
            peerDevices = listOf(uwbDevice),
            updateRateType = RangingParameters.RANGING_UPDATE_RATE_FREQUENT
        )

        val sessionFlow = controllerSession.prepareSession(rangingParams)
        return sessionFlow
    }


    //create instance
    companion object {
        @Volatile
        private var instance: UWBControllerImp? = null

        fun getInstance(): UWBControllerImp {
            return instance ?: synchronized(this) {
                instance ?: UWBControllerImp().also { instance = it }
            }
        }
    }

}