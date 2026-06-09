package ru.itis.meshy.android.widget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import ru.itis.meshy.R

class OnboardingFullDialogFragment : DialogFragment() {

	override fun onCreate(
		savedInstanceState: Bundle?
	) {
		super.onCreate(savedInstanceState)

		setStyle(
			STYLE_NORMAL,
			R.style.MeshyFullScreenDialogTheme
		)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {

		val view = inflater.inflate(
			R.layout.fragment_onboarding_full,
			container,
			false
		)

		val args = requireArguments()

		val toolbar: Toolbar =
			view.findViewById(R.id.toolbar)

		toolbar.setNavigationOnClickListener {
			dismiss()
		}

		toolbar.setTitle(
			args.getInt(RES_TITLE)
		)

		val contentView: TextView =
			view.findViewById(R.id.contentView)

		contentView.setText(
			args.getInt(RES_CONTENT)
		)

		view.findViewById<View>(R.id.button)
			.setOnClickListener {
				dismiss()
			}

		return view
	}

	companion object {

        @JvmField
		val TAG: String =
			OnboardingFullDialogFragment::class.java.name

		private const val RES_TITLE = "resTitle"

		private const val RES_CONTENT = "resContent"

        @JvmStatic
		fun newInstance(
			@StringRes title: Int,
			@StringRes content: Int
		): OnboardingFullDialogFragment {

			return OnboardingFullDialogFragment().apply {

				arguments = Bundle().apply {

					putInt(RES_TITLE, title)

					putInt(RES_CONTENT, content)
				}
			}
		}
	}
}