package com.sapphyx.logeon.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.sapphyx.logeon.data.repository.util.authentication.EmailValidationResult
import com.sapphyx.logeon.data.repository.util.authentication.EmailValidator
import com.sapphyx.logeon.data.repository.util.authentication.PasswordValidationResult
import com.sapphyx.logeon.data.repository.util.authentication.PasswordValidator
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class AuthenticationRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val emailValidator: EmailValidator,
    private val passwordValidator: PasswordValidator
) {

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun isUserLoggedIn(): Boolean = currentUser != null

    fun observeAuthState(): Flow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser != null)
        }

        auth.addAuthStateListener(listener)

        awaitClose {
            auth.removeAuthStateListener(listener)
        }
    }.distinctUntilChanged()

    fun createAccountWithEmailAndPassword(
        email: String,
        password: String
    ): Flow<AuthResponse> = executeAuthAction(
        email = email,
        password = password,
        defaultError = CREATE_ACCOUNT_DEFAULT_ERROR
    ) {
        auth.createUserWithEmailAndPassword(email, password).await()
    }

    fun loginWithEmailAndPassword(
        email: String,
        password: String
    ): Flow<AuthResponse> = executeAuthAction(
        email = email,
        password = password,
        defaultError = LOGIN_DEFAULT_ERROR
    ) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    private fun executeAuthAction(
        email: String,
        password: String,
        defaultError: String,
        authCall: suspend () -> Unit
    ): Flow<AuthResponse> = flow {
        val emailResult = emailValidator(email)
        if (emailResult != EmailValidationResult.CORRECT) {
            emit(AuthResponse.EmailValidationError(emailResult))
            return@flow
        }

        val passwordResult = passwordValidator(password)
        if (passwordResult != PasswordValidationResult.CORRECT) {
            emit(AuthResponse.PasswordValidationError(passwordResult))
            return@flow
        }

        emit(AuthResponse.Loading)

        val response = try {
            authCall()
            AuthResponse.Success
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AuthResponse.Error(e.message ?: defaultError)
        }
        emit(response)
    }

    fun signOut() {
        auth.signOut()
    }

    private companion object {
        const val CREATE_ACCOUNT_DEFAULT_ERROR = "Create account with email - default error"
        const val LOGIN_DEFAULT_ERROR = "Sign in with email - default error"
    }
}

sealed interface AuthResponse {
    data object Loading : AuthResponse
    data object Success : AuthResponse
    data class Error(val message: String) : AuthResponse
    data class EmailValidationError(val validationError: EmailValidationResult) : AuthResponse
    data class PasswordValidationError(val validationError: PasswordValidationResult) : AuthResponse
}