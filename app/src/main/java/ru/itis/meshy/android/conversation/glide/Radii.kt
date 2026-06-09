package ru.itis.meshy.android.conversation.glide

import org.briarproject.nullsafety.NotNullByDefault

/**
 * Четыре радиуса скругления углов прямоугольника в пикселях.
 * Используется в [CustomCornersTransformation] для conversation-сообщений.
 *
 * `data class` даёт автоматические [equals]/[hashCode]/[toString].
 * Java-код продолжает обращаться к полям как `radii.topLeft` через
 * `@JvmField` — благодаря этому остаются прямые поля, как в исходнике.
 */
@NotNullByDefault
data class Radii(
    @JvmField val topLeft: Int,
    @JvmField val topRight: Int,
    @JvmField val bottomLeft: Int,
    @JvmField val bottomRight: Int,
)