package com.sapphyx.logeon.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.sapphyx.logeon.data.repository.AuthenticationRepository
import com.sapphyx.logeon.data.repository.util.authentication.EmailValidator
import com.sapphyx.logeon.data.repository.util.authentication.PasswordValidator
import com.sapphyx.logeon.navigation.NavigationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideNavigationManager(): NavigationManager = NavigationManager()

    @Provides
    @Singleton
    fun provideEmailValidator(): EmailValidator = EmailValidator()

    @Provides
    @Singleton
    fun providePasswordValidator(): PasswordValidator = PasswordValidator()

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Provides
    @Singleton
    fun provideAuthenticationRepository(
        @ApplicationContext appContext: Context,
        externalScope: CoroutineScope,
        firebaseAuth: FirebaseAuth,
        emailValidator: EmailValidator,
        passwordValidator: PasswordValidator
    ) = AuthenticationRepository(
        appContext = appContext,
        externalScope = externalScope,
        auth = firebaseAuth,
        emailValidator = emailValidator,
        passwordValidator = passwordValidator
    )
}