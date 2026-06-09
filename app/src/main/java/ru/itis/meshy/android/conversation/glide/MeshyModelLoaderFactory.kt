package ru.itis.meshy.android.conversation.glide

import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import ru.itis.meshy.api.attachment.AttachmentHeader
import org.briarproject.nullsafety.NotNullByDefault
import ru.itis.meshy.android.MeshyApplication
import java.io.InputStream

/**
 * Фабрика [MeshyModelLoader]-ов. Glide требует фабрику, потому что
 * `ModelLoader` создаётся лениво при первом запросе на загрузку.
 * Бывший `BriarModelLoaderFactory`.
 */
@NotNullByDefault
internal class MeshyModelLoaderFactory(
    private val app: MeshyApplication,
) : ModelLoaderFactory<AttachmentHeader, InputStream> {

    override fun build(
        multiFactory: MultiModelLoaderFactory,
    ): ModelLoader<AttachmentHeader, InputStream> = MeshyModelLoader(app)

    override fun teardown() {
        // noop
    }
}