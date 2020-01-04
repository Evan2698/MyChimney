package com.example.mychimney.datacore

import java.text.DecimalFormat

class FormatUtils {

    private val units = arrayOf("KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB", "BB", "NB", "DB", "CB")

    private val numberFormat = DecimalFormat("@@@")


    fun Format(size: Long): String {
        var n: Double = size.toDouble()
        var i = -1
        while (n >= 999.5) {
            n /= 1024
            ++i
        }
        return if (i < 0) "0.1~KB"
        else "${numberFormat.format(n)} ${units[i]}"
    }
}