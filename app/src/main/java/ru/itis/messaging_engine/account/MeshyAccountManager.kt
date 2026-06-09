package ru.itis.messaging_engine.account

import android.app.Application
import android.content.SharedPreferences
import ru.itis.messaging_engine.api.crypto.CryptoComponent
import ru.itis.messaging_engine.api.db.DatabaseConfig
import ru.itis.messaging_engine.api.identity.IdentityManager
import ru.itis.meshy.R
import ru.itis.meshy.android.Localizer
import ru.itis.meshy.android.util.UiUtils
import javax.inject.Inject

/**
 * Meshy-specific [AndroidAccountManager]: добавляет переинициализацию
 * локализации и сброс темы при удалении аккаунта.
 *
 * Package-private (`internal`), как и в исходном Java-коде: класс
 * предоставляется наружу только через [MeshyAccountModule].
 *
 * Внешние импорты `org.briarproject.bramble.api.*` оставлены без
 * переименования — это апстримная библиотека MessagingEngine Core,
 * которая будет переименована отдельно.
 */
internal class MeshyAccountManager @Inject constructor(
    databaseConfig: DatabaseConfig,
    crypto: CryptoComponent,
    identityManager: IdentityManager,
    prefs: SharedPreferences,
    app: Application,
) : AndroidAccountManager(databaseConfig, crypto, identityManager, prefs, app) {

    override fun deleteAccount() {
        synchronized(stateChangeLock) {
            super.deleteAccount()
            Localizer.reinitialize()
            UiUtils.setTheme(
                appContext,
                appContext.getString(R.string.pref_theme_system_value),
            )
        }
    }
}