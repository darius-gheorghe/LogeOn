package com.sapphyx.logeon.test.viewModelImpl.onboarding

import app.cash.turbine.test
import com.google.firebase.auth.FirebaseUser
import com.sapphyx.logeon.data.repository.AuthenticationRepository
import com.sapphyx.logeon.data.repository.OnboardingResponse
import com.sapphyx.logeon.data.repository.UserRepository
import com.sapphyx.logeon.data.repository.util.user.UsernameValidationResult
import com.sapphyx.logeon.viewModelImpl.onboarding.OnboardingScreenAction
import com.sapphyx.logeon.viewModelImpl.onboarding.OnboardingScreenInteractor
import com.sapphyx.logeon.viewModelImpl.onboarding.OnboardingScreenResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class OnboardingScreenInteractorTest {

    private val authRepository: AuthenticationRepository = mockk(relaxed = true)
    private val userRepository: UserRepository = mockk()
    private val interactor = OnboardingScreenInteractor(authRepository, userRepository)

    @Test
    fun `initResults returns empty flow`() = runTest {
        interactor.initResults().test {
            awaitComplete()
        }
    }

    @Test
    fun `UsernameChanged normalizes to lowercase and emits UsernameChecked after debounce`() =
        runTest {
            coEvery {
                userRepository.checkUsernameAvailable("alex_1")
            } returns UsernameValidationResult.CORRECT

            interactor.actionToResult(OnboardingScreenAction.UsernameChanged("Alex_1")).test {
                assertEquals(OnboardingScreenResult.UsernameUpdated("alex_1"), awaitItem())
                assertEquals(
                    OnboardingScreenResult.UsernameChecked("alex_1", UsernameValidationResult.CORRECT),
                    awaitItem()
                )
                awaitComplete()
            }
        }

    @Test
    fun `field selection actions emit corresponding updated results`() = runTest {
        interactor.actionToResult(OnboardingScreenAction.DisplayNameChanged("Alex")).test {
            assertEquals(OnboardingScreenResult.DisplayNameUpdated("Alex"), awaitItem())
            awaitComplete()
        }
        interactor.actionToResult(OnboardingScreenAction.BioChanged("Hello")).test {
            assertEquals(OnboardingScreenResult.BioUpdated("Hello"), awaitItem())
            awaitComplete()
        }
        interactor.actionToResult(OnboardingScreenAction.DateOfBirthSelected(946684800000L)).test {
            assertEquals(OnboardingScreenResult.DateOfBirthUpdated(946684800000L), awaitItem())
            awaitComplete()
        }
        interactor.actionToResult(OnboardingScreenAction.CountrySelected("Romania")).test {
            assertEquals(OnboardingScreenResult.CountryUpdated("Romania"), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `CompleteOnboardingClick emits Error when currentUser is null`() = runTest {
        every { authRepository.currentUser } returns null

        interactor.actionToResult(
            OnboardingScreenAction.CompleteOnboardingClick("alex", "", "", 1000L, "Romania")
        ).test {
            assertEquals(
                OnboardingScreenResult.Error("User session expired. Please sign in again."),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun `CompleteOnboardingClick maps all OnboardingResponse states`() = runTest {
        val mockUser: FirebaseUser = mockk {
            every { uid } returns "uid-1"
            every { email } returns "user@logeon.com"
        }
        every { authRepository.currentUser } returns mockUser
        every {
            userRepository.completeOnboarding(
                uid = "uid-1",
                email = "user@logeon.com",
                username = "alex",
                displayName = "Alex",
                bio = "Bio",
                dateOfBirthMillis = 1000L,
                country = "Romania"
            )
        } returns flowOf(
            OnboardingResponse.Loading,
            OnboardingResponse.UsernameError(UsernameValidationResult.ALREADY_TAKEN),
            OnboardingResponse.DateOfBirthRequired,
            OnboardingResponse.CountryRequired,
            OnboardingResponse.Error("Write failed"),
            OnboardingResponse.Success
        )

        interactor.actionToResult(
            OnboardingScreenAction.CompleteOnboardingClick("alex", "Alex", "Bio", 1000L, "Romania")
        ).test {
            assertEquals(OnboardingScreenResult.Loading, awaitItem())
            assertEquals(
                OnboardingScreenResult.UsernameError(UsernameValidationResult.ALREADY_TAKEN),
                awaitItem()
            )
            assertEquals(OnboardingScreenResult.DateOfBirthError, awaitItem())
            assertEquals(OnboardingScreenResult.CountryError, awaitItem())
            assertEquals(OnboardingScreenResult.Error("Write failed"), awaitItem())
            assertEquals(OnboardingScreenResult.Success, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `SignOutClick calls authRepository signOut and emits SignedOut`() = runTest {
        interactor.actionToResult(OnboardingScreenAction.SignOutClick).test {
            assertEquals(OnboardingScreenResult.SignedOut, awaitItem())
            awaitComplete()
        }

        verify(exactly = 1) { authRepository.signOut() }
    }
}