package ru.itis.meshy.android

import ru.itis.messaging_engine.api.crypto.KeyStrengthener
import ru.itis.messaging_engine.api.db.DatabaseConfig
import org.briarproject.nullsafety.NotNullByDefault
import java.io.File

/**
 * Реализация [DatabaseConfig] для Android. `keyStrengthener` опционален —
 * на устройствах без StrongBox / без поддержки aware-keystore он может быть null.
 *
 * Геттеры реализованы через property-accessors (`override val ...`),
 * которые на JVM генерируют те же `getDatabaseDirectory()` / `getDatabaseKeyDirectory()`
 * / `getKeyStrengthener()` — Java-код, реализующий `DatabaseConfig`,
 * остаётся совместим.
 */
@NotNullByDefault
internal class AndroidDatabaseConfig(
    private val dbDir: File,
    private val keyDir: File,
    private val keyStrengthener: KeyStrengthener?,
) : DatabaseConfig {

    override fun getDatabaseDirectory(): File = dbDir

    override fun getDatabaseKeyDirectory(): File = keyDir

    override fun getKeyStrengthener(): KeyStrengthener? = keyStrengthener
}