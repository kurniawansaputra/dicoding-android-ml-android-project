package com.dicoding.asclepius.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun dateFormatter(input: String): String {
    val sdfInput = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH)
    sdfInput.timeZone = TimeZone.getTimeZone("UTC")

    val sdfOutput = SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH)
    val date = sdfInput.parse(input)
    return sdfOutput.format(date as Date)
}
