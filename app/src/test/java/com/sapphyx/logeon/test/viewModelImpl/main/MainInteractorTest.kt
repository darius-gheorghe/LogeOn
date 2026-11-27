package com.sapphyx.logeon.test.viewModelImpl.main

import app.cash.turbine.test
import com.sapphyx.logeon.data.repository.AuthStatus
import com.sapphyx.logeon.data.repository.AuthenticationRepository
import com.sapphyx.logeon.navigation.LogeOnDestinations
import com.sapphyx.logeon.navigation.NavigationCommands
import com.sapphyx.logeon.navigation.NavigationManager
import com.sapphyx.logeon.viewModelImpl.main.MainInteractor
import com.sapphyx.logeon.viewModelImpl.main.MainScreenResult
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class MainInteractorTest {

    private val authRepository: AuthenticationRepository = mockk()
    private val navigationManager: NavigationManager = mockk()
    private val interactor = MainInteractor(navigationManager, authRepository)

    @Test
    fun `initResults maps AuthStatus and NavigationCommands into MainScreenResults`() = runTest {
        val navFlow = MutableSharedFlow<NavigationCommands>(extraBufferCapacity = 2)
        every { authRepository.observeAuthState() } returns flowOf(
            AuthStatus.LoggedOut,
            AuthStatus.UnverifiedEmail,
            AuthStatus.Verified
        )
        every { navigationManager.navigateCommands } returns navFlow

        interactor.initResults().test {
            assertEquals(MainScreenResult.NotLoggedIn, awaitItem())
            assertEquals(MainScreenResult.UnverifiedEmail, awaitItem())
            assertEquals(MainScreenResult.LoggedIn, awaitItem())

            navFlow.tryEmit(NavigationCommands.NavigateCommand(LogeOnDestinations.SignUp))
            assertEquals(MainScreenResult.NavigateWithDestination(LogeOnDestinations.SignUp), awaitItem())

            navFlow.tryEmit(NavigationCommands.PopBackCommand)
            assertEquals(MainScreenResult.PopBack, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }
}