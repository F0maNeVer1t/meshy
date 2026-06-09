package ru.itis.meshy.android

import android.net.TrafficStats
import android.os.Process
import ru.itis.messaging_engine.util.LogUtils.now
import org.briarproject.nullsafety.NotNullByDefault
import ru.itis.meshy.api.android.NetworkUsageMetrics
import ru.itis.meshy.api.android.NetworkUsageMetrics.Metrics
import java.util.logging.Level.INFO
import java.util.logging.Logger

/**
 * Считает байты rx/tx для UID приложения за сессию (от `startService`
 * до `stopService`). На остановке логирует итоговые цифры на INFO.
 */
@NotNullByDefault
internal class NetworkUsageMetricsImpl : NetworkUsageMetrics {

    @Volatile private var startTime: Long = 0L
    @Volatile private var rxBytes: Long = 0L
    @Volatile private var txBytes: Long = 0L

    override fun startService() {
        startTime = now()
        val uid = Process.myUid()
        rxBytes = TrafficStats.getUidRxBytes(uid)
        txBytes = TrafficStats.getUidTxBytes(uid)
    }

    override fun stopService() {
        if (LOG.isLoggable(INFO)) {
            val metrics = getMetrics()
            LOG.info("Duration ${metrics.sessionDurationMs / 1000} seconds")
            LOG.info("Received ${metrics.rxBytes} bytes")
            LOG.info("Sent ${metrics.txBytes} bytes")
        }
    }

    override fun getMetrics(): Metrics {
        val sessionDurationMs = now() - startTime
        val uid = Process.myUid()
        val rx = TrafficStats.getUidRxBytes(uid) - rxBytes
        val tx = TrafficStats.getUidTxBytes(uid) - txBytes
        return Metrics(sessionDurationMs, rx, tx)
    }

    companion object {
        private val LOG: Logger = Logger.getLogger(NetworkUsageMetricsImpl::class.java.name)
    }
}