package com.sapphyx.logeon.viewModelImpl.onboarding

import com.sapphyx.logeon.data.repository.util.user.UsernameValidationResult
import com.sapphyx.logeon.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingScreenViewModel @Inject constructor(
    interactor: OnboardingScreenInteractor
) : BaseViewModel<OnboardingScreenState, OnboardingScreenAction, OnboardingScreenEffect, OnboardingScreenResult>(
    initialUiState = OnboardingScreenState(),
    interactor = interactor
) {

    override suspend fun handleResult(
        previousState: OnboardingScreenState,
        result: OnboardingScreenResult
    ): OnboardingScreenState {
        return when (result) {
            is OnboardingScreenResult.UsernameUpdated -> previousState.copy(
                username = result.username,
                usernameError = null,
                isUsernameAvailable = false,
                generalError = null
            )

            is OnboardingScreenResult.UsernameChecked -> {
                if (previousState.username != result.checkedUsername) {
                    previousState
                } else if (result.validationResult == UsernameValidationResult.CORRECT) {
                    previousState.copy(
                        usernameError = null,
                        isUsernameAvailable = true
                    )
                } else {
                    previousState.copy(
                        usernameError = result.validationResult,
                        isUsernameAvailable = false
                    )
                }
            }

            is OnboardingScreenResult.DisplayNameUpdated -> previousState.copy(
                displayName = result.displayName,
                generalError = null
            )

            is OnboardingScreenResult.BioUpdated -> previousState.copy(
                bio = result.bio,
                generalError = null
            )

            is OnboardingScreenResult.DateOfBirthUpdated -> previousState.copy(
                dateOfBirthMillis = result.millis,
                dateOfBirthError = false,
                generalError = null
            )

            is OnboardingScreenResult.CountryUpdated -> previousState.copy(
                country = result.country,
                countryError = false,
                generalError = null
            )

            is OnboardingScreenResult.Loading -> previousState.copy(
                isLoading = true,
                generalError = null
            )

            is OnboardingScreenResult.Success -> previousState.copy(
                isLoading = false
            )

            is OnboardingScreenResult.UsernameError -> previousState.copy(
                isLoading = false,
                usernameError = result.error,
                isUsernameAvailable = false
            )

            is OnboardingScreenResult.DateOfBirthError -> previousState.copy(
                isLoading = false,
                dateOfBirthError = true
            )

            is OnboardingScreenResult.CountryError -> previousState.copy(
                isLoading = false,
                countryError = true
            )

            is OnboardingScreenResult.Error -> previousState.copy(
                isLoading = false,
                generalError = result.message
            )

            is OnboardingScreenResult.SignedOut -> previousState
        }
    }
}

data class OnboardingScreenState(
    val username: String = "",
    val displayName: String = "",
    val bio: String = "",
    val dateOfBirthMillis: Long? = null,
    val country: String = "",
    val isLoading: Boolean = false,
    val isUsernameAvailable: Boolean = false,
    val usernameError: UsernameValidationResult? = null,
    val dateOfBirthError: Boolean = false,
    val countryError: Boolean = false,
    val generalError: String? = null
)

sealed interface OnboardingScreenAction {
    data class UsernameChanged(val username: String) : OnboardingScreenAction
    data class DisplayNameChanged(val displayName: String) : OnboardingScreenAction
    data class BioChanged(val bio: String) : OnboardingScreenAction
    data class DateOfBirthSelected(val millis: Long) : OnboardingScreenAction
    data class CountrySelected(val country: String) : OnboardingScreenAction
    data class CompleteOnboardingClick(
        val username: String,
        val displayName: String,
        val bio: String,
        val dateOfBirthMillis: Long?,
        val country: String
    ) : OnboardingScreenAction

    data object SignOutClick : OnboardingScreenAction
}

sealed interface OnboardingScreenEffect

sealed interface OnboardingScreenResult {
    data class UsernameUpdated(val username: String) : OnboardingScreenResult
    data class UsernameChecked(
        val checkedUsername: String,
        val validationResult: UsernameValidationResult
    ) : OnboardingScreenResult

    data class DisplayNameUpdated(val displayName: String) : OnboardingScreenResult
    data class BioUpdated(val bio: String) : OnboardingScreenResult
    data class DateOfBirthUpdated(val millis: Long) : OnboardingScreenResult
    data class CountryUpdated(val country: String) : OnboardingScreenResult
    data object Loading : OnboardingScreenResult
    data object Success : OnboardingScreenResult
    data class UsernameError(val error: UsernameValidationResult) : OnboardingScreenResult
    data object DateOfBirthError : OnboardingScreenResult
    data object CountryError : OnboardingScreenResult
    data class Error(val message: String) : OnboardingScreenResult
    data object SignedOut : OnboardingScreenResult
}