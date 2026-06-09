package ru.itis.meshy.android.conversation.glide

import android.graphics.Bitmap
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader.TileMode.CLAMP
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import org.briarproject.nullsafety.NotNullByDefault
import java.security.MessageDigest
import javax.annotation.concurrent.Immutable

/**
 * Трансформация округления углов изображения с **разными радиусами**
 * для каждого угла (задаются [Radii]). Используется в conversation для
 * группирования последовательных сообщений (первое сообщение в серии —
 * скруглён верх, последнее — низ, и т.п.).
 *
 * Рисует четыре прямоугольника, перекрывающихся в центре изображения,
 * каждый со своим радиусом скругления. Углы, у которых radius == 0,
 * рисуются обычным [Canvas.drawRect].
 */
@Immutable
@NotNullByDefault
internal class CustomCornersTransformation(
    private val radii: Radii,
) : BitmapTransformation() {

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int,
    ): Bitmap {
        val width = toTransform.width
        val height = toTransform.height

        val bitmap: Bitmap = pool[width, height, ARGB_8888]
        bitmap.setHasAlpha(true)

        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            isAntiAlias = true
            shader = BitmapShader(toTransform, CLAMP, CLAMP)
        }
        drawRect(canvas, paint, width.toFloat(), height.toFloat())
        return bitmap
    }

    private fun drawRect(canvas: Canvas, paint: Paint, width: Float, height: Float) {
        drawTopLeft(canvas, paint, radii.topLeft, width, height)
        drawTopRight(canvas, paint, radii.topRight, width, height)
        drawBottomLeft(canvas, paint, radii.bottomLeft, width, height)
        drawBottomRight(canvas, paint, radii.bottomRight, width, height)
    }

    private fun drawTopLeft(
        canvas: Canvas,
        paint: Paint,
        radius: Int,
        width: Float,
        height: Float,
    ) {
        val rect = RectF(
            0f,
            0f,
            width / 2 + radius + 1,
            height / 2 + radius + 1,
        )
        if (radius == 0) canvas.drawRect(rect, paint)
        else canvas.drawRoundRect(rect, radius.toFloat(), radius.toFloat(), paint)
    }

    private fun drawTopRight(
        canvas: Canvas,
        paint: Paint,
        radius: Int,
        width: Float,
        height: Float,
    ) {
        val rect = RectF(
            width / 2 - radius,
            0f,
            width,
            height / 2 + radius + 1,
        )
        if (radius == 0) canvas.drawRect(rect, paint)
        else canvas.drawRoundRect(rect, radius.toFloat(), radius.toFloat(), paint)
    }

    private fun drawBottomLeft(
        canvas: Canvas,
        paint: Paint,
        radius: Int,
        width: Float,
        height: Float,
    ) {
        val rect = RectF(
            0f,
            height / 2 - radius,
            width / 2 + radius + 1,
            height,
        )
        if (radius == 0) canvas.drawRect(rect, paint)
        else canvas.drawRoundRect(rect, radius.toFloat(), radius.toFloat(), paint)
    }

    private fun drawBottomRight(
        canvas: Canvas,
        paint: Paint,
        radius: Int,
        width: Float,
        height: Float,
    ) {
        val rect = RectF(
            width / 2 - radius,
            height / 2 - radius,
            width,
            height,
        )
        if (radius == 0) canvas.drawRect(rect, paint)
        else canvas.drawRoundRect(rect, radius.toFloat(), radius.toFloat(), paint)
    }

    override fun toString(): String = "ImageCornerTransformation($radii)"

    override fun equals(other: Any?): Boolean =
        other is CustomCornersTransformation && radii == other.radii

    override fun hashCode(): Int = ID.hashCode() + radii.hashCode()

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update((ID + radii).toByteArray(CHARSET))
    }

    companion object {
        private val ID: String = CustomCornersTransformation::class.java.name
    }
}