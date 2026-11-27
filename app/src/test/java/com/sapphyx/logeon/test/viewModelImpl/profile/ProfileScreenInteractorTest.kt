package com.sapphyx.logeon.test.viewModelImpl.profile

import app.cash.turbine.test
import com.google.firebase.auth.FirebaseUser
import com.sapphyx.logeon.data.repository.AuthenticationRepository
import com.sapphyx.logeon.viewModelImpl.profile.ProfileScreenAction
import com.sapphyx.logeon.viewModelImpl.profile.ProfileScreenInteractor
import com.sapphyx.logeon.viewModelImpl.profile.ProfileScreenResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileScreenInteractorTest {

    private val authRepository: AuthenticationRepository = mockk(relaxed = true)
    private val interactor = ProfileScreenInteractor(authRepository)

    @Test
    fun `initResults loads current user email`() = runTest {
        val mockUser: FirebaseUser = mockk {
            every { email } returns "profile@logeon.com"
        }
        every { authRepository.currentUser } returns mockUser

        interactor.initResults().test {
            assertEquals(ProfileScreenResult.UserLoaded("profile@logeon.com"), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `SignOutClick calls repository signOut and emits SignedOut`() = runTest {
        interactor.actionToResult(ProfileScreenAction.SignOutClick).test {
            assertEquals(ProfileScreenResult.SignedOut, awaitItem())
            awaitComplete()
        }

        verify(exactly = 1) { authRepository.signOut() }
    }
}