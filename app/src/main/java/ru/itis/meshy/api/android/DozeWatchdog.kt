package ru.itis.meshy.api.android

interface DozeWatchdog {

    fun getAndResetDozeFlag(): Boolean
}