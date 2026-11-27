package com.sapphyx.logeon.viewModelImpl.onboarding

import com.sapphyx.logeon.data.repository.AuthenticationRepository
import com.sapphyx.logeon.data.repository.OnboardingResponse
import com.sapphyx.logeon.data.repository.UserRepository
import com.sapphyx.logeon.viewModel.BaseInteractor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

class OnboardingScreenInteractor @Inject constructor(
    private val authRepository: AuthenticationRepository,
    private val userRepository: UserRepository
) : BaseInteractor<OnboardingScreenAction, OnboardingScreenResult> {

    override fun initResults(): Flow<OnboardingScreenResult> = emptyFlow()

    override fun actionToResult(action: OnboardingScreenAction): Flow<OnboardingScreenResult> =
        when (action) {
            is OnboardingScreenAction.UsernameChanged -> flow {
                val normalized = action.username.lowercase()
                emit(OnboardingScreenResult.UsernameUpdated(normalized))
                if (normalized.trim().isNotEmpty()) {
                    delay(USERNAME_CHECK_DEBOUNCE_MS.milliseconds)
                    val availability = userRepository.checkUsernameAvailable(normalized)
                    emit(OnboardingScreenResult.UsernameChecked(normalized, availability))
                }
            }

            is OnboardingScreenAction.DisplayNameChanged ->
                flowOf(OnboardingScreenResult.DisplayNameUpdated(action.displayName))

            is OnboardingScreenAction.BioChanged ->
                flowOf(OnboardingScreenResult.BioUpdated(action.bio))

            is OnboardingScreenAction.DateOfBirthSelected ->
                flowOf(OnboardingScreenResult.DateOfBirthUpdated(action.millis))

            is OnboardingScreenAction.CountrySelected ->
                flowOf(OnboardingScreenResult.CountryUpdated(action.country))

            is OnboardingScreenAction.CompleteOnboardingClick -> {
                val currentUser = authRepository.currentUser
                if (currentUser == null) {
                    flowOf(OnboardingScreenResult.Error("User session expired. Please sign in again."))
                } else {
                    userRepository.completeOnboarding(
                        uid = currentUser.uid,
                        email = currentUser.email.orEmpty(),
                        username = action.username,
                        displayName = action.displayName,
                        bio = action.bio,
                        dateOfBirthMillis = action.dateOfBirthMillis,
                        country = action.country
                    ).mapToOnboardingResult()
                }
            }

            is OnboardingScreenAction.SignOutClick -> {
                authRepository.signOut()
                flowOf(OnboardingScreenResult.SignedOut)
            }
        }

    private fun Flow<OnboardingResponse>.mapToOnboardingResult(): Flow<OnboardingScreenResult> =
        map { response ->
            when (response) {
                is OnboardingResponse.Loading -> OnboardingScreenResult.Loading
                is OnboardingResponse.Success -> OnboardingScreenResult.Success
                is OnboardingResponse.UsernameError ->
                    OnboardingScreenResult.UsernameError(response.error)

                is OnboardingResponse.DateOfBirthRequired ->
                    OnboardingScreenResult.DateOfBirthError

                is OnboardingResponse.CountryRequired ->
                    OnboardingScreenResult.CountryError

                is OnboardingResponse.Error ->
                    OnboardingScreenResult.Error(response.message)
            }
        }

    private companion object {
        const val USERNAME_CHECK_DEBOUNCE_MS = 400L
    }
}