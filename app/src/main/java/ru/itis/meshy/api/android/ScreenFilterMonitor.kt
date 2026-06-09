package ru.itis.meshy.api.android

import androidx.annotation.UiThread
import org.briarproject.nullsafety.NotNullByDefault

@NotNullByDefault
interface ScreenFilterMonitor {

    /**
     * Returns the details of all apps that have requested the
     * `SYSTEM_ALERT_WINDOW` permission, excluding system apps, Google Play
     * Services, and any apps that have been allowed by calling
     * [allowApps].
     *
     * Only works on `SDK_INT 29` and below.
     */
    @UiThread
    fun getApps(): Collection<AppDetails>

    /**
     * Allows the apps with the given package names to use overlay windows.
     * They will not be returned by future calls to [getApps].
     *
     * Only works on `SDK_INT 29` and below.
     */
    @UiThread
    fun allowApps(packageNames: Collection<String>)

    /**
     * Описание стороннего приложения, претендующего на оверлей экрана.
     *
     * `@JvmField` сохраняет прямой доступ `appDetails.name` и
     * `appDetails.packageName` из существующего Java-кода без
     * вызова геттеров (как было в исходных `public final`-полях).
     */
    data class AppDetails(
        @JvmField val name: String,
        @JvmField val packageName: String,
    )
}