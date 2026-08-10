package com.recipe.di

import com.recipe.chat.data.ai.TfliteIngredientAnalyzer
import com.recipe.chat.domain.repository.IngredientAnalyzer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AiModule {
    @Binds
    @Singleton
    abstract fun bindIngredientAnalyzer(impl: TfliteIngredientAnalyzer): IngredientAnalyzer
}
