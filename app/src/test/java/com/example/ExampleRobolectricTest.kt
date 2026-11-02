package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ReviewEntity
import com.example.data.repository.HirafiRepository
import com.example.util.AppLanguage
import com.example.util.Localization
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("دوّرني", appName)
    }

    @Test
    fun `test distance calculation between Tripoli districts`() {
        // Hay Al-Andalus to Ben Ashour (approx 3.5 km)
        val dist = HirafiRepository.calculateDistanceKm(
            32.8910, 13.1520,
            32.8760, 13.1860
        )
        assertTrue(dist in 2.0..6.0)
    }

    @Test
    fun `test review overall rating calculation`() {
        val review = ReviewEntity(
            craftsmanId = 1,
            clientName = "سالم",
            qualityRating = 5.0f,
            punctualityRating = 4.0f,
            communicationRating = 4.5f,
            comment = "ممتاز"
        )
        assertEquals(4.5f, review.overallRating, 0.01f)
    }

    @Test
    fun `test bilingual localization strings`() {
        val appNameAr = Localization.get("app_name", AppLanguage.ARABIC)
        val appNameEn = Localization.get("app_name", AppLanguage.ENGLISH)
        assertEquals("دوّرني", appNameAr)
        assertEquals("Dawarni", appNameEn)
    }

    @Test
    fun `test notification preferences defaults`() {
        val prefs = com.example.data.model.NotificationPreferences()
        assertTrue(prefs.newMatchingRequestsEnabled)
        assertTrue(prefs.requestAcceptedEnabled)
        assertTrue(prefs.serviceStatusUpdatesEnabled)
        assertTrue(prefs.appointmentRemindersEnabled)
        assertTrue(prefs.soundAndVibrateEnabled)
    }

    @Test
    fun `test portfolio item creation`() {
        val item = com.example.data.model.PortfolioItemEntity(
            craftsmanId = 1,
            title = "تركيب شبكة PEX",
            description = "شبكة مياه معزولة بالكامل",
            mediaUri = "sample_uri",
            projectCostLyd = "1,200 د.ل",
            completedDate = "أكتوبر 2026"
        )
        assertEquals("تركيب شبكة PEX", item.title)
        assertEquals("1,200 د.ل", item.projectCostLyd)
    }
}
