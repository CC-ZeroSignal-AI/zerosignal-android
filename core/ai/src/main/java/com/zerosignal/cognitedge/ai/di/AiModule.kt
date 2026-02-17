package com.zerosignal.cognitedge.ai.di

import android.content.Context
import android.os.Build
import com.nexa.sdk.NexaSdk
import com.zerosignal.cognitedge.ai.util.AiFileLog
import com.zerosignal.cognitedge.ai.engine.NexaInferenceEngine
import com.zerosignal.cognitedge.ai.engine.NexaSdkEngine
import com.zerosignal.cognitedge.ai.repository.NexaInferenceRepository
import com.zerosignal.cognitedge.ai.repository.NexaModelRepository
import com.zerosignal.cognitedge.domain.model.UnsupportedDeviceException
import com.zerosignal.cognitedge.domain.repository.InferenceRepository
import com.zerosignal.cognitedge.domain.repository.ModelRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AiModule {

    @Binds
    @Singleton
    abstract fun bindInferenceRepository(
        impl: NexaInferenceRepository
    ): InferenceRepository

    @Binds
    @Singleton
    abstract fun bindModelRepository(
        impl: NexaModelRepository
    ): ModelRepository

    companion object {
        private const val TAG = "AiModule"

        @Provides
        @Singleton
        fun provideNexaInferenceEngine(
            @ApplicationContext context: Context,
            modelRepository: ModelRepository
        ): NexaInferenceEngine {
            AiFileLog.init(context)
            AiFileLog.i(TAG, "--- Engine provisioning started ---")
            val deviceInfo = DeviceCapability.detect()

            if (deviceInfo.isEmulator) {
                AiFileLog.e(TAG, "BLOCKED: Device is an emulator — engine creation denied")
                throw UnsupportedDeviceException(
                    "This app requires a Qualcomm Snapdragon device with NPU support. " +
                        "Emulators and non-Snapdragon devices are not supported."
                )
            }
            if (!deviceInfo.isSnapdragon) {
                AiFileLog.e(TAG, "BLOCKED: Device is not Snapdragon — engine creation denied")
                throw UnsupportedDeviceException(
                    "This app requires a Qualcomm Snapdragon device with NPU support. " +
                        "Emulators and non-Snapdragon devices are not supported."
                )
            }

            AiFileLog.i(TAG, "Device is supported (Snapdragon, non-emulator) — proceeding with SDK init")
            AiFileLog.i(TAG, "Initializing Nexa SDK...")
            NexaSdk.getInstance().init(context, object : NexaSdk.InitCallback {
                override fun onSuccess() {
                    AiFileLog.i(TAG, "Nexa SDK initialized successfully")
                }

                override fun onFailure(reason: String) {
                    AiFileLog.e(TAG, "Nexa SDK initialization failed: $reason")
                }
            })

            val npuModelPath = modelRepository.getModelPath()
            val ggufModelPath = modelRepository.getGgufModelPath()
            val nativeLibDir = context.applicationInfo.nativeLibraryDir
            AiFileLog.i(TAG, "Model paths resolved:")
            AiFileLog.i(TAG, "  NPU model:  ${npuModelPath ?: "(not downloaded)"}")
            AiFileLog.i(TAG, "  GGUF model: ${ggufModelPath ?: "(not downloaded)"}")
            AiFileLog.i(TAG, "  Native lib: $nativeLibDir")
            AiFileLog.i(TAG, "--- Engine provisioning complete — handing off to NexaSdkEngine ---")
            return NexaSdkEngine(
                npuModelPath = npuModelPath ?: "",
                ggufModelPath = ggufModelPath ?: "",
                nativeLibDir = nativeLibDir
            )
        }
    }
}

data class DeviceCapability(
    val isEmulator: Boolean,
    val isSnapdragon: Boolean
) {
    companion object {
        private const val TAG = "DeviceCapability"

        fun detect(): DeviceCapability {
            AiFileLog.i(TAG, "--- Device detection started ---")
            AiFileLog.i(TAG, "Build.FINGERPRINT = ${Build.FINGERPRINT}")
            AiFileLog.i(TAG, "Build.MODEL       = ${Build.MODEL}")
            AiFileLog.i(TAG, "Build.BRAND       = ${Build.BRAND}")
            AiFileLog.i(TAG, "Build.DEVICE      = ${Build.DEVICE}")
            AiFileLog.i(TAG, "Build.HARDWARE    = ${Build.HARDWARE}")
            AiFileLog.i(TAG, "Build.BOARD       = ${Build.BOARD}")
            AiFileLog.i(TAG, "Build.PRODUCT     = ${Build.PRODUCT}")
            AiFileLog.i(TAG, "Build.MANUFACTURER= ${Build.MANUFACTURER}")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                AiFileLog.i(TAG, "Build.SOC_MODEL   = ${Build.SOC_MODEL}")
                AiFileLog.i(TAG, "Build.SOC_MANUFACTURER = ${Build.SOC_MANUFACTURER}")
            }
            AiFileLog.i(TAG, "Build.VERSION.SDK_INT = ${Build.VERSION.SDK_INT}")

            val fingerprintGeneric = Build.FINGERPRINT.contains("generic", ignoreCase = true)
            val modelEmulator = Build.MODEL.contains("Emulator", ignoreCase = true)
            val modelSdk = Build.MODEL.contains("Android SDK", ignoreCase = true)
            val brandGeneric = Build.BRAND.startsWith("generic")
            val deviceGeneric = Build.DEVICE.startsWith("generic")
            val isEmulator = fingerprintGeneric || modelEmulator || modelSdk || brandGeneric || deviceGeneric
            AiFileLog.i(TAG, "Emulator checks — fingerprint=$fingerprintGeneric, model_emu=$modelEmulator, model_sdk=$modelSdk, brand=$brandGeneric, device=$deviceGeneric → isEmulator=$isEmulator")

            val hwQcom = Build.HARDWARE.contains("qcom", ignoreCase = true)
            val boardSm = Build.BOARD.contains("sm", ignoreCase = true)
            val socSnapdragon = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && Build.SOC_MODEL.contains("Snapdragon", ignoreCase = true)
            val isSnapdragon = hwQcom || boardSm || socSnapdragon
            AiFileLog.i(TAG, "Snapdragon checks — hw_qcom=$hwQcom, board_sm=$boardSm, soc_snapdragon=$socSnapdragon → isSnapdragon=$isSnapdragon")

            AiFileLog.i(TAG, "--- Device detection result: isEmulator=$isEmulator, isSnapdragon=$isSnapdragon ---")
            return DeviceCapability(isEmulator, isSnapdragon)
        }
    }
}
