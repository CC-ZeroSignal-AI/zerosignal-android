package com.zerosignal.cognitedge.data.di

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.zerosignal.cognitedge.data.database.ObjectBoxProvider
import com.zerosignal.cognitedge.data.prompt.PromptBuilder
import com.zerosignal.cognitedge.data.remote.ZeroSignalApi
import com.zerosignal.cognitedge.data.repository.ObjectBoxContextRepository
import com.zerosignal.cognitedge.data.repository.RemotePackStoreRepository
import com.zerosignal.cognitedge.domain.repository.ContextRepository
import com.zerosignal.cognitedge.domain.repository.InferenceRepository
import com.zerosignal.cognitedge.domain.repository.PackStoreRepository
import com.zerosignal.cognitedge.domain.usecase.AskQuestionUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.objectbox.BoxStore
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindContextRepository(
        impl: ObjectBoxContextRepository
    ): ContextRepository

    @Binds
    @Singleton
    abstract fun bindPackStoreRepository(
        impl: RemotePackStoreRepository
    ): PackStoreRepository

    companion object {
        private const val BASE_URL = "http://10.0.2.2:8000/"

        @Provides
        @Singleton
        fun provideBoxStore(@ApplicationContext context: Context): BoxStore {
            return ObjectBoxProvider.get(context)
        }

        @Provides
        @Singleton
        fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
            return context.getSharedPreferences("zerosignal_prefs", Context.MODE_PRIVATE)
        }

        @Provides
        @Singleton
        fun provideOkHttpClient(): OkHttpClient {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            return OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
        }

        @Provides
        @Singleton
        fun provideMoshi(): Moshi {
            return Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()
        }

        @Provides
        @Singleton
        fun provideRetrofit(client: OkHttpClient, moshi: Moshi): Retrofit {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
        }

        @Provides
        @Singleton
        fun provideZeroSignalApi(retrofit: Retrofit): ZeroSignalApi {
            return retrofit.create(ZeroSignalApi::class.java)
        }

        @Provides
        @Singleton
        fun provideAskQuestionUseCase(
            contextRepository: ContextRepository,
            inferenceRepository: InferenceRepository,
            promptBuilder: PromptBuilder
        ): AskQuestionUseCase {
            return AskQuestionUseCase(
                contextRepository = contextRepository,
                inferenceRepository = inferenceRepository,
                promptBuilder = { question, contexts -> promptBuilder.buildPrompt(question, contexts) }
            )
        }
    }
}
