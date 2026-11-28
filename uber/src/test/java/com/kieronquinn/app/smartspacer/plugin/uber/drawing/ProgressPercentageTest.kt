package com.kieronquinn.app.smartspacer.plugin.uber.drawing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProgressPercentageTest {

    @Test
    fun nullProgressIsOmitted() {
        assertNull(null.toProgressPercentage())
    }

    @Test
    fun zeroProgressIsFormatted() {
        assertEquals("0%", 0f.toProgressPercentage())
    }

    @Test
    fun fractionalProgressIsRoundedToNearestWholePercent() {
        assertEquals("46%", 0.456f.toProgressPercentage())
    }

    @Test
    fun completeProgressIsFormatted() {
        assertEquals("100%", 1f.toProgressPercentage())
    }

    @Test
    fun outOfRangeValuesAreClampedForDisplay() {
        assertEquals("0%", (-0.2f).toProgressPercentage())
        assertEquals("100%", 1.2f.toProgressPercentage())
    }

    @Test
    fun nonFiniteProgressIsOmitted() {
        assertNull(Float.NaN.toProgressPercentage())
        assertNull(Float.POSITIVE_INFINITY.toProgressPercentage())
        assertNull(Float.NEGATIVE_INFINITY.toProgressPercentage())
    }
}
