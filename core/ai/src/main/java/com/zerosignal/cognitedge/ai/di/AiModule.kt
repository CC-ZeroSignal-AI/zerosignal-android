package com.zerosignal.cognitedge.ai.di

import android.content.Context
import android.os.Build
import android.util.Log
import com.nexa.sdk.NexaSdk
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
            val deviceInfo = DeviceCapability.detect()
            Log.i(TAG, "Device capability — emulator=${deviceInfo.isEmulator}, snapdragon=${deviceInfo.isSnapdragon}")

            if (deviceInfo.isEmulator || !deviceInfo.isSnapdragon) {
                Log.e(TAG, "Unsupported device — blocking engine creation")
                throw UnsupportedDeviceException(
                    "This app requires a Qualcomm Snapdragon device with NPU support. " +
                        "Emulators and non-Snapdragon devices are not supported."
                )
            }

            Log.i(TAG, "Initializing Nexa SDK")
            NexaSdk.getInstance().init(context, object : NexaSdk.InitCallback {
                override fun onSuccess() {
                    Log.i(TAG, "Nexa SDK initialized successfully")
                }

                override fun onFailure(reason: String) {
                    Log.e(TAG, "Nexa SDK initialization failed: $reason")
                }
            })

            val npuModelPath = modelRepository.getModelPath()
            val ggufModelPath = modelRepository.getGgufModelPath()
            val nativeLibDir = context.applicationInfo.nativeLibraryDir
            Log.i(TAG, "NPU model path: ${npuModelPath ?: "(not downloaded)"}")
            Log.i(TAG, "GGUF model path: ${ggufModelPath ?: "(not downloaded)"}")
            Log.i(TAG, "Native lib dir: $nativeLibDir")
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
        fun detect(): DeviceCapability {
            val isEmulator = Build.FINGERPRINT.contains("generic", ignoreCase = true)
                || Build.MODEL.contains("Emulator", ignoreCase = true)
                || Build.MODEL.contains("Android SDK", ignoreCase = true)
                || Build.BRAND.startsWith("generic")
                || Build.DEVICE.startsWith("generic")

            val isSnapdragon = Build.HARDWARE.contains("qcom", ignoreCase = true)
                || Build.BOARD.contains("sm", ignoreCase = true)
                || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    && Build.SOC_MODEL.contains("Snapdragon", ignoreCase = true))

            return DeviceCapability(isEmulator, isSnapdragon)
        }
    }
}
