package ru.itis.meshy.android.conversation.glide

import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.DataSource.LOCAL
import com.bumptech.glide.load.data.DataFetcher
import ru.itis.messaging_engine.api.db.DatabaseExecutor
import ru.itis.messaging_engine.api.db.DbException
import ru.itis.messaging_engine.util.IoUtils.tryToClose
import ru.itis.meshy.api.attachment.AttachmentHeader
import ru.itis.meshy.api.attachment.AttachmentReader
import org.briarproject.nullsafety.NotNullByDefault
import java.io.InputStream
import java.util.concurrent.Executor
import java.util.logging.Level.WARNING
import java.util.logging.Logger
import javax.inject.Inject

/**
 * Glide [DataFetcher] для подгрузки вложений из БД messaging-engine.
 *
 * Создаётся фабрикой [MeshyDataFetcherFactory] на каждый attachment.
 * Бывший `BriarDataFetcher`.
 *
 * Поля `inputStream` и `canceled` помечены `@Volatile`, потому что
 * читаются и пишутся из разных потоков: задача загрузки выполняется
 * на DB executor'е, а `cancel()` / `cleanup()` могут быть вызваны
 * из любого потока, в т.ч. UI.
 */
@NotNullByDefault
internal class MeshyDataFetcher @Inject constructor(
    private val attachmentReader: AttachmentReader,
    @param:DatabaseExecutor private val dbExecutor: Executor,
    private val attachmentHeader: AttachmentHeader,
) : DataFetcher<InputStream> {

    @Volatile
    private var inputStream: InputStream? = null

    @Volatile
    private var canceled: Boolean = false

    override fun loadData(
        priority: Priority,
        callback: DataFetcher.DataCallback<in InputStream>,
    ) {
        dbExecutor.execute {
            if (canceled) return@execute
            try {
                val a = attachmentReader.getAttachment(attachmentHeader)
                val stream = a.stream
                inputStream = stream
                callback.onDataReady(stream)
            } catch (e: DbException) {
                callback.onLoadFailed(e)
            }
        }
    }

    override fun cleanup() {
        tryToClose(inputStream, LOG, WARNING)
    }

    override fun cancel() {
        canceled = true
    }

    override fun getDataClass(): Class<InputStream> = InputStream::class.java

    override fun getDataSource(): DataSource = LOCAL

    companion object {
        private val LOG: Logger = Logger.getLogger(MeshyDataFetcher::class.java.name)
    }
}