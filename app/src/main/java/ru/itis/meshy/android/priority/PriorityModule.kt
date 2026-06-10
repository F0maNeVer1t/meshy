package ru.itis.meshy.android.priority

import android.app.Application
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class PriorityModule {

    @Provides
    @Singleton
    fun provideMessagePriorityClassifier(
        application: Application
    ): MessagePriorityClassifier =
        MessagePriorityClassifier(application.applicationContext)
}