package com.example.project3temp.data.network

fun formatHours(open: String?, close: String?): String? {
    val o = validateHourMinute(open) ?: return null
    val c = validateHourMinute(close) ?: return null
    return "$o ~ $c"
}

// "HH:mm" 형식인지 확인하고 그대로 반환 (백엔드가 "HH:mm" 문자열로 내려준다고 가정)
private fun validateHourMinute(hm: String?): String? {
    if (hm.isNullOrBlank()) return null
    if (hm.length != 5 || hm[2] != ':') return null // 길이 5 아니거나 중간에 : 없으면 null 반환
    if (!hm.substring(0, 2).all { it.isDigit() }) return null // 중간 : 제외, 숫자가 아니면 null 반환
    if (!hm.substring(3, 5).all { it.isDigit() }) return null
    return hm
}
