package com.sapphyx.logeon.viewModelImpl.profile

import com.sapphyx.logeon.viewModel.BaseInteractor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

object ProfileScreenInteractor : BaseInteractor<ProfileScreenAction, ProfileScreenResult> {
    override fun actionToResult(action: ProfileScreenAction): Flow<ProfileScreenResult> {
        TODO("Not yet implemented")
    }

    override fun initResults(): Flow<ProfileScreenResult> {
        return emptyFlow()
    }
}