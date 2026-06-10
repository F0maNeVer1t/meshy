package ru.itis.meshy.android.priority

import android.app.Application
import dagger.Module
import dagger.Provides
import ru.itis.meshy.api.messaging.priority.MessagePriorityClassifier
import javax.inject.Singleton

@Module
class PriorityModule {

    @Provides
    @Singleton
    fun provideMessagePriorityClassifier(
        application: Application
    ): MessagePriorityClassifier =
        MessagePriorityClassifierImpl(application.applicationContext)
}