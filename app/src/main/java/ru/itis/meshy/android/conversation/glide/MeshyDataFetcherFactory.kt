package ru.itis.meshy.android.conversation.glide

import ru.itis.messaging_engine.api.db.DatabaseExecutor
import ru.itis.meshy.api.attachment.AttachmentHeader
import ru.itis.meshy.api.attachment.AttachmentReader
import org.briarproject.nullsafety.NotNullByDefault
import java.util.concurrent.Executor
import javax.inject.Inject

/**
 * Фабрика [MeshyDataFetcher]-ов. Принимает зависимости через DI один раз,
 * а потом дёшево порождает фетчер на каждый загружаемый attachment.
 * Бывший `BriarDataFetcherFactory`.
 */
@NotNullByDefault
class MeshyDataFetcherFactory @Inject constructor(
    private val attachmentReader: AttachmentReader,
    @param:DatabaseExecutor private val dbExecutor: Executor,
) {
    internal fun create(model: AttachmentHeader): MeshyDataFetcher =
        MeshyDataFetcher(attachmentReader, dbExecutor, model)
}