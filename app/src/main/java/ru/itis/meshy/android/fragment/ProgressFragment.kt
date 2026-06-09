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
 * Простой фрагмент-«индикатор»: показывает текст в центре экрана.
 * Сообщение передаётся через arguments в [newInstance].
 */
@MethodsNotNullByDefault
@ParametersNotNullByDefault
class ProgressFragment : BaseFragment() {

    private lateinit var progressMessage: String

    override val uniqueTag: String get() = TAG

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressMessage = requireArguments().getString(PROGRESS_MSG)
            ?: error("Missing argument: $PROGRESS_MSG")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val v = inflater.inflate(R.layout.fragment_progress, container, false)
        val msg = v.findViewById<TextView>(R.id.progressMessage)
        msg.text = progressMessage
        return v
    }

    companion object {
        @JvmField
        val TAG: String = ProgressFragment::class.java.name

        private const val PROGRESS_MSG = "progressMessage"

        @JvmStatic
        fun newInstance(message: String): ProgressFragment = ProgressFragment().apply {
            arguments = Bundle().apply { putString(PROGRESS_MSG, message) }
        }
    }
}