package ru.itis.meshy.api.android

import android.app.Activity
import androidx.annotation.UiThread
import androidx.lifecycle.LiveData

/**
 * Управляет авто-блокировкой приложения по таймауту неактивности.
 *
 * Под Compose [isLockable] также можно будет наблюдать через
 * `liveData.observeAsState()` (lifecycle-runtime-compose) без изменений
 * сигнатуры — поэтому пока оставляем LiveData. Когда экраны переедут
 * на Compose, можно будет добавить параллельный `val isLockable: StateFlow<Boolean>`,
 * не ломая существующих Java/View-потребителей.
 */
interface LockManager {

    /**
     * Stops the inactivity timer when the user interacts with the app.
     * Should typically be called by [Activity.onStart].
     */
    @UiThread
    fun onActivityStart()

    /**
     * Starts the inactivity timer which will lock the app.
     * Should typically be called by [Activity.onStop].
     */
    @UiThread
    fun onActivityStop()

    /**
     * Returns an observable LiveData to indicate whether the app can be locked.
     */
    fun isLockable(): LiveData<Boolean>

    /**
     * Updates the LiveData returned by [isLockable].
     * It checks whether a device screen lock is available and
     * whether the app setting is checked.
     */
    @UiThread
    fun checkIfLockable()

    /**
     * Returns true if app is currently locked, false otherwise.
     * If the device's screen lock was removed while the app was locked,
     * calling this will unlock the app automatically.
     */
    fun isLocked(): Boolean

    /**
     * Locks the app if true is passed, otherwise unlocks the app.
     */
    fun setLocked(locked: Boolean)

    companion object {
        const val ACTION_LOCK = "lock"
        const val EXTRA_PID = "PID"
    }
}