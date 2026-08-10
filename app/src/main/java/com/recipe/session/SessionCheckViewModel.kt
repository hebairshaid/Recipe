package com.recipe.session

import androidx.lifecycle.ViewModel
import com.recipe.domain.session.ValidateSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SessionCheckViewModel @Inject constructor(
    private val validateSessionUseCase: ValidateSessionUseCase,
) : ViewModel() {
    suspend fun validateSession(): Boolean = validateSessionUseCase()
}
