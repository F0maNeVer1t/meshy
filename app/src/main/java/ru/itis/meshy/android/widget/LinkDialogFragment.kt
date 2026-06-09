package ru.itis.meshy.android.widget

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.fragment.app.DialogFragment
import ru.itis.meshy.R

class LinkDialogFragment : DialogFragment() {

	private lateinit var url: String

	override fun onCreate(
		savedInstanceState: Bundle?
	) {
		super.onCreate(savedInstanceState)

		val args = requireArguments()

		url = requireNotNull(
			args.getString(ARG_URL)
		)

		setStyle(
			STYLE_NO_TITLE,
			R.style.MeshyDialogTheme
		)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {

		val view = inflater.inflate(
			R.layout.fragment_link_dialog,
			container,
			false
		)

		val urlView: TextView =
			view.findViewById(R.id.urlView)

		urlView.text = url

		// Prepare normal intent or chooser
		val context = requireContext()

		val baseIntent = Intent(
			ACTION_VIEW,
			Uri.parse(url)
		)

		val packageManager =
			context.packageManager

		val activities =
			packageManager.queryIntentActivities(
				baseIntent,
				MATCH_DEFAULT_ONLY
			)

		val shouldShowChooser =
			activities.size > 1

		val intent =
			if (shouldShowChooser) {
				Intent.createChooser(
					baseIntent,
					getString(
						R.string.link_warning_open_link
					)
				)
			} else {
				baseIntent
			}

		val openButton: Button =
			view.findViewById(R.id.openButton)

		openButton.setOnClickListener {

			if (
				intent.resolveActivity(packageManager)
				!= null
			) {
				startActivity(intent)
			} else {

				Toast.makeText(
					context,
					R.string.error_start_activity,
					LENGTH_SHORT
				).show()
			}

			dialog?.dismiss()
		}

		val cancelButton: Button =
			view.findViewById(R.id.cancelButton)

		cancelButton.setOnClickListener {
			dialog?.cancel()
		}

		return view
	}

	fun getUniqueTag(): String {
		return TAG
	}

	companion object {

		private val TAG: String =
			LinkDialogFragment::class.java.name

		private const val ARG_URL = "url"

        @JvmStatic
		fun newInstance(
			url: String
		): LinkDialogFragment {

			return LinkDialogFragment().apply {

				arguments = Bundle().apply {
					putString(ARG_URL, url)
				}
			}
		}
	}
}