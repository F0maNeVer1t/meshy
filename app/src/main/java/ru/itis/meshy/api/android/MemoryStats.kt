package ru.itis.meshy.api.android

import java.io.Serializable
import javax.annotation.concurrent.Immutable

/**
 * Memory usage stats to be included in feedback and crash reports. This class
 * is [Serializable] so it can be passed from the crashed process to the
 * crash reporter process.
 *
 * Реализовано как `data class` с `val`-полями — иммутабельно по
 * построению, аннотация [Immutable] оставлена как документация.
 */
@Immutable
data class MemoryStats(
    @JvmField val systemMemoryTotal: Long,
    @JvmField val systemMemoryFree: Long,
    @JvmField val systemMemoryThreshold: Long,
    @JvmField val systemMemoryLow: Boolean,
    @JvmField val vmMemoryTotal: Long,
    @JvmField val vmMemoryFree: Long,
    @JvmField val vmMemoryMax: Long,
    @JvmField val nativeHeapTotal: Long,
    @JvmField val nativeHeapAllocated: Long,
    @JvmField val nativeHeapFree: Long,
) : Serializable {

    companion object {
        // Сериализуемый класс должен иметь стабильный serialVersionUID,
        // иначе при изменении класса между версиями приложения старые
        // сериализованные данные перестают читаться.
        private const val serialVersionUID: Long = 1L
    }
}