package com.koflox.currency_exchanger.util.number

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.*

private val suffixesDecimal: NavigableMap<BigDecimal, String> = TreeMap<BigDecimal, String>().apply {
    this["1000".toBigDecimal()] = "k"
    this["1000000".toBigDecimal()] = "m"
    this["1000000000".toBigDecimal()] = "b"
    this["1000000000000".toBigDecimal()] = "t"
    this["1000000000000000".toBigDecimal()] = "q"
    this["1000000000000000000".toBigDecimal()] = "sx"
    this["1000000000000000000000".toBigDecimal()] = "sp"
}

private val formatterMathContext = MathContext(5, RoundingMode.HALF_UP)

fun formatDecimal(value: BigDecimal, outputMathContext: MathContext): String {
    val mapDecimalForOutput: (BigDecimal) -> String = {
        it.setScale(outputMathContext.precision, outputMathContext.roundingMode)
            .stripTrailingZeros()
            .toPlainString()
    }
    if (value < BigDecimal(1000)) return mapDecimalForOutput(value)
    val (divideBy, suffix) = suffixesDecimal.floorEntry(value)!!
    val shortNumber = value.divide(divideBy, formatterMathContext)
    return mapDecimalForOutput(shortNumber) + suffix
}
