package com.sapphyx.logeon.viewModelImpl.home

import com.sapphyx.logeon.viewModel.BaseInteractor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

object HomeScreenInteractor : BaseInteractor<HomeScreenAction, HomeScreenResult> {
    override fun actionToResult(action: HomeScreenAction): Flow<HomeScreenResult> {
        TODO("Not yet implemented")
    }

    override fun initResults(): Flow<HomeScreenResult> {
        return emptyFlow()
    }
}