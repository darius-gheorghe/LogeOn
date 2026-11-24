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
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class AuthenticationRepository @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val externalScope: CoroutineScope,
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
        email: String, password: String
    ): Flow<AuthResponse> = executeAuthAction(
        email = email, password = password, defaultError = CREATE_ACCOUNT_DEFAULT_ERROR
    ) {
        auth.createUserWithEmailAndPassword(email, password).await()
    }

    fun loginWithEmailAndPassword(
        email: String, password: String
    ): Flow<AuthResponse> = executeAuthAction(
        email = email, password = password, defaultError = LOGIN_DEFAULT_ERROR
    ) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    private fun executeAuthAction(
        email: String, password: String, defaultError: String, authCall: suspend () -> Unit
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

    private fun createNonce(): String {
        val rawNonce = UUID.randomUUID()
        val bytes = rawNonce.toString().toByteArray()
        val md = MessageDigest.getInstance(ENCRYPTION_ALGORITHM)
        val digest = md.digest(bytes)

        return digest.fold(INITIAL_FOLD) { str, it ->
            str + DIGEST_FORMAT.format(it)
        }
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
            // User dismissed the bottom sheet
            AuthResponse.Cancelled
        } catch (e: GoogleIdTokenParsingException) {
            AuthResponse.Error(e.message ?: GOOGLE_LOGIN_DEFAULT_ERROR)
        } catch (e: Exception) {
            AuthResponse.Error(e.message ?: GOOGLE_LOGIN_DEFAULT_ERROR)
        }

        emit(response)
    }

    private tailrec fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
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
                Log.e(LOG_SIGN_OUT_EXCEPTION_TAG, LOG_SIGN_OUT_EXCEPTION_MESSAGE, e)
            }
        }
    }

    private companion object {
        const val CREATE_ACCOUNT_DEFAULT_ERROR = "Create account with email - default error"
        const val LOGIN_DEFAULT_ERROR = "Sign in with email - default error"
        const val GOOGLE_LOGIN_DEFAULT_ERROR = "Sign in with google account - default error"
        const val GOOGLE_LOGIN_CONTEXT_ERROR = "Sign in with google account - context error"
        const val ENCRYPTION_ALGORITHM = "SHA-256"
        const val INITIAL_FOLD = ""
        const val DIGEST_FORMAT = "%02x"
        const val LOG_SIGN_OUT_EXCEPTION_TAG = "Sign out Exception"
        const val LOG_SIGN_OUT_EXCEPTION_MESSAGE = "Exception at signOut method in AuthRepository()"
    }
}

sealed interface AuthResponse {
    data object Loading : AuthResponse
    data object Success : AuthResponse
    data object Cancelled : AuthResponse
    data class Error(val message: String) : AuthResponse
    data class EmailValidationError(val validationError: EmailValidationResult) : AuthResponse
    data class PasswordValidationError(val validationError: PasswordValidationResult) : AuthResponse
}