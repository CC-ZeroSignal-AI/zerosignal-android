package com.zerosignal.cognitedge.data.di

import android.content.Context
import com.zerosignal.cognitedge.data.database.ObjectBoxProvider
import com.zerosignal.cognitedge.data.prompt.PromptBuilder
import com.zerosignal.cognitedge.data.repository.ObjectBoxContextRepository
import com.zerosignal.cognitedge.domain.repository.ContextRepository
import com.zerosignal.cognitedge.domain.repository.InferenceRepository
import com.zerosignal.cognitedge.domain.usecase.AskQuestionUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.objectbox.BoxStore
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindContextRepository(
        impl: ObjectBoxContextRepository
    ): ContextRepository

    companion object {
        @Provides
        @Singleton
        fun provideBoxStore(@ApplicationContext context: Context): BoxStore {
            return ObjectBoxProvider.get(context)
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
