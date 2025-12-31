package com.yonatankarp.feature4k.property

import com.yonatankarp.feature4k.property.PropertyFixtures.HIGH_PRECISION_DECIMAL
import com.yonatankarp.feature4k.property.PropertyFixtures.PRICE_TIERS
import com.yonatankarp.feature4k.property.PropertyFixtures.PRICE_TIER_MEDIUM
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for PropertyBigDecimal class.
 *
 * @author Yonatan Karp-Rudin
 */
class PropertyBigDecimalTest {
    @Test
    fun `stores name and value`() {
        // Given
        val name = "preciseValue"
        val value = HIGH_PRECISION_DECIMAL

        // When
        val property = PropertyBigDecimal(name = name, value = value)

        // Then
        assertEquals(name, property.name)
        assertEquals(value, property.value)
    }

    @Test
    fun `validates value is valid BigDecimal`() {
        // Given
        val name = "invalid"
        val value = "not-a-decimal"

        // When/Then
        assertFailsWith<IllegalArgumentException> {
            PropertyBigDecimal(name = name, value = value)
        }
    }

    @Test
    fun `serializes to JSON`() {
        // Given
        val property = PropertyBigDecimal(name = "price", value = "99.99")

        // When
        val jsonString = Json.encodeToString(PropertyBigDecimal.serializer(), property)

        // Then
        assertTrue(jsonString.contains("\"name\":\"price\""))
        assertTrue(jsonString.contains("\"value\":\"99.99\""))
    }

    @Test
    fun `deserializes from JSON`() {
        // Given
        val original = PropertyBigDecimal(name = "amount", value = "1234.56", description = "Transaction amount")

        // When
        val jsonString = Json.encodeToString(PropertyBigDecimal.serializer(), original)
        val deserialized = Json.decodeFromString(PropertyBigDecimal.serializer(), jsonString)

        // Then
        assertEquals(original, deserialized)
    }

    @Test
    fun `hasFixedValues returns false when no fixed values`() {
        // Given
        val name = "preciseValue"
        val value = HIGH_PRECISION_DECIMAL

        // When
        val property = PropertyBigDecimal(name = name, value = value)

        // Then
        assertFalse(property.hasFixedValues)
    }

    @Test
    fun `hasFixedValues returns true when fixed values defined`() {
        // Given
        val name = "price"
        val value = PRICE_TIER_MEDIUM
        val fixedValues = PRICE_TIERS

        // When
        val property = PropertyBigDecimal(name = name, value = value, fixedValues = fixedValues)

        // Then
        assertTrue(property.hasFixedValues)
    }

    @Test
    fun `isValid returns true when no fixed values`() {
        // Given
        val name = "preciseValue"
        val value = HIGH_PRECISION_DECIMAL

        // When
        val property = PropertyBigDecimal(name = name, value = value)

        // Then
        assertTrue(property.isValid)
    }

    @Test
    fun `isValid returns true when value in fixed values`() {
        // Given
        val name = "price"
        val value = PRICE_TIER_MEDIUM
        val fixedValues = PRICE_TIERS

        // When
        val property = PropertyBigDecimal(name = name, value = value, fixedValues = fixedValues)

        // Then
        assertTrue(property.isValid)
    }

    @Test
    fun `isValid returns false when value not in fixed values`() {
        // Given
        val name = "price"
        val value = "49.99"
        val fixedValues = PRICE_TIERS

        // When
        val property = PropertyBigDecimal(name = name, value = value, fixedValues = fixedValues)

        // Then
        assertFalse(property.isValid)
    }
}
