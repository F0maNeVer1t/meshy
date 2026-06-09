package ru.itis.meshy.android.conversation.glide

import android.content.Context
import android.util.Log.WARN
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import ru.itis.meshy.api.attachment.AttachmentHeader
import org.briarproject.nullsafety.NotNullByDefault
import ru.itis.meshy.android.MeshyApplication
import java.io.InputStream

/**
 * Glide-модуль приложения. Регистрирует [MeshyModelLoaderFactory] как загрузчик
 * для [AttachmentHeader] → [InputStream]. Бывший `BriarGlideModule`.
 *
 * `applyOptions`: log-level фиксирован на `WARN`. Раньше тут было ветвление
 * `IS_DEBUG_BUILD ? DEBUG : WARN`; после унификации сборок осталась только
 * production-ветка. Если нужно временно повысить verbosity при отладке —
 * меняй значение здесь руками.
 */
@GlideModule
@NotNullByDefault
class MeshyGlideModule : AppGlideModule() {

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        val app = context.applicationContext as MeshyApplication
        val factory = MeshyModelLoaderFactory(app)
        registry.prepend(AttachmentHeader::class.java, InputStream::class.java, factory)
    }

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setLogLevel(WARN)
    }

    override fun isManifestParsingEnabled(): Boolean = false
}