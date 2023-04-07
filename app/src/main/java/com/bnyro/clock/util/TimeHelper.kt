package com.bnyro.clock.util

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

object TimeHelper {
    val currentTime: Date get() = Calendar.getInstance().time

    fun getAvailableTimeZones(): List<com.bnyro.clock.obj.TimeZone> {
        return TimeZone.getAvailableIDs().distinct().mapNotNull {
            val zone = TimeZone.getTimeZone(it)
            getDisplayName(it)?.let { displayName ->
                com.bnyro.clock.obj.TimeZone(it, displayName, zone.rawOffset)
            }
        }.distinctBy { it.displayName }
            .sortedBy { it.displayName }
    }

    private fun getDisplayName(timeZone: String): String? {
        return timeZone
            .takeIf { it.none { c -> c.isDigit() } }
            ?.split("/", limit = 2)
            ?.takeIf { it.size > 1 }
            ?.let { "${it.last().replace("_", "")} (${it.first()})" }
    }

    fun getOffset(): Int {
        val mCalendar: Calendar = GregorianCalendar()
        return mCalendar.timeZone.rawOffset
    }

    fun getCurrentWeekDay(): Int {
        return Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    }

    fun formatDateTime(time: Date): Pair<String, String> {
        val datePattern: String = android.text.format.DateFormat.getBestDateTimePattern(
            Locale.getDefault(),
            "EE dd-MMM-yyyy",
        )
        val dateFormatter: DateFormat = SimpleDateFormat(datePattern, Locale.getDefault())
        val timeFormatter: DateFormat = DateFormat.getTimeInstance()
        return dateFormatter.format(time) to timeFormatter.format(time)
    }
}
