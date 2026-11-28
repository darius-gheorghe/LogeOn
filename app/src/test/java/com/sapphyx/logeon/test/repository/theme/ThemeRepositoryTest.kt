package com.sapphyx.logeon.test.repository.theme

import android.content.Context
import android.content.SharedPreferences
import com.sapphyx.logeon.data.repository.AppThemeMode
import com.sapphyx.logeon.data.repository.ThemeRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ThemeRepositoryTest {

    private val storage = mutableMapOf<String, String?>()
    private lateinit var context: Context

    @Before
    fun setUp() {
        storage.clear()

        val editor: SharedPreferences.Editor = mockk(relaxed = true) {
            every { putString(any(), any()) } answers {
                storage[firstArg()] = secondArg()
                this@mockk
            }
        }

        val prefs: SharedPreferences = mockk {
            every { getString(any(), any()) } answers {
                storage[firstArg()] ?: secondArg()
            }
            every { edit() } returns editor
        }

        context = mockk {
            every { getSharedPreferences("logeon_settings", Context.MODE_PRIVATE) } returns prefs
        }
    }

    @Test
    fun `themeMode defaults to SYSTEM when no preference is stored`() {
        val repository = ThemeRepository(context)
        assertEquals(AppThemeMode.SYSTEM, repository.themeMode.value)
    }

    @Test
    fun `setThemeMode updates StateFlow and persists across new repository instances`() {
        val repository = ThemeRepository(context)

        repository.setThemeMode(AppThemeMode.DARK)
        assertEquals(AppThemeMode.DARK, repository.themeMode.value)

        // Simulate app restart with a fresh repository reading the same SharedPreferences
        val reloadedRepository = ThemeRepository(context)
        assertEquals(AppThemeMode.DARK, reloadedRepository.themeMode.value)

        reloadedRepository.setThemeMode(AppThemeMode.LIGHT)
        assertEquals(AppThemeMode.LIGHT, reloadedRepository.themeMode.value)
    }

    @Test
    fun `invalid stored theme value falls back safely to SYSTEM`() {
        storage["theme_mode"] = "CORRUPTED_VALUE"

        val repository = ThemeRepository(context)
        assertEquals(AppThemeMode.SYSTEM, repository.themeMode.value)
    }
}