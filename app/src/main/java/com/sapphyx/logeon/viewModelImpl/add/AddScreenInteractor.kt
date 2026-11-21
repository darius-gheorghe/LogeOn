package com.sapphyx.logeon.viewModelImpl.add

import com.sapphyx.logeon.viewModel.BaseInteractor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

object AddScreenInteractor : BaseInteractor<AddScreenAction, AddScreenResult> {
    override fun actionToResult(action: AddScreenAction): Flow<AddScreenResult> {
        TODO("Not yet implemented")
    }

    override fun initResults(): Flow<AddScreenResult> {
        return emptyFlow()
    }
}