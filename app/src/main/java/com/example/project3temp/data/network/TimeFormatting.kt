package com.example.project3temp.data.network

fun formatHours(open: String?, close: String?): String? {
    val o = extractHourMinute(open) ?: return null
    val c = extractHourMinute(close) ?: return null
    return "$o ~ $c"
}

// Accepts any "yyyy-MM-ddTHH:mm:ss[.fraction][timezone]" string and returns "HH:mm".
// Treats the value as wall-clock time (no timezone conversion) since the backend
// sends LocalDateTime without an offset.
private fun extractHourMinute(iso: String?): String? {
    if (iso.isNullOrBlank()) return null
    val tIdx = iso.indexOf('T')
    if (tIdx < 0 || iso.length < tIdx + 6) return null
    val hm = iso.substring(tIdx + 1, tIdx + 6)
    if (hm.length != 5 || hm[2] != ':') return null
    if (!hm.substring(0, 2).all { it.isDigit() }) return null
    if (!hm.substring(3, 5).all { it.isDigit() }) return null
    return hm
}
