package com.tinaut1986.pricesmart

import com.tinaut1986.pricesmart.model.Offer
import com.tinaut1986.pricesmart.model.OfferType
import com.tinaut1986.pricesmart.model.Product
import org.junit.Assert.assertEquals
import org.junit.Test

class ProductPriceTest {
    private val delta = 1e-9

    private fun product(offer: Offer = Offer(), compareQuantity: Int = 1, unit: String = "kg") = Product(
        name = "Test",
        price = 25.0,
        quantityPerUnit = 2.0,
        unit = unit,
        offer = offer,
        compareQuantity = compareQuantity
    )

    @Test
    fun buyXPayY_onlyCompleteBatchesAreDiscounted() {
        val offer = Offer(OfferType.BUY_X_PAY_Y, 3.0, 2.0)
        assertEquals(25.0, product(offer, 1).totalComparePrice, delta)
        assertEquals(50.0, product(offer, 3).totalComparePrice, delta)
        assertEquals(75.0, product(offer, 4).totalComparePrice, delta)
        assertEquals(100.0, product(offer, 5).totalComparePrice, delta)
        assertEquals(100.0, product(offer, 6).totalComparePrice, delta)
    }

    @Test
    fun buyXPayY_perBaseUnitAndAverage() {
        val p = product(Offer(OfferType.BUY_X_PAY_Y, 3.0, 2.0), 4)
        assertEquals(18.75, p.pricePerUnitInCompare, delta)
        assertEquals(9.375, p.pricePerBaseUnit, delta)
        assertEquals(8.0, p.totalCompareQuantity, delta)
        assertEquals(25, p.savingPercentage)
    }

    @Test
    fun nthUnitDiscount_appliesToEveryNthUnit() {
        val offer = Offer(OfferType.NTH_UNIT_DISCOUNT, 2.0, 50.0)
        assertEquals(37.5, product(offer, 2).totalComparePrice, delta)
        assertEquals(62.5, product(offer, 3).totalComparePrice, delta)
    }

    @Test
    fun fixedPriceForX_remainderAtFullPrice() {
        val offer = Offer(OfferType.FIXED_PRICE_FOR_X, 3.0, 60.0)
        assertEquals(85.0, product(offer, 4).totalComparePrice, delta)
    }

    @Test
    fun extraQuantity_isIncludedInTotalQuantity() {
        val p = product(Offer(OfferType.EXTRA_QUANTITY, 50.0), 2)
        assertEquals(6.0, p.totalCompareQuantity, delta)
        assertEquals(50.0 / 6.0, p.pricePerBaseUnit, delta)
    }

    @Test
    fun zeroQuantity_isTreatedAsOne() {
        val p = product(compareQuantity = 0)
        assertEquals(25.0, p.totalComparePrice, delta)
        assertEquals(12.5, p.pricePerBaseUnit, delta)
    }
}
