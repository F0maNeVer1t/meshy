package ru.itis.meshy.api.android

import ru.itis.messaging_engine.api.lifecycle.Service
import org.briarproject.nullsafety.NotNullByDefault

@NotNullByDefault
interface NetworkUsageMetrics : Service {

    fun getMetrics(): Metrics

    /**
     * Снимок метрик сетевой активности за текущую сессию.
     *
     * Был обычным Java-классом с приватными финальными полями и
     * геттерами; в Kotlin это естественно ложится на `data class`
     * с `val`-свойствами. Геттеры `getSessionDurationMs()` /
     * `getRxBytes()` / `getTxBytes()` сохраняются автоматически —
     * Kotlin генерирует их под капотом.
     */
    data class Metrics(
        val sessionDurationMs: Long,
        val rxBytes: Long,
        val txBytes: Long,
    )
}