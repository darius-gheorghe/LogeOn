package com.sapphyx.logeon.test.viewModelImpl.main

import app.cash.turbine.test
import com.sapphyx.logeon.navigation.LogeOnDestinations
import com.sapphyx.logeon.util.MainDispatcherRule
import com.sapphyx.logeon.viewModelImpl.main.MainInteractor
import com.sapphyx.logeon.viewModelImpl.main.MainScreenAction
import com.sapphyx.logeon.viewModelImpl.main.MainScreenEffect
import com.sapphyx.logeon.viewModelImpl.main.MainScreenResult
import com.sapphyx.logeon.viewModelImpl.main.MainScreenState
import com.sapphyx.logeon.viewModelImpl.main.MainViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial auth state from Loading updates uiState without emitting AuthNavigation`() =
        runTest {
            val resultsFlow = MutableSharedFlow<MainScreenResult>(extraBufferCapacity = 4)
            val interactor: MainInteractor = mockk {
                every { initResults() } returns resultsFlow
            }

            val viewModel = MainViewModel(interactor)
            assertEquals(MainScreenState.Loading, viewModel.uiState.value)

            viewModel.effect.test {
                resultsFlow.tryEmit(MainScreenResult.OnboardingRequired)
                assertEquals(MainScreenState.OnboardingRequired, viewModel.uiState.value)
                expectNoEvents()

                // Duplicate state emits no effect
                resultsFlow.tryEmit(MainScreenResult.OnboardingRequired)
                assertEquals(MainScreenState.OnboardingRequired, viewModel.uiState.value)
                expectNoEvents()
            }
        }

    @Test
    fun `transitions between auth and onboarding states emit AuthNavigation effects`() = runTest {
        val resultsFlow = MutableSharedFlow<MainScreenResult>(extraBufferCapacity = 8)
        val interactor: MainInteractor = mockk {
            every { initResults() } returns resultsFlow
        }

        val viewModel = MainViewModel(interactor)

        viewModel.effect.test {
            // Initial transition from Loading -> NotLoggedIn (no effect)
            resultsFlow.tryEmit(MainScreenResult.NotLoggedIn)
            assertEquals(MainScreenState.NotLoggedIn, viewModel.uiState.value)
            expectNoEvents()

            // NotLoggedIn -> UnverifiedEmail
            resultsFlow.tryEmit(MainScreenResult.UnverifiedEmail)
            assertEquals(MainScreenState.UnverifiedEmail, viewModel.uiState.value)
            assertEquals(
                MainScreenEffect.AuthNavigation(LogeOnDestinations.VerifyEmail.route),
                awaitItem()
            )

            // UnverifiedEmail -> OnboardingRequired
            resultsFlow.tryEmit(MainScreenResult.OnboardingRequired)
            assertEquals(MainScreenState.OnboardingRequired, viewModel.uiState.value)
            assertEquals(
                MainScreenEffect.AuthNavigation(LogeOnDestinations.Onboarding.route),
                awaitItem()
            )

            // OnboardingRequired -> LoggedIn
            resultsFlow.tryEmit(MainScreenResult.LoggedIn)
            assertEquals(MainScreenState.LoggedIn, viewModel.uiState.value)
            assertEquals(
                MainScreenEffect.AuthNavigation(LogeOnDestinations.Home.route),
                awaitItem()
            )

            // LoggedIn -> NotLoggedIn (Sign Out)
            resultsFlow.tryEmit(MainScreenResult.NotLoggedIn)
            assertEquals(MainScreenState.NotLoggedIn, viewModel.uiState.value)
            assertEquals(
                MainScreenEffect.AuthNavigation(LogeOnDestinations.Login.route),
                awaitItem()
            )
        }
    }

    @Test
    fun `ProfileError transitions to Error state and recovery emits no AuthNavigation`() =
        runTest {
            val resultsFlow = MutableSharedFlow<MainScreenResult>(extraBufferCapacity = 4)
            val interactor: MainInteractor = mockk {
                every { initResults() } returns resultsFlow
            }

            val viewModel = MainViewModel(interactor)

            viewModel.effect.test {
                resultsFlow.tryEmit(MainScreenResult.ProfileError("Network timeout"))
                assertEquals(MainScreenState.Error("Network timeout"), viewModel.uiState.value)
                expectNoEvents()

                resultsFlow.tryEmit(MainScreenResult.Loading)
                assertEquals(MainScreenState.Loading, viewModel.uiState.value)
                expectNoEvents()

                resultsFlow.tryEmit(MainScreenResult.LoggedIn)
                assertEquals(MainScreenState.LoggedIn, viewModel.uiState.value)
                expectNoEvents()
            }
        }

    @Test
    fun `navigation results and BottomBarClick emit expected navigation effects`() = runTest {
        val resultsFlow = MutableSharedFlow<MainScreenResult>(extraBufferCapacity = 4)
        val interactor: MainInteractor = mockk {
            every { initResults() } returns resultsFlow
            every {
                actionToResult(MainScreenAction.BottomBarClick(LogeOnDestinations.Profile.route))
            } returns flowOf(MainScreenResult.NavigateWithNavBar(LogeOnDestinations.Profile.route))
        }

        val viewModel = MainViewModel(interactor)

        viewModel.effect.test {
            resultsFlow.tryEmit(MainScreenResult.NavigateWithDestination(LogeOnDestinations.SignUp))
            assertEquals(
                MainScreenEffect.NavigateWithDestination(LogeOnDestinations.SignUp.route),
                awaitItem()
            )

            viewModel.processUiAction(
                MainScreenAction.BottomBarClick(LogeOnDestinations.Profile.route)
            )
            assertEquals(
                MainScreenEffect.NavigateWithNavBar(LogeOnDestinations.Profile.route),
                awaitItem()
            )

            resultsFlow.tryEmit(MainScreenResult.PopBack)
            assertEquals(MainScreenEffect.PopBack, awaitItem())
        }
    }
}