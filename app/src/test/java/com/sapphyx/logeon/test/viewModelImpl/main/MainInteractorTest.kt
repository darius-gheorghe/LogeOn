package com.sapphyx.logeon.test.viewModelImpl.main

import app.cash.turbine.test
import com.google.firebase.auth.FirebaseUser
import com.sapphyx.logeon.data.model.UserProfile
import com.sapphyx.logeon.data.repository.AuthStatus
import com.sapphyx.logeon.data.repository.AuthenticationRepository
import com.sapphyx.logeon.data.repository.UserProfileState
import com.sapphyx.logeon.data.repository.UserRepository
import com.sapphyx.logeon.navigation.LogeOnDestinations
import com.sapphyx.logeon.navigation.NavigationCommands
import com.sapphyx.logeon.navigation.NavigationManager
import com.sapphyx.logeon.viewModelImpl.main.MainInteractor
import com.sapphyx.logeon.viewModelImpl.main.MainScreenAction
import com.sapphyx.logeon.viewModelImpl.main.MainScreenResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class MainInteractorTest {

    private val authRepository: AuthenticationRepository = mockk(relaxed = true)
    private val userRepository: UserRepository = mockk()
    private val navigationManager: NavigationManager = mockk {
        every { navigateCommands } returns MutableSharedFlow()
    }

    private val interactor = MainInteractor(authRepository, userRepository, navigationManager)

    @Test
    fun `initResults emits NotLoggedIn when AuthStatus is LoggedOut`() = runTest {
        every { authRepository.observeAuthState() } returns flowOf(AuthStatus.LoggedOut)

        interactor.initResults().test {
            assertEquals(MainScreenResult.NotLoggedIn, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initResults emits UnverifiedEmail when AuthStatus is UnverifiedEmail`() = runTest {
        every { authRepository.observeAuthState() } returns flowOf(AuthStatus.UnverifiedEmail)

        interactor.initResults().test {
            assertEquals(MainScreenResult.UnverifiedEmail, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initResults emits NotLoggedIn when Verified but currentUser is null`() = runTest {
        every { authRepository.observeAuthState() } returns flowOf(AuthStatus.Verified)
        every { authRepository.currentUser } returns null

        interactor.initResults().test {
            assertEquals(MainScreenResult.NotLoggedIn, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initResults maps UserProfileState emissions when Verified`() = runTest {
        val mockUser: FirebaseUser = mockk {
            every { uid } returns "uid-123"
        }
        every { authRepository.observeAuthState() } returns flowOf(AuthStatus.Verified)
        every { authRepository.currentUser } returns mockUser
        every { userRepository.observeUserProfile("uid-123") } returns flowOf(
            UserProfileState.Loading,
            UserProfileState.NotFound,
            UserProfileState.Error("Firestore unavailable"),
            UserProfileState.Success(UserProfile(uid = "uid-123", onboardingCompleted = true))
        )

        interactor.initResults().test {
            assertEquals(MainScreenResult.Loading, awaitItem())
            assertEquals(MainScreenResult.OnboardingRequired, awaitItem())
            assertEquals(MainScreenResult.ProfileError("Firestore unavailable"), awaitItem())
            assertEquals(MainScreenResult.LoggedIn, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initResults maps NavigationCommands into MainScreenResults`() = runTest {
        val navFlow = MutableSharedFlow<NavigationCommands>(extraBufferCapacity = 2)
        every { authRepository.observeAuthState() } returns emptyFlow()
        every { navigationManager.navigateCommands } returns navFlow

        interactor.initResults().test {
            navFlow.tryEmit(NavigationCommands.NavigateCommand(LogeOnDestinations.SignUp))
            assertEquals(
                MainScreenResult.NavigateWithDestination(LogeOnDestinations.SignUp),
                awaitItem()
            )

            navFlow.tryEmit(NavigationCommands.PopBackCommand)
            assertEquals(MainScreenResult.PopBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `RetryProfileLoad emits Loading and re-triggers observeSessionStatus`() = runTest {
        val mockUser: FirebaseUser = mockk {
            every { uid } returns "uid-123"
        }
        every { authRepository.observeAuthState() } returns flowOf(AuthStatus.Verified)
        every { authRepository.currentUser } returns mockUser
        every { userRepository.observeUserProfile("uid-123") } returnsMany listOf(
            flowOf(UserProfileState.Error("Temporary error")),
            flowOf(UserProfileState.Success(UserProfile(uid = "uid-123", onboardingCompleted = true)))
        )

        interactor.initResults().test {
            assertEquals(MainScreenResult.ProfileError("Temporary error"), awaitItem())

            interactor.actionToResult(MainScreenAction.RetryProfileLoad).test {
                assertEquals(MainScreenResult.Loading, awaitItem())
                awaitComplete()
            }

            assertEquals(MainScreenResult.LoggedIn, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `SignOutClick calls authRepository signOut and emits NotLoggedIn`() = runTest {
        interactor.actionToResult(MainScreenAction.SignOutClick).test {
            assertEquals(MainScreenResult.NotLoggedIn, awaitItem())
            awaitComplete()
        }

        verify(exactly = 1) { authRepository.signOut() }
    }

    @Test
    fun `BottomBarClick emits NavigateWithNavBar`() = runTest {
        interactor.actionToResult(MainScreenAction.BottomBarClick(LogeOnDestinations.Home.route))
            .test {
                assertEquals(
                    MainScreenResult.NavigateWithNavBar(LogeOnDestinations.Home.route),
                    awaitItem()
                )
                awaitComplete()
            }
    }
}