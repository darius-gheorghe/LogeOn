package com.sapphyx.logeon.test.viewModelImpl.onboarding

import app.cash.turbine.test
import com.sapphyx.logeon.data.repository.util.user.UsernameValidationResult
import com.sapphyx.logeon.util.MainDispatcherRule
import com.sapphyx.logeon.viewModelImpl.onboarding.OnboardingScreenAction
import com.sapphyx.logeon.viewModelImpl.onboarding.OnboardingScreenInteractor
import com.sapphyx.logeon.viewModelImpl.onboarding.OnboardingScreenResult
import com.sapphyx.logeon.viewModelImpl.onboarding.OnboardingScreenViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class OnboardingScreenViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val interactor: OnboardingScreenInteractor = mockk {
        every { initResults() } returns emptyFlow()
    }

    @Test
    fun `UsernameChecked updates availability only when username still matches current input`() =
        runTest {
            every {
                interactor.actionToResult(OnboardingScreenAction.UsernameChanged("alex"))
            } returns flowOf(
                OnboardingScreenResult.UsernameUpdated("alex"),
                OnboardingScreenResult.UsernameChecked("stale_name", UsernameValidationResult.CORRECT),
                OnboardingScreenResult.UsernameChecked("alex", UsernameValidationResult.CORRECT)
            )

            val viewModel = OnboardingScreenViewModel(interactor)

            viewModel.uiState.test {
                awaitItem() // Initial

                viewModel.processUiAction(OnboardingScreenAction.UsernameChanged("alex"))

                val updated = awaitItem()
                assertEquals("alex", updated.username)
                assertFalse(updated.isUsernameAvailable)

                // Stale check is ignored (no new emission), valid check emits available = true
                val available = awaitItem()
                assertTrue(available.isUsernameAvailable)
                assertNull(available.usernameError)
            }
        }

    @Test
    fun `form field updates store values and clear previous validation errors`() = runTest {
        every {
            interactor.actionToResult(OnboardingScreenAction.CompleteOnboardingClick("", "", "", null, ""))
        } returns flowOf(
            OnboardingScreenResult.DateOfBirthError,
            OnboardingScreenResult.CountryError
        )
        every {
            interactor.actionToResult(OnboardingScreenAction.DateOfBirthSelected(946684800000L))
        } returns flowOf(OnboardingScreenResult.DateOfBirthUpdated(946684800000L))
        every {
            interactor.actionToResult(OnboardingScreenAction.CountrySelected("Romania"))
        } returns flowOf(OnboardingScreenResult.CountryUpdated("Romania"))
        every {
            interactor.actionToResult(OnboardingScreenAction.DisplayNameChanged("Alex"))
        } returns flowOf(OnboardingScreenResult.DisplayNameUpdated("Alex"))
        every {
            interactor.actionToResult(OnboardingScreenAction.BioChanged("Developer"))
        } returns flowOf(OnboardingScreenResult.BioUpdated("Developer"))

        val viewModel = OnboardingScreenViewModel(interactor)

        viewModel.uiState.test {
            awaitItem()

            viewModel.processUiAction(
                OnboardingScreenAction.CompleteOnboardingClick("", "", "", null, "")
            )
            assertTrue(awaitItem().dateOfBirthError)
            assertTrue(awaitItem().countryError)

            viewModel.processUiAction(OnboardingScreenAction.DateOfBirthSelected(946684800000L))
            val dobCleared = awaitItem()
            assertEquals(946684800000L, dobCleared.dateOfBirthMillis)
            assertFalse(dobCleared.dateOfBirthError)

            viewModel.processUiAction(OnboardingScreenAction.CountrySelected("Romania"))
            val countryCleared = awaitItem()
            assertEquals("Romania", countryCleared.country)
            assertFalse(countryCleared.countryError)

            viewModel.processUiAction(OnboardingScreenAction.DisplayNameChanged("Alex"))
            assertEquals("Alex", awaitItem().displayName)

            viewModel.processUiAction(OnboardingScreenAction.BioChanged("Developer"))
            assertEquals("Developer", awaitItem().bio)
        }
    }

    @Test
    fun `CompleteOnboardingClick handles Loading, Errors, and Success`() = runTest {
        val action = OnboardingScreenAction.CompleteOnboardingClick(
            username = "alex",
            displayName = "",
            bio = "",
            dateOfBirthMillis = 1000L,
            country = "Romania"
        )
        val submitResults = MutableSharedFlow<OnboardingScreenResult>()
        every { interactor.actionToResult(action) } returns submitResults

        val viewModel = OnboardingScreenViewModel(interactor)

        viewModel.uiState.test {
            assertFalse(awaitItem().isLoading) // Initial

            viewModel.processUiAction(action)

            submitResults.emit(OnboardingScreenResult.Loading)
            assertTrue(awaitItem().isLoading)

            submitResults.emit(
                OnboardingScreenResult.UsernameError(UsernameValidationResult.ALREADY_TAKEN)
            )
            val takenState = awaitItem()
            assertFalse(takenState.isLoading)
            assertEquals(UsernameValidationResult.ALREADY_TAKEN, takenState.usernameError)

            submitResults.emit(OnboardingScreenResult.Error("Network error"))
            val errorState = awaitItem()
            assertEquals("Network error", errorState.generalError)

            submitResults.emit(OnboardingScreenResult.Loading)
            assertTrue(awaitItem().isLoading)

            submitResults.emit(OnboardingScreenResult.Success)
            val successState = awaitItem()
            assertFalse(successState.isLoading)
        }
    }
}