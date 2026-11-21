package com.sapphyx.logeon.viewModelImpl.pulse

import com.sapphyx.logeon.viewModel.BaseInteractor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

object PulseScreenInteractor : BaseInteractor<PulseScreenAction, PulseScreenResult> {
    override fun actionToResult(action: PulseScreenAction): Flow<PulseScreenResult> {
        TODO("Not yet implemented")
    }

    override fun initResults(): Flow<PulseScreenResult> {
        return emptyFlow()
    }
}