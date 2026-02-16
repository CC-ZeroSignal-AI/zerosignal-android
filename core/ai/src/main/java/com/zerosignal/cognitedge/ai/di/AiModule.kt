package com.zerosignal.cognitedge.ai.di

import android.content.Context
import android.os.Build
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
        @Provides
        @Singleton
        fun provideNexaInferenceEngine(
            @ApplicationContext context: Context,
            modelRepository: ModelRepository
        ): NexaInferenceEngine {
            val deviceInfo = DeviceCapability.detect()

            if (deviceInfo.isEmulator || !deviceInfo.isSnapdragon) {
                throw UnsupportedDeviceException(
                    "This app requires a Qualcomm Snapdragon device with NPU support. " +
                        "Emulators and non-Snapdragon devices are not supported."
                )
            }

            // Initialize Nexa SDK
            // init takes (Context, InitCallback) but second arg is optional?
            // If not optional, we pass null or empty callback
            // Checking javap output: public final void init(Context, InitCallback)
            // There is no init$default for the callback. So we must provide it.
            // But InitCallback is an interface. We can pass a simple implementation.
            
            NexaSdk.getInstance().init(context, object : NexaSdk.InitCallback {
                override fun onSuccess() {
                    // Initialization successful
                }

                override fun onFailure(error: String) {
                    // Initialization failed
                    // Since this is just initialization, we might log it but can't really crash here
                    // The engine will fail later if SDK isn't initialized
                }
            })

            val modelPath = modelRepository.getModelPath()
            // Model path may be null if not yet downloaded — the engine is still
            // provided but will fail at inference time. The SetupScreen gate
            // enforces download before reaching the chat screen.
            return NexaSdkEngine(modelPath ?: "", pluginId = "npu")
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
