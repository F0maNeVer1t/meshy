package ru.itis.meshy.android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.os.Build.VERSION.SDK_INT
import android.os.PowerManager
import android.os.PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED
import android.os.PowerManager.ACTION_DEVICE_LIGHT_IDLE_MODE_CHANGED
import android.os.PowerManager.ACTION_LOW_POWER_STANDBY_ENABLED_CHANGED
import androidx.annotation.RequiresApi
import ru.itis.messaging_engine.api.lifecycle.Service
import ru.itis.messaging_engine.util.AndroidUtils.registerReceiver
import ru.itis.meshy.api.android.DozeWatchdog
import java.util.concurrent.atomic.AtomicBoolean
import java.util.logging.Level.WARNING
import java.util.logging.Logger

/**
 * Слушает системные broadcast'ы doze/light-idle/low-power-standby и
 * выставляет флаг — приложение его сбрасывает через [getAndResetDozeFlag],
 * чтобы понимать, было ли усыпление за время сессии.
 *
 * `DozeBroadcastReceiver` — `inner class`, потому что использует поля
 * внешнего класса (`appContext`, `dozed`).
 */
internal class DozeWatchdogImpl(
    private val appContext: Context,
) : DozeWatchdog, Service {

    private val dozed = AtomicBoolean(false)
    private val receiver: BroadcastReceiver = DozeBroadcastReceiver()

    override fun getAndResetDozeFlag(): Boolean = dozed.getAndSet(false)

    override fun startService() {
        if (SDK_INT < 23) return
        val filter = IntentFilter(ACTION_DEVICE_IDLE_MODE_CHANGED)
        if (SDK_INT >= 33) {
            filter.addAction(ACTION_DEVICE_LIGHT_IDLE_MODE_CHANGED)
            filter.addAction(ACTION_LOW_POWER_STANDBY_ENABLED_CHANGED)
        }
        registerReceiver(appContext, receiver, filter, false)
    }

    override fun stopService() {
        if (SDK_INT < 23) return
        appContext.unregisterReceiver(receiver)
    }

    private inner class DozeBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (SDK_INT < 23) return
            val action = intent.action ?: return
            val pm = appContext.getSystemService(POWER_SERVICE) as PowerManager
            if (ACTION_DEVICE_IDLE_MODE_CHANGED == action) {
                if (pm.isDeviceIdleMode) dozed.set(true)
            } else if (SDK_INT >= 33) {
                onReceive33(action, pm)
            }
        }

        @RequiresApi(33)
        private fun onReceive33(action: String, pm: PowerManager) {
            when (action) {
                ACTION_LOW_POWER_STANDBY_ENABLED_CHANGED -> {
                    if (pm.isLowPowerStandbyEnabled) {
                        if (LOG.isLoggable(WARNING)) {
                            LOG.warning("System is in low power standby mode")
                        }
                        dozed.set(true)
                    }
                }
                ACTION_DEVICE_LIGHT_IDLE_MODE_CHANGED -> {
                    if (pm.isDeviceLightIdleMode) {
                        if (LOG.isLoggable(WARNING)) {
                            LOG.warning("System is in light idle mode")
                        }
                        dozed.set(true)
                    }
                }
            }
        }
    }

    companion object {
        private val LOG: Logger = Logger.getLogger(DozeWatchdogImpl::class.java.name)
    }
}