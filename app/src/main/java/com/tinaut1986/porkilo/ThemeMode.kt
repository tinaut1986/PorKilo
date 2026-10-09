package com.tinaut1986.porkilo

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class ThemeMode : Parcelable {
    SYSTEM, LIGHT, DARK
}
