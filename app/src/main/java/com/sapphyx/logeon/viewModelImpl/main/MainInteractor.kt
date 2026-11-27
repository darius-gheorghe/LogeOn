package com.sapphyx.logeon.viewModelImpl.main

import com.sapphyx.logeon.data.repository.AuthStatus
import com.sapphyx.logeon.data.repository.AuthenticationRepository
import com.sapphyx.logeon.data.repository.UserProfileState
import com.sapphyx.logeon.data.repository.UserRepository
import com.sapphyx.logeon.navigation.NavigationCommands
import com.sapphyx.logeon.navigation.NavigationManager
import com.sapphyx.logeon.viewModel.BaseInteractor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class MainInteractor @Inject constructor(
    private val authRepository: AuthenticationRepository,
    private val userRepository: UserRepository,
    private val navigationManager: NavigationManager
) : BaseInteractor<MainScreenAction, MainScreenResult> {

    private val retryTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override fun initResults(): Flow<MainScreenResult> = merge(
        observeSessionStatus(),
        observeNavigationCommands()
    )

    override fun actionToResult(action: MainScreenAction): Flow<MainScreenResult> =
        when (action) {
            is MainScreenAction.BottomBarClick ->
                flowOf(MainScreenResult.NavigateWithNavBar(action.route))

            is MainScreenAction.RetryProfileLoad -> {
                retryTrigger.tryEmit(Unit)
                flowOf(MainScreenResult.Loading)
            }

            is MainScreenAction.SignOutClick -> {
                authRepository.signOut()
                flowOf(MainScreenResult.NotLoggedIn)
            }
        }

    private fun observeSessionStatus(): Flow<MainScreenResult> =
        retryTrigger.onStart { emit(Unit) }.flatMapLatest {
            authRepository.observeAuthState().flatMapLatest { authStatus ->
                when (authStatus) {
                    is AuthStatus.LoggedOut -> flowOf(MainScreenResult.NotLoggedIn)
                    is AuthStatus.UnverifiedEmail -> flowOf(MainScreenResult.UnverifiedEmail)
                    is AuthStatus.Verified -> {
                        val uid = authRepository.currentUser?.uid
                        if (uid == null) {
                            flowOf(MainScreenResult.NotLoggedIn)
                        } else {
                            userRepository.observeUserProfile(uid).map { profileState ->
                                when (profileState) {
                                    is UserProfileState.Loading -> MainScreenResult.Loading
                                    is UserProfileState.Success -> MainScreenResult.LoggedIn
                                    is UserProfileState.NotFound -> MainScreenResult.OnboardingRequired
                                    is UserProfileState.Error ->
                                        MainScreenResult.ProfileError(profileState.message)
                                }
                            }
                        }
                    }
                }
            }
        }

    private fun observeNavigationCommands(): Flow<MainScreenResult> =
        navigationManager.navigateCommands.map { command ->
            when (command) {
                is NavigationCommands.NavigateCommand ->
                    MainScreenResult.NavigateWithDestination(command.destination)

                is NavigationCommands.PopBackCommand ->
                    MainScreenResult.PopBack
            }
        }
}