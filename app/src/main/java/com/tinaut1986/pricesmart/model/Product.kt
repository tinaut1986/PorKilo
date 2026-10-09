package com.tinaut1986.pricesmart.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class OfferType : Parcelable {
    NONE,
    PERCENTAGE_DISCOUNT, // X% off
    BUY_X_PAY_Y,         // 3x2, 2x1
    NTH_UNIT_DISCOUNT,   // Nst unit -X%
    FIXED_PRICE_FOR_X,   // 3 for 5€
    EXTRA_QUANTITY       // +X% extra free
}

@Parcelize
data class Offer(
    val type: OfferType = OfferType.NONE,
    val value1: Double = 0.0, // X units, or N-th unit
    val value2: Double = 0.0  // Y units, or Discount %, or Fixed Price
) : Parcelable

@Parcelize
data class Product(
    val id: Long = System.currentTimeMillis(),
    val name: String,
    val price: Double,
    val unitsPerPackage: Int = 1,
    val quantityPerUnit: Double,
    val unit: String, // kg, g, l, ml, units, etc.
    val offer: Offer = Offer(),
    val barcode: String? = null,
    val compareQuantity: Int = 1
) : Parcelable {
    val totalQuantity: Double get() = unitsPerPackage * quantityPerUnit
    
    fun calculateTotalPrice(qty: Int): Double {
        return when (offer.type) {
            OfferType.NONE -> price * qty
            OfferType.PERCENTAGE_DISCOUNT -> (price * (1 - offer.value1 / 100.0)) * qty
            OfferType.BUY_X_PAY_Y -> {
                val x = offer.value1.toInt()
                val y = offer.value2.toInt()
                if (x > 0) {
                    val sets = qty / x
                    val remainder = qty % x
                    (sets * y + remainder) * price
                } else price * qty
            }
            OfferType.NTH_UNIT_DISCOUNT -> {
                val n = offer.value1.toInt()
                val discount = offer.value2 / 100.0
                if (n > 0) {
                    val discountedUnits = qty / n
                    val fullPriceUnits = qty - discountedUnits
                    (fullPriceUnits * price) + (discountedUnits * price * (1 - discount))
                } else price * qty
            }
            OfferType.FIXED_PRICE_FOR_X -> {
                val x = offer.value1.toInt()
                val fixedPrice = offer.value2
                if (x > 0) {
                    val sets = qty / x
                    val remainder = qty % x
                    (sets * fixedPrice) + (remainder * price)
                } else price * qty
            }
            OfferType.EXTRA_QUANTITY -> price * qty
        }
    }

    val effectiveCompareQuantity: Int get() = compareQuantity.coerceAtLeast(1)

    val totalComparePrice: Double get() = calculateTotalPrice(effectiveCompareQuantity)

    val pricePerUnitInCompare: Double get() = totalComparePrice / effectiveCompareQuantity

    // Quantity actually received per package, including any free extra quantity
    val effectiveQuantityPerPackage: Double get() = if (offer.type == OfferType.EXTRA_QUANTITY) {
        totalQuantity * (1 + offer.value1 / 100.0)
    } else {
        totalQuantity
    }

    val totalCompareQuantity: Double get() = effectiveQuantityPerPackage * effectiveCompareQuantity

    val pricePerBaseUnit: Double get() {
        val u = unit.lowercase()
        val factor = when {
            u.startsWith("g") && u.length == 1 -> 1000.0
            u.startsWith("ml") -> 1000.0
            else -> 1.0
        }
        
        return (pricePerUnitInCompare / effectiveQuantityPerPackage) * factor
    }

    val pricePerBaseUnitWithoutOffer: Double get() {
        val u = unit.lowercase()
        val factor = when {
            u.startsWith("g") && u.length == 1 -> 1000.0
            u.startsWith("ml") -> 1000.0
            else -> 1.0
        }
        return (price / totalQuantity) * factor
    }

    val savingPercentage: Int get() {
        if (offer.type == OfferType.NONE) return 0
        val normal = pricePerBaseUnitWithoutOffer
        val withOffer = pricePerBaseUnit
        if (normal <= 0) return 0
        return (((normal - withOffer) / normal) * 100).toInt().coerceAtLeast(0)
    }

    val baseUnit: String get() {
        val u = unit.lowercase()
        return when {
            u.contains("g") -> "kg"
            u.contains("l") || u.contains("ml") -> "l"
            else -> unit
        }
    }
}
