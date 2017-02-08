package charlie.bilionlinekeeper.util

import java.util.*

object TimeUtil {
    fun calculateToTomorrowMillis(): Long {
        val now = Date()
        return Calendar.getInstance().apply {
            time = now
            isLenient = true
            this[Calendar.DAY_OF_YEAR]++
            this[Calendar.HOUR_OF_DAY] = 0
            this[Calendar.MINUTE] = 0
            this[Calendar.SECOND] = 0
            this[Calendar.MILLISECOND] = 0
        }.timeInMillis - now.time
    }
}