package com.sapphyx.logeon.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.sapphyx.logeon.data.model.UserProfile
import com.sapphyx.logeon.data.repository.util.user.UsernameValidationResult
import com.sapphyx.logeon.data.repository.util.user.UsernameValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val usernameValidator: UsernameValidator
) {

    fun observeUserProfile(uid: String): Flow<UserProfileState> = callbackFlow {
        trySend(UserProfileState.Loading)

        val registration = firestore.collection(USERS_COLLECTION)
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(LOG_TAG, LOG_OBSERVE_PROFILE_ERROR, error)
                    trySend(UserProfileState.Error(error.message ?: DEFAULT_PROFILE_ERROR))
                    return@addSnapshotListener
                }

                val profile = snapshot?.toObject(UserProfile::class.java)
                if (profile != null && profile.onboardingCompleted) {
                    trySend(UserProfileState.Success(profile))
                } else {
                    trySend(UserProfileState.NotFound)
                }
            }

        awaitClose { registration.remove() }
    }

    suspend fun checkUsernameAvailable(username: String): UsernameValidationResult {
        val trimmed = username.trim()
        val formatResult = usernameValidator(trimmed)
        if (formatResult != UsernameValidationResult.CORRECT) {
            return formatResult
        }

        return try {
            val doc = firestore.collection(USERNAMES_COLLECTION)
                .document(trimmed.lowercase())
                .get()
                .await()
            if (doc.exists()) {
                UsernameValidationResult.ALREADY_TAKEN
            } else {
                UsernameValidationResult.CORRECT
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(LOG_TAG, LOG_CHECK_USERNAME_ERROR, e)
            UsernameValidationResult.CORRECT
        }
    }

    fun completeOnboarding(
        uid: String,
        email: String,
        username: String,
        displayName: String?,
        bio: String?,
        dateOfBirthMillis: Long?,
        country: String
    ): Flow<OnboardingResponse> = flow {
        val trimmedUsername = username.trim()
        val trimmedCountry = country.trim()

        val usernameResult = usernameValidator(trimmedUsername)
        if (usernameResult != UsernameValidationResult.CORRECT) {
            emit(OnboardingResponse.UsernameError(usernameResult))
            return@flow
        }

        if (dateOfBirthMillis == null || dateOfBirthMillis <= 0L) {
            emit(OnboardingResponse.DateOfBirthRequired)
            return@flow
        }

        if (trimmedCountry.isEmpty()) {
            emit(OnboardingResponse.CountryRequired)
            return@flow
        }

        emit(OnboardingResponse.Loading)

        val normalizedUsername = trimmedUsername.lowercase()
        val usernameDocRef = firestore.collection(USERNAMES_COLLECTION).document(normalizedUsername)
        val userDocRef = firestore.collection(USERS_COLLECTION).document(uid)

        val response = try {
            val userProfile = UserProfile(
                uid = uid,
                email = email,
                username = trimmedUsername,
                displayName = displayName?.trim()?.takeIf { it.isNotEmpty() },
                bio = bio?.trim()?.takeIf { it.isNotEmpty() },
                dateOfBirthMillis = dateOfBirthMillis,
                country = trimmedCountry,
                onboardingCompleted = true
            )

            firestore.runTransaction { transaction ->
                val existingUsername = transaction.get(usernameDocRef)
                if (existingUsername.exists()) {
                    throw UsernameTakenException()
                }
                transaction.set(usernameDocRef, mapOf(FIELD_UID to uid))
                transaction.set(userDocRef, userProfile)
            }.await()

            OnboardingResponse.Success
        } catch (e: CancellationException) {
            throw e
        } catch (_: UsernameTakenException) {
            OnboardingResponse.UsernameError(UsernameValidationResult.ALREADY_TAKEN)
        } catch (e: Exception) {
            Log.e(LOG_TAG, LOG_COMPLETE_ONBOARDING_ERROR, e)
            OnboardingResponse.Error(e.message ?: DEFAULT_ONBOARDING_ERROR)
        }

        emit(response)
    }

    fun updateUserProfile(
        uid: String,
        oldUsername: String,
        newUsername: String,
        displayName: String?,
        bio: String?,
        country: String
    ): Flow<UpdateProfileResponse> = flow {
        val trimmedNewUsername = newUsername.trim()
        val trimmedOldUsername = oldUsername.trim()
        val trimmedCountry = country.trim()

        val usernameResult = usernameValidator(trimmedNewUsername)
        if (usernameResult != UsernameValidationResult.CORRECT) {
            emit(UpdateProfileResponse.UsernameError(usernameResult))
            return@flow
        }

        if (trimmedCountry.isEmpty()) {
            emit(UpdateProfileResponse.CountryRequired)
            return@flow
        }

        emit(UpdateProfileResponse.Loading)

        val normalizedNew = trimmedNewUsername.lowercase()
        val normalizedOld = trimmedOldUsername.lowercase()
        val isUsernameChanged = normalizedNew != normalizedOld

        val newUsernameDocRef = firestore.collection(USERNAMES_COLLECTION).document(normalizedNew)
        val oldUsernameDocRef = firestore.collection(USERNAMES_COLLECTION).document(normalizedOld)
        val userDocRef = firestore.collection(USERS_COLLECTION).document(uid)

        val sanitizedDisplayName = displayName?.trim()?.takeIf { it.isNotEmpty() }
        val sanitizedBio = bio?.trim()?.takeIf { it.isNotEmpty() }

        val response = try {
            firestore.runTransaction { transaction ->
                if (isUsernameChanged) {
                    val existingUsername = transaction.get(newUsernameDocRef)
                    if (existingUsername.exists()) {
                        throw UsernameTakenException()
                    }
                    transaction.set(newUsernameDocRef, mapOf(FIELD_UID to uid))
                    if (normalizedOld.isNotEmpty()) {
                        transaction.delete(oldUsernameDocRef)
                    }
                }

                transaction.update(
                    userDocRef,
                    mapOf(
                        FIELD_USERNAME to trimmedNewUsername,
                        FIELD_DISPLAY_NAME to sanitizedDisplayName,
                        FIELD_BIO to sanitizedBio,
                        FIELD_COUNTRY to trimmedCountry
                    )
                )
            }.await()

            UpdateProfileResponse.Success
        } catch (e: CancellationException) {
            throw e
        } catch (_: UsernameTakenException) {
            UpdateProfileResponse.UsernameError(UsernameValidationResult.ALREADY_TAKEN)
        } catch (e: Exception) {
            Log.e(LOG_TAG, LOG_UPDATE_PROFILE_ERROR, e)
            UpdateProfileResponse.Error(e.message ?: DEFAULT_UPDATE_PROFILE_ERROR)
        }

        emit(response)
    }

    suspend fun deleteUserData(uid: String, username: String) {
        try {
            val normalizedUsername = username.trim().lowercase()
            val userDocRef = firestore.collection(USERS_COLLECTION).document(uid)
            val usernameDocRef = if (normalizedUsername.isNotEmpty()) {
                firestore.collection(USERNAMES_COLLECTION).document(normalizedUsername)
            } else {
                null
            }

            firestore.runTransaction { transaction ->
                if (usernameDocRef != null) {
                    transaction.delete(usernameDocRef)
                }
                transaction.delete(userDocRef)
            }.await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(LOG_TAG, LOG_DELETE_USER_DATA_ERROR, e)
            throw e
        }
    }

    private class UsernameTakenException : Exception()

    private companion object {
        const val LOG_TAG = "UserRepository"
        const val USERS_COLLECTION = "users"
        const val USERNAMES_COLLECTION = "usernames"
        const val FIELD_UID = "uid"
        const val DEFAULT_PROFILE_ERROR = "Failed to load user profile"
        const val DEFAULT_ONBOARDING_ERROR = "Failed to complete onboarding"
        const val LOG_OBSERVE_PROFILE_ERROR = "Error observing user profile"
        const val LOG_CHECK_USERNAME_ERROR = "Error checking username availability"
        const val LOG_COMPLETE_ONBOARDING_ERROR = "Error completing onboarding"
        const val FIELD_USERNAME = "username"
        const val FIELD_DISPLAY_NAME = "displayName"
        const val FIELD_BIO = "bio"
        const val FIELD_COUNTRY = "country"
        const val DEFAULT_UPDATE_PROFILE_ERROR = "Failed to update profile"
        const val LOG_UPDATE_PROFILE_ERROR = "Error updating user profile"
        const val LOG_DELETE_USER_DATA_ERROR = "Error deleting user data"
    }
}

sealed interface UserProfileState {
    data object Loading : UserProfileState
    data object NotFound : UserProfileState
    data class Success(val profile: UserProfile) : UserProfileState
    data class Error(val message: String) : UserProfileState
}

sealed interface OnboardingResponse {
    data object Loading : OnboardingResponse
    data object Success : OnboardingResponse
    data class UsernameError(val error: UsernameValidationResult) : OnboardingResponse
    data object DateOfBirthRequired : OnboardingResponse
    data object CountryRequired : OnboardingResponse
    data class Error(val message: String) : OnboardingResponse
}

sealed interface UpdateProfileResponse {
    data object Loading : UpdateProfileResponse
    data object Success : UpdateProfileResponse
    data class UsernameError(val error: UsernameValidationResult) : UpdateProfileResponse
    data object CountryRequired : UpdateProfileResponse
    data class Error(val message: String) : UpdateProfileResponse
}