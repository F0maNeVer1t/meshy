package ru.itis.meshy.android.conversation.glide

import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.signature.ObjectKey
import ru.itis.meshy.api.attachment.AttachmentHeader
import org.briarproject.nullsafety.MethodsNotNullByDefault
import org.briarproject.nullsafety.ParametersNotNullByDefault
import ru.itis.meshy.android.MeshyApplication
import java.io.InputStream
import javax.inject.Inject

/**
 * Glide [ModelLoader] для конвертации [AttachmentHeader] в [InputStream].
 * Сам инжектирует [MeshyDataFetcherFactory] в конструкторе через
 * `app.getApplicationComponent().inject(this)`. Бывший `BriarModelLoader`.
 */
@MethodsNotNullByDefault
@ParametersNotNullByDefault
class MeshyModelLoader internal constructor(app: MeshyApplication) :
    ModelLoader<AttachmentHeader, InputStream> {

    @Inject
    internal lateinit var dataFetcherFactory: MeshyDataFetcherFactory

    init {
        app.getApplicationComponent().inject(this)
    }

    override fun buildLoadData(
        model: AttachmentHeader,
        width: Int,
        height: Int,
        options: Options,
    ): ModelLoader.LoadData<InputStream> {
        val key = ObjectKey(model.messageId)
        val dataFetcher = dataFetcherFactory.create(model)
        return ModelLoader.LoadData(key, dataFetcher)
    }

    override fun handles(model: AttachmentHeader): Boolean = true
}