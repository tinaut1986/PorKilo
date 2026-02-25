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
    val barcode: String? = null
) : Parcelable {
    val totalQuantity: Double get() = unitsPerPackage * quantityPerUnit
    
    val pricePerBaseUnit: Double get() {
        val u = unit.lowercase()
        val factor = when {
            u.startsWith("g") && u.length == 1 -> 1000.0
            u.startsWith("ml") -> 1000.0
            else -> 1.0
        }
        
        val effectivePrice = when (offer.type) {
            OfferType.PERCENTAGE_DISCOUNT -> price * (1 - offer.value1 / 100.0)
            OfferType.BUY_X_PAY_Y -> {
                // value1 = X (buy), value2 = Y (pay)
                if (offer.value1 > 0) price * (offer.value2 / offer.value1) else price
            }
            OfferType.NTH_UNIT_DISCOUNT -> {
                // value1 = Unit index (N), value2 = Discount (%)
                // Buy N units, the N-th one is -D%
                // Cost for N units = (N-1)*price + price*(1 - D/100) = price * (N - D/100)
                // Average price per 1 unit = price * (1 - (D/100)/N)
                if (offer.value1 > 0) price * (1.0 - (offer.value2 / 100.0) / offer.value1) else price
            }
            OfferType.FIXED_PRICE_FOR_X -> {
                // value1 = X units, value2 = Total price
                if (offer.value1 > 0) offer.value2 / offer.value1 else price
            }
            else -> price
        }

        val effectiveQuantity = if (offer.type == OfferType.EXTRA_QUANTITY) {
            totalQuantity * (1 + offer.value1 / 100.0)
        } else {
            totalQuantity
        }

        return (effectivePrice / effectiveQuantity) * factor
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
