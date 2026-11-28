package com.sapphyx.logeon.data.repository

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.sapphyx.logeon.R
import com.sapphyx.logeon.data.repository.util.authentication.EmailValidationResult
import com.sapphyx.logeon.data.repository.util.authentication.EmailValidator
import com.sapphyx.logeon.data.repository.util.authentication.PasswordValidationResult
import com.sapphyx.logeon.data.repository.util.authentication.PasswordValidator
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class AuthenticationRepository @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val externalScope: CoroutineScope,
    private val auth: FirebaseAuth,
    private val emailValidator: EmailValidator,
    private val passwordValidator: PasswordValidator
) {

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    private val _manualRefreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    fun observeAuthState(): Flow<AuthStatus> {
        val firebaseAuthFlow = callbackFlow {
            val listener = FirebaseAuth.AuthStateListener {
                trySend(getCurrentAuthStatus())
            }
            auth.addAuthStateListener(listener)
            awaitClose { auth.removeAuthStateListener(listener) }
        }

        val manualRefreshFlow = _manualRefreshTrigger.map {
            getCurrentAuthStatus()
        }

        return merge(firebaseAuthFlow, manualRefreshFlow)
            .distinctUntilChanged()
    }

    fun loginWithEmailAndPassword(
        email: String, password: String
    ): Flow<AuthResponse> = executeAuthAction(
        email = email,
        password = password,
        defaultError = LOGIN_DEFAULT_ERROR,
        validatePassword = passwordValidator::validateForLogin
    ) { trimmedEmail ->
        auth.signInWithEmailAndPassword(trimmedEmail, password).await()
    }

    fun createAccountWithEmailAndPassword(
        email: String, password: String
    ): Flow<AuthResponse> = executeAuthAction(
        email = email,
        password = password,
        defaultError = CREATE_ACCOUNT_DEFAULT_ERROR,
        validatePassword = passwordValidator::invoke
    ) { trimmedEmail ->
        val result = auth.createUserWithEmailAndPassword(trimmedEmail, password).await()
        //Sending verification email after creating account with email and password
        result.user?.sendEmailVerification()?.await()
    }

    fun signInWithGoogle(context: Context): Flow<AuthResponse> = flow {
        val activity = context.findActivity()
        if (activity == null) {
            emit(AuthResponse.Error(GOOGLE_LOGIN_CONTEXT_ERROR))
            return@flow
        }

        emit(AuthResponse.Loading)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(activity.getString(R.string.default_web_client_id))
            .setAutoSelectEnabled(false)
            .setNonce(createNonce())
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val response = try {
            val credentialManager = CredentialManager.create(activity)
            val result = credentialManager.getCredential(
                context = activity,
                request = request
            )

            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential
                    .createFrom(credential.data)

                val firebaseCredential = GoogleAuthProvider
                    .getCredential(
                        googleIdTokenCredential.idToken,
                        null
                    )

                auth.signInWithCredential(firebaseCredential).await()
                AuthResponse.Success
            } else {
                AuthResponse.Error(GOOGLE_LOGIN_DEFAULT_ERROR)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: GetCredentialCancellationException) {
            Log.e(LOG_TAG, LOG_CREDENTIAL_CANCELLATION_EXCEPTION_MESSAGE, e)
            // User dismissed the bottom sheet
            AuthResponse.Cancelled
        } catch (e: GoogleIdTokenParsingException) {
            AuthResponse.Error(e.message ?: GOOGLE_LOGIN_DEFAULT_ERROR)
        } catch (e: Exception) {
            AuthResponse.Error(e.message ?: GOOGLE_LOGIN_DEFAULT_ERROR)
        }

        emit(response)
    }

    suspend fun resendVerificationEmail(): Result<Unit> = try {
        currentUser?.sendEmailVerification()?.await()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun checkEmailVerified(): Boolean = try {
        currentUser?.reload()?.await()
        val isVerified = currentUser?.isEmailVerified == true
        if (isVerified) {
            _manualRefreshTrigger.tryEmit(Unit)
        }
        isVerified
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e(LOG_TAG, LOG_CHECK_EMAIL_VERIFIED_EXCEPTION_MESSAGE, e)
        false
    }

    fun sendPasswordResetEmail(email: String): Flow<AuthResponse> = flow {
        val trimmedEmail = email.trim()
        val emailResult = emailValidator(trimmedEmail)
        if (emailResult != EmailValidationResult.CORRECT) {
            emit(AuthResponse.EmailValidationError(emailResult))
            return@flow
        }

        emit(AuthResponse.Loading)

        val response = try {
            auth.sendPasswordResetEmail(trimmedEmail).await()
            AuthResponse.Success
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(LOG_TAG, LOG_PASSWORD_RESET_EXCEPTION_MESSAGE, e)
            AuthResponse.Error(e.message ?: PASSWORD_RESET_DEFAULT_ERROR)
        }
        emit(response)
    }

    fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmNewPassword: String
    ): Flow<AccountSecurityResponse> = flow {
        if (currentPassword.isEmpty()) {
            emit(AccountSecurityResponse.CurrentPasswordEmpty)
            return@flow
        }

        val passwordResult = passwordValidator(newPassword)
        if (passwordResult != PasswordValidationResult.CORRECT) {
            emit(AccountSecurityResponse.NewPasswordError(passwordResult))
            return@flow
        }

        if (newPassword != confirmNewPassword) {
            emit(AccountSecurityResponse.PasswordsDoNotMatch)
            return@flow
        }

        val user = currentUser
        val email = user?.email
        if (user == null || email.isNullOrEmpty()) {
            emit(AccountSecurityResponse.Error(DEFAULT_AUTH_SESSION_ERROR))
            return@flow
        }

        emit(AccountSecurityResponse.Loading)

        val response = try {
            val credential = EmailAuthProvider.getCredential(email, currentPassword)
            user.reauthenticate(credential).await()
            user.updatePassword(newPassword).await()
            AccountSecurityResponse.Success
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(LOG_TAG, LOG_CHANGE_PASSWORD_ERROR, e)
            AccountSecurityResponse.Error(e.message ?: DEFAULT_CHANGE_PASSWORD_ERROR)
        }

        emit(response)
    }

    fun deleteAccount(
        currentPassword: String,
        onBeforeAuthDelete: suspend (uid: String) -> Unit
    ): Flow<AccountSecurityResponse> = flow {
        if (currentPassword.isEmpty()) {
            emit(AccountSecurityResponse.CurrentPasswordEmpty)
            return@flow
        }

        val user = currentUser
        val email = user?.email
        if (user == null || email.isNullOrEmpty()) {
            emit(AccountSecurityResponse.Error(DEFAULT_AUTH_SESSION_ERROR))
            return@flow
        }

        emit(AccountSecurityResponse.Loading)

        val response = try {
            val credential = EmailAuthProvider.getCredential(email, currentPassword)
            user.reauthenticate(credential).await()
            onBeforeAuthDelete(user.uid)
            user.delete().await()
            AccountSecurityResponse.Success
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(LOG_TAG, LOG_DELETE_ACCOUNT_ERROR, e)
            AccountSecurityResponse.Error(e.message ?: DEFAULT_DELETE_ACCOUNT_ERROR)
        }

        emit(response)
    }

    fun signOut() {
        auth.signOut()

        externalScope.launch {
            try {
                val credentialManager = CredentialManager.create(appContext)
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(LOG_TAG, LOG_SIGN_OUT_EXCEPTION_MESSAGE, e)
            }
        }
    }

    private fun getCurrentAuthStatus(): AuthStatus = when (currentUser?.isEmailVerified) {
        null -> AuthStatus.LoggedOut
        false -> AuthStatus.UnverifiedEmail
        true -> AuthStatus.Verified
    }

    private fun executeAuthAction(
        email: String,
        password: String,
        defaultError: String,
        validatePassword: (String) -> PasswordValidationResult,
        authCall: suspend (trimmedEmail: String) -> Unit
    ): Flow<AuthResponse> = flow {
        val trimmedEmail = email.trim()
        val emailResult = emailValidator(trimmedEmail)
        if (emailResult != EmailValidationResult.CORRECT) {
            emit(AuthResponse.EmailValidationError(emailResult))
            return@flow
        }

        val passwordResult = validatePassword(password)
        if (passwordResult != PasswordValidationResult.CORRECT) {
            emit(AuthResponse.PasswordValidationError(passwordResult))
            return@flow
        }

        emit(AuthResponse.Loading)

        val response = try {
            authCall(trimmedEmail)
            AuthResponse.Success
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AuthResponse.Error(e.message ?: defaultError)
        }
        emit(response)
    }

    private fun createNonce(): String {
        val rawNonce = UUID.randomUUID()
        val bytes = rawNonce.toString().toByteArray()
        val md = MessageDigest.getInstance(ENCRYPTION_ALGORITHM)
        val digest = md.digest(bytes)

        return digest.fold(INITIAL_FOLD) { str, it ->
            str + DIGEST_FORMAT.format(it)
        }
    }

    private tailrec fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

    private companion object {
        const val CREATE_ACCOUNT_DEFAULT_ERROR = "Create account with email - default error"
        const val LOGIN_DEFAULT_ERROR = "Sign in with email - default error"
        const val GOOGLE_LOGIN_DEFAULT_ERROR = "Sign in with google account - default error"
        const val GOOGLE_LOGIN_CONTEXT_ERROR = "Sign in with google account - context error"
        const val ENCRYPTION_ALGORITHM = "SHA-256"
        const val INITIAL_FOLD = ""
        const val DIGEST_FORMAT = "%02x"
        const val LOG_TAG = "AuthenticationRepository"
        const val LOG_SIGN_OUT_EXCEPTION_MESSAGE = "Exception at signOut method in AuthRepository()"
        const val LOG_CHECK_EMAIL_VERIFIED_EXCEPTION_MESSAGE = "Check email verified exception"
        const val LOG_CREDENTIAL_CANCELLATION_EXCEPTION_MESSAGE =
            "Credential cancellation exception"

        const val PASSWORD_RESET_DEFAULT_ERROR = "Send password reset email - default error"
        const val LOG_PASSWORD_RESET_EXCEPTION_MESSAGE = "Send password reset email exception"
        const val DEFAULT_AUTH_SESSION_ERROR = "User session expired. Please sign in again."
        const val DEFAULT_CHANGE_PASSWORD_ERROR = "Failed to change password"
        const val DEFAULT_DELETE_ACCOUNT_ERROR = "Failed to delete account"
        const val LOG_CHANGE_PASSWORD_ERROR = "Error changing password"
        const val LOG_DELETE_ACCOUNT_ERROR = "Error deleting account"
    }
}

sealed interface AuthStatus {
    data object LoggedOut : AuthStatus
    data object UnverifiedEmail : AuthStatus
    data object Verified : AuthStatus
}

sealed interface AuthResponse {
    data object Loading : AuthResponse
    data object Success : AuthResponse
    data object Cancelled : AuthResponse
    data class Error(val message: String) : AuthResponse
    data class EmailValidationError(val validationError: EmailValidationResult) : AuthResponse
    data class PasswordValidationError(val validationError: PasswordValidationResult) : AuthResponse
}

sealed interface AccountSecurityResponse {
    data object Loading : AccountSecurityResponse
    data object Success : AccountSecurityResponse
    data object CurrentPasswordEmpty : AccountSecurityResponse
    data class NewPasswordError(val error: PasswordValidationResult) : AccountSecurityResponse
    data object PasswordsDoNotMatch : AccountSecurityResponse
    data class Error(val message: String) : AccountSecurityResponse
}