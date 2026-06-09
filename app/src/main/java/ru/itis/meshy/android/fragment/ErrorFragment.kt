package ru.itis.meshy.android.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.briarproject.nullsafety.MethodsNotNullByDefault
import org.briarproject.nullsafety.ParametersNotNullByDefault
import ru.itis.meshy.R

/**
 * Простой фрагмент-«ошибка»: показывает текст сообщения в центре экрана.
 * Используется, например, из [ru.itis.meshy.android.StartupFailureActivity]
 * для отображения причины невозможности запуска.
 */
@MethodsNotNullByDefault
@ParametersNotNullByDefault
class ErrorFragment : BaseFragment() {

    private lateinit var errorMessage: String

    override val uniqueTag: String get() = TAG

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        errorMessage = requireArguments().getString(ERROR_MSG)
            ?: error("Missing argument: $ERROR_MSG")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val v = inflater.inflate(R.layout.fragment_error, container, false)
        val msg = v.findViewById<TextView>(R.id.errorMessage)
        msg.text = errorMessage
        return v
    }

    companion object {
        @JvmField
        val TAG: String = ErrorFragment::class.java.name

        private const val ERROR_MSG = "errorMessage"

        @JvmStatic
        fun newInstance(message: String): ErrorFragment = ErrorFragment().apply {
            arguments = Bundle().apply { putString(ERROR_MSG, message) }
        }
    }
}