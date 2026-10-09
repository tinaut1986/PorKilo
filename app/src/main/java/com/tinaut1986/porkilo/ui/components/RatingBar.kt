package com.tinaut1986.porkilo.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tinaut1986.porkilo.R
import java.text.NumberFormat

private val StarColor = Color(0xFFFFB300)
private const val MAX_STARS = 5

/**
 * Five-star rating with half-star precision. Tapping the left half of a star selects a half star;
 * tapping the current value again clears the rating. Read-only when [onRatingChange] is null.
 */
@Composable
fun RatingBar(
    rating: Float,
    modifier: Modifier = Modifier,
    starSize: Dp = 20.dp,
    onRatingChange: ((Float) -> Unit)? = null
) {
    val description = stringResource(
        R.string.rating_description,
        NumberFormat.getNumberInstance().format(rating)
    )
    Row(modifier = modifier.semantics { contentDescription = description }) {
        for (star in 1..MAX_STARS) {
            val icon = when {
                rating >= star -> Icons.Filled.Star
                rating >= star - 0.5f -> Icons.AutoMirrored.Filled.StarHalf
                else -> Icons.Filled.StarBorder
            }
            val tapModifier = if (onRatingChange != null) {
                Modifier.pointerInput(rating, onRatingChange) {
                    detectTapGestures { offset ->
                        val tapped = if (offset.x < size.width / 2) star - 0.5f else star.toFloat()
                        onRatingChange(if (tapped == rating) 0f else tapped)
                    }
                }
            } else {
                Modifier
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (icon == Icons.Filled.StarBorder) StarColor.copy(alpha = 0.6f) else StarColor,
                modifier = Modifier.size(starSize).then(tapModifier)
            )
        }
    }
}
