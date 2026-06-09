package ru.itis.meshy.android

import com.vanniktech.emoji.EmojiUtils
import com.vanniktech.emoji.RecentEmoji
import com.vanniktech.emoji.emoji.Emoji
import ru.itis.messaging_engine.api.db.DatabaseExecutor
import ru.itis.messaging_engine.api.db.DbException
import ru.itis.messaging_engine.api.db.Transaction
import ru.itis.messaging_engine.api.lifecycle.LifecycleManager.OpenDatabaseHook
import ru.itis.messaging_engine.api.settings.Settings
import ru.itis.messaging_engine.api.settings.SettingsManager
import ru.itis.messaging_engine.api.system.AndroidExecutor
import ru.itis.messaging_engine.util.LogUtils.logException
import ru.itis.messaging_engine.util.StringUtils
import org.briarproject.nullsafety.MethodsNotNullByDefault
import org.briarproject.nullsafety.ParametersNotNullByDefault
import ru.itis.meshy.android.settings.SettingsFragment.SETTINGS_NAMESPACE
import java.util.LinkedList
import java.util.concurrent.Executor
import java.util.logging.Level.WARNING
import java.util.logging.Logger
import javax.inject.Inject

/**
 * Хранит LRU-список последних использованных эмодзи; при открытии БД
 * восстанавливает его из настроек, при вызове [persist] — пишет обратно.
 *
 * Список обновляется только на UI-потоке (контракт `RecentEmoji`),
 * поэтому без блокировок.
 */
@MethodsNotNullByDefault
@ParametersNotNullByDefault
internal class RecentEmojiImpl @Inject constructor(
    @DatabaseExecutor private val dbExecutor: Executor,
    private val androidExecutor: AndroidExecutor,
    private val settingsManager: SettingsManager,
) : RecentEmoji, OpenDatabaseHook {

    // UI thread
    private val recentlyUsed = LinkedList<Emoji>()

    override fun getRecentEmojis(): Collection<Emoji> = ArrayList(recentlyUsed)

    override fun addEmoji(emoji: Emoji) {
        recentlyUsed.remove(emoji)
        recentlyUsed.add(0, emoji)
        if (recentlyUsed.size > EMOJI_LRU_SIZE) recentlyUsed.removeLast()
    }

    override fun persist() {
        if (recentlyUsed.isNotEmpty()) save(serialize(recentlyUsed))
    }

    @Throws(DbException::class)
    override fun onDatabaseOpened(txn: Transaction) {
        val settings = settingsManager.getSettings(txn, SETTINGS_NAMESPACE)
        val serialized = settings[EMOJI_LRU_PREFERENCE]
        if (serialized != null) {
            androidExecutor.runOnUiThread {
                recentlyUsed.addAll(deserialize(serialized))
            }
        }
    }

    private fun serialize(emojis: Collection<Emoji>): String {
        val strings = emojis.map { it.unicode }
        return StringUtils.join(strings, "\t")
    }

    private fun deserialize(serialized: String): Collection<Emoji> {
        val ranges = EmojiUtils.emojis(serialized)
        return ranges.map { it.emoji }
    }

    private fun save(serialized: String) {
        dbExecutor.execute {
            val settings = Settings()
            settings[EMOJI_LRU_PREFERENCE] = serialized
            try {
                settingsManager.mergeSettings(settings, SETTINGS_NAMESPACE)
            } catch (e: DbException) {
                logException(LOG, WARNING, e)
            }
        }
    }

    companion object {
        private val LOG: Logger = Logger.getLogger(RecentEmojiImpl::class.java.name)

        private const val EMOJI_LRU_PREFERENCE = "pref_emoji_recent2"
        private const val EMOJI_LRU_SIZE = 50
    }
}