package com.sapphyx.logeon.test.viewModelImpl.profile

import com.sapphyx.logeon.util.MainDispatcherRule
import com.sapphyx.logeon.viewModelImpl.profile.ProfileScreenInteractor
import com.sapphyx.logeon.viewModelImpl.profile.ProfileScreenResult
import com.sapphyx.logeon.viewModelImpl.profile.ProfileScreenViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ProfileScreenViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initResults populates userEmail in uiState`() = runTest {
        val interactor: ProfileScreenInteractor = mockk {
            every { initResults() } returns flowOf(
                ProfileScreenResult.UserLoaded("profile@logeon.com")
            )
        }

        val viewModel = ProfileScreenViewModel(interactor)

        assertEquals("profile@logeon.com", viewModel.uiState.value.userEmail)
    }
}
