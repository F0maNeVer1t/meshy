package ru.itis.meshy.android.priority

import android.content.Context
import com.github.jfasttext.JFastText
import org.briarproject.nullsafety.NotNullByDefault
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.exp

/**
 * Классифицирует сообщения на стандартные и emergency-приоритетные
 * через локальную fastText-модель.
 *
 * Inference: 1-5 мс на CPU, никаких сетевых запросов, модель полностью
 * локальная и не нарушает E2E-шифрование Briar/Meshy: классификация
 * происходит до шифрования на устройстве отправителя.
 */
@Singleton
@NotNullByDefault
class MessagePriorityClassifier @Inject constructor(
    private val context: Context,
) {

    /**
     * Порог уверенности для класса emergency. Подобран экспериментально
     * на validation-выборке: при значении 0.85 достигается precision ≥ 0.95
     * (минимизация false-positives — пользователь не должен получать
     * "ложные тревоги" в обычной переписке).
     */
    private val confidenceThreshold = 0.85

    private val model: JFastText by lazy {
        val modelFile = copyModelFromAssets()
        JFastText().apply { loadModel(modelFile.absolutePath) }
    }

    fun classify(text: String): MessagePriority {
        if (text.isBlank()) return MessagePriority.STANDARD

        val preprocessed = preprocess(text)
        val predictions = model.predictProba(preprocessed, 2)
        if (predictions.isEmpty()) return MessagePriority.STANDARD

        val top = predictions[0]
        val topLabel = top.label
        val topConfidence = exp(top.logProb)

        return if (topLabel == LABEL_EMERGENCY && topConfidence >= confidenceThreshold) {
            MessagePriority.EMERGENCY
        } else {
            MessagePriority.STANDARD
        }
    }

    /**
     * JFastText требует filesystem path к модели (не InputStream),
     * поэтому копируем .ftz из APK assets в private files dir.
     * Копирование выполняется один раз — при первом обращении к [model].
     */
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
        // Та же нормализация, что и при обучении модели в Python-скрипте
        return text.lowercase().trim()
    }

    companion object {
        private const val MODEL_FILE_NAME = "meshy_priority.ftz"
        private const val LABEL_EMERGENCY = "__label__emergency"
    }
}