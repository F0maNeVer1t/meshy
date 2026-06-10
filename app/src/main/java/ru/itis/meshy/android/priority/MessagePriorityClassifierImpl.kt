package ru.itis.meshy.android.priority

import android.content.Context
import android.util.Log
import org.briarproject.nullsafety.NotNullByDefault
import ru.itis.meshy.api.messaging.priority.MessagePriority
import ru.itis.meshy.api.messaging.priority.MessagePriorityClassifier
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@NotNullByDefault
class MessagePriorityClassifierImpl @Inject constructor(
    private val context: Context,
) : MessagePriorityClassifier {

    private val confidenceThreshold = 0.85

    private val modelLoaded: Boolean by lazy {
        val modelFile = copyModelFromAssets()
        FastTextNative.loadModel(modelFile.absolutePath)
    }

    override fun classify(text: String): MessagePriority {
        if (text.isBlank()) return MessagePriority.STANDARD
        if (!modelLoaded) return MessagePriority.STANDARD

        val preprocessed = preprocess(text)
        val results = FastTextNative.predict(preprocessed, 2)
            ?: return MessagePriority.STANDARD

        if (results.size < 2) return MessagePriority.STANDARD

        val topLabel = results[0]
        val topConfidence = results[1].toDoubleOrNull() ?: 0.0

        val priority = if (topLabel == LABEL_EMERGENCY && topConfidence >= confidenceThreshold) {
            MessagePriority.EMERGENCY
        } else {
            MessagePriority.STANDARD
        }
        Log.d(TAG, "text=\"$preprocessed\" label=$topLabel conf=$topConfidence -> ${priority.name}")
        return priority
    }

    private fun copyModelFromAssets(): File {
        val modelFile = File(context.filesDir, MODEL_FILE_NAME)
        if (!modelFile.exists()) {
            context.assets.open(MODEL_FILE_NAME).use { input ->
                modelFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
        return modelFile
    }

    private fun preprocess(text: String): String {
        return text.lowercase().trim()
    }

    companion object {
        private const val TAG = "MeshyPriority"
        private const val MODEL_FILE_NAME = "meshy_priority.ftz"
        private const val LABEL_EMERGENCY = "__label__emergency"
    }
}
