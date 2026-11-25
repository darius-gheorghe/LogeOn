package com.sapphyx.logeon.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

interface IBaseViewModel<STATE, ACTION, EFFECT> {

    val uiState: StateFlow<STATE>
    val effect: SharedFlow<EFFECT>
    fun processUiAction(action: ACTION)
}

abstract class BaseViewModel<STATE, ACTION, EFFECT, RESULT>(
    private val interactor: BaseInteractor<ACTION, RESULT>,
    initialUiState: STATE
) : IBaseViewModel<STATE, ACTION, EFFECT>,
    ViewModel() {

    private val _effect: MutableSharedFlow<EFFECT> = MutableSharedFlow(extraBufferCapacity = 64)
    private val _actionToResultFlow: MutableSharedFlow<RESULT> = MutableSharedFlow()

    abstract suspend fun handleResult(previousState: STATE, result: RESULT): STATE

    override val uiState = merge(_actionToResultFlow, interactor.initResults())
        .scan(initialUiState, ::handleResult)
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialUiState)

    override val effect = _effect.asSharedFlow()

    override fun processUiAction(action: ACTION) {
        viewModelScope.launch {
            val result = interactor.actionToResult(action)
            _actionToResultFlow.emitAll(result)
        }
    }

    suspend fun emitEffect(effect: EFFECT) {
        _effect.emit(effect)
    }
}