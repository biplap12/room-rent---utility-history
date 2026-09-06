package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object FormatUtils {
    private val currencyFormat = DecimalFormat("#,##0.##", DecimalFormatSymbols(Locale.US))
    private val unitFormat = DecimalFormat("#,##0.#", DecimalFormatSymbols(Locale.US))

    fun formatMoney(amount: Double, symbol: String = "Rs."): String {
        return "$symbol ${currencyFormat.format(amount)}"
    }

    fun formatUnits(units: Double): String {
        return unitFormat.format(units)
    }

    fun parseDoubleOrZero(text: String): Double {
        return text.trim().toDoubleOrNull() ?: 0.0
    }
}
