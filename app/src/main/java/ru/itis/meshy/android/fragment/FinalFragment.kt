package ru.itis.meshy.android.fragment

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat.setImageTintList
import androidx.fragment.app.Fragment
import ru.itis.meshy.R
import ru.itis.meshy.android.util.UiUtils.hideViewOnSmallScreen

/**
 * A fragment used at the end of a user flow
 * where the user should not be able to navigate back.
 *
 * Displays final information before closing the activity.
 */
open class FinalFragment : Fragment() {

	protected lateinit var buttonView: Button

    @JvmField
	protected val onBackPressedCallback =
		object : OnBackPressedCallback(true) {

			override fun handleOnBackPressed() {
				onBackButtonPressed()
			}
		}

    override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		val view = inflater.inflate(
			R.layout.fragment_final,
			container,
			false
		)

		val iconView: ImageView =
			view.findViewById(R.id.iconView)

		val titleView: TextView =
			view.findViewById(R.id.titleView)

		val textView: TextView =
			view.findViewById(R.id.textView)

		buttonView = view.findViewById(R.id.button)

		val args = requireArguments()

		titleView.setText(args.getInt(ARG_TITLE))

		iconView.setImageResource(args.getInt(ARG_ICON))

        @ColorRes
		val tintRes = args.getInt(ARG_ICON_TINT)

        if (tintRes != 0) {

            val color = ContextCompat.getColor(
                requireContext(),
                tintRes
            )

            val tint = ColorStateList.valueOf(color)

            setImageTintList(iconView, tint)
        }

		val textRes = args.getInt(ARG_TEXT)

		if (textRes == 0) {
			textView.visibility = GONE
		} else {
			textView.setText(textRes)
		}

		buttonView.setOnClickListener {
			onBackButtonPressed()
		}

		val activity = requireActivity() as AppCompatActivity

		activity.setTitle(args.getInt(ARG_TITLE))

		activity.onBackPressedDispatcher.addCallback(
			viewLifecycleOwner,
			onBackPressedCallback
		)

		return view
	}

	override fun onAttach(context: Context) {
		super.onAttach(context)

		// onAttach(Activity) is deprecated,
		// therefore Context should be cast instead
		val activity = context as AppCompatActivity

		val actionBar: ActionBar? =
			activity.supportActionBar

		if (shouldHideActionBarBackButton() &&
			actionBar != null
		) {
			actionBar.setDisplayHomeAsUpEnabled(false)
			actionBar.setHomeButtonEnabled(false)
		}
	}

	override fun onStart() {
		super.onStart()

		hideViewOnSmallScreen(
			requireView().findViewById(R.id.iconView)
		)
	}

	override fun onDetach() {
		val activity = requireActivity() as AppCompatActivity

		val actionBar: ActionBar? =
			activity.supportActionBar

		if (shouldHideActionBarBackButton() &&
			actionBar != null
		) {
			actionBar.setDisplayHomeAsUpEnabled(true)
			actionBar.setHomeButtonEnabled(true)
		}

		super.onDetach()
	}

	/**
	 * Action performed by both the system back button
	 * and the bottom action button.
	 */
	protected open fun onBackButtonPressed() {
		requireActivity().supportFinishAfterTransition()
	}

	/**
	 * If [onBackButtonPressed] is overridden and the fragment
	 * is no longer finishing the activity, return false here.
	 *
	 * Otherwise the ActionBar back button will disappear.
	 */
	protected open fun shouldHideActionBarBackButton(): Boolean {
		return true
	}

	companion object {

        @JvmField
		val TAG: String = FinalFragment::class.java.name

		const val ARG_TITLE = "title"
		const val ARG_ICON = "icon"
		const val ARG_ICON_TINT = "iconTint"
		const val ARG_TEXT = "text"

        @JvmStatic
		fun newInstance(
			@StringRes title: Int,
			@DrawableRes icon: Int,
			@ColorRes iconTint: Int,
			@StringRes text: Int
		): FinalFragment {
			return FinalFragment().apply {
				arguments = Bundle().apply {
					putInt(ARG_TITLE, title)
					putInt(ARG_ICON, icon)
					putInt(ARG_ICON_TINT, iconTint)
					putInt(ARG_TEXT, text)
				}
			}
		}
	}
}