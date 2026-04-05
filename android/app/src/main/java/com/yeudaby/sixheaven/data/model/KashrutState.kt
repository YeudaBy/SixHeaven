package com.yeudaby.sixheaven.data.model

enum class KashrutState {
    NEUTRAL,   // Pareve – no restriction
    MEATY,     // Ate meat, waiting before dairy
    DAIRY      // Ate dairy, waiting before meat
}

data class WaitSettings(
    val meatWaitMinutes: Int = 360,   // 6 hours default
    val dairyWaitMinutes: Int = 30    // 30 minutes default
)

data class KashrutTimerState(
    val state: KashrutState = KashrutState.NEUTRAL,
    val startTimeMs: Long = 0L,
    val totalWaitMs: Long = 0L
) {
    val endTimeMs: Long
        get() = startTimeMs + totalWaitMs

    val remainingMs: Long
        get() = (endTimeMs - System.currentTimeMillis()).coerceAtLeast(0L)

    val isActive: Boolean
        get() = state != KashrutState.NEUTRAL && remainingMs > 0L

    /** Fraction of the wait period already elapsed (0.0 → just started, 1.0 → done). */
    val progressFraction: Float
        get() {
            if (totalWaitMs == 0L || !isActive) return 0f
            val elapsed = (System.currentTimeMillis() - startTimeMs).coerceAtLeast(0L)
            return (elapsed.toFloat() / totalWaitMs.toFloat()).coerceIn(0f, 1f)
        }
}
