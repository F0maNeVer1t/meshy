package ru.itis.messaging_engine.account

import dagger.Module
import dagger.Provides
import ru.itis.messaging_engine.api.account.AccountManager
import javax.inject.Singleton

/**
 * Dagger-модуль, поставляющий [AccountManager] из meshy-специфичной
 * реализации [MeshyAccountManager].
 *
 * Подключается в составе AppModule / AndroidComponent.
 */
@Module
class MeshyAccountModule {

    @Provides
    @Singleton
    internal fun provideAccountManager(
        accountManager: MeshyAccountManager,
    ): AccountManager = accountManager
}