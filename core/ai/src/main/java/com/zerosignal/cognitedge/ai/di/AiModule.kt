package com.zerosignal.cognitedge.ai.di

import android.content.Context
import android.os.Build
import com.zerosignal.cognitedge.ai.engine.FakeNexaInferenceEngine
import com.zerosignal.cognitedge.ai.engine.NexaInferenceEngine
import com.zerosignal.cognitedge.ai.engine.NexaSdkEngine
import com.zerosignal.cognitedge.ai.repository.NexaInferenceRepository
import com.zerosignal.cognitedge.ai.repository.NexaModelRepository
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
            return when {
                deviceInfo.isEmulator -> FakeNexaInferenceEngine()
                deviceInfo.isSnapdragon -> {
                    val modelPath = modelRepository.getModelPath()
                        ?: return FakeNexaInferenceEngine()
                    NexaSdkEngine(modelPath, pluginId = "npu")
                }
                else -> {
                    val modelPath = modelRepository.getModelPath()
                        ?: return FakeNexaInferenceEngine()
                    NexaSdkEngine(modelPath, pluginId = "cpu_gpu")
                }
            }
        }
    }
}

internal data class DeviceCapability(
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
