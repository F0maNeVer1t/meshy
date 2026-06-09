package ru.itis.meshy.android

interface DestroyableContext {

    fun runOnUiThreadUnlessDestroyed(runnable: Runnable)
}