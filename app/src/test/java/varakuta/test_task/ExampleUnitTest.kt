package varakuta.test_task

import org.junit.Assert.*
import org.junit.Test

class UtilsTest {

    @Test
    fun `getOrientation returns vertical`() {
        val orientation = getOrientation(arrayOf(1080, 1920))
        assertEquals("vertical", orientation)
    }

    @Test
    fun `getOrientation returns horizontal`() {
        val orientation = getOrientation(arrayOf(1920, 1080))
        assertEquals("horizontal", orientation)
    }

    @Test
    fun `calculateGridCells vertical`() {
        val result = calculateGridCells(1080, 3f, "vertical")
        assertEquals(176f, result)
    }

    @Test
    fun `calculateGridCells horizontal`() { // NEW
        val result = calculateGridCells(1080, 3f, "horizontal")
        // expected = 1080 / 12 - 4 = 86
        assertEquals(86f, result)
    }

    @Test
    fun `calculateItemWidth vertical`() {
        val result = calculateItemWidth(1080, 3f, "vertical")
        // expected = (1080 / 2) / 3 = 180
        assertEquals(180f, result)
    }

    @Test
    fun `calculateItemWidth horizontal`() {
        val result = calculateItemWidth(1080, 3f, "horizontal")
        assertEquals(90f, result)
    }

    @Test
    fun `calculateImageHeight returns correct height`() {
        val result = calculateImageHeight(300, 600, 1080f, 3f)
        // expected = (600 * 1080 * 3 / 300) / 3 = 2160
        assertEquals(2160f, result)
    }

    @Test
    fun `scaleToFit vertical`() {
        val result = scaleToFit(arrayOf(1080, 1920), 300, 600, 3f, "vertical")
        assertTrue(result.first > 0)
        assertTrue(result.second > 0)
    }

    @Test
    fun `scaleToFit horizontal`() {
        val result = scaleToFit(arrayOf(1920, 1080), 600, 300, 3f, "horizontal")
        assertTrue(result.first > 0)
        assertTrue(result.second > 0)
    }

    @Test
    fun `generateFakeLoadingFields returns expected number`() {
        val list = generateFakeLoadingFields(1000, 100, 200)
        assertEquals(10, list.size)
        for (height in list) {
            assertTrue(height in 100..200)
        }
    }

    @Test
    fun `generateFakeLoadingFields handles zero screen height`() {
        val list = generateFakeLoadingFields(0, 100, 200)
        assertEquals(0, list.size)
    }
}
