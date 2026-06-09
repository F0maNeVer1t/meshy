package ru.itis.meshy.android.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.FLAG_WINDOW_IS_OBSCURED
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import ru.itis.meshy.android.util.UiUtils

class TapSafeFrameLayout : FrameLayout {

	private var listener: OnTapFilteredListener? = null

	constructor(
		context: Context
	) : super(context) {

		UiUtils.setFilterTouchesWhenObscured(
			this,
			false
		)
	}

	constructor(
		context: Context,
		attrs: AttributeSet?
	) : super(context, attrs) {

		UiUtils.setFilterTouchesWhenObscured(
			this,
			false
		)
	}

	constructor(
		context: Context,
		attrs: AttributeSet?,
		@AttrRes defStyleAttr: Int
	) : super(
		context,
		attrs,
		defStyleAttr
	) {

		UiUtils.setFilterTouchesWhenObscured(
			this,
			false
		)
	}

	fun setOnTapFilteredListener(
		listener: OnTapFilteredListener
	) {
		this.listener = listener
	}

	override fun onFilterTouchEventForSecurity(
		event: MotionEvent
	): Boolean {

		val obscured =
			(event.flags and FLAG_WINDOW_IS_OBSCURED) != 0

		return if (obscured && listener != null) {
			listener!!.shouldAllowTap()
		} else {
			!obscured
		}
	}

	interface OnTapFilteredListener {

		fun shouldAllowTap(): Boolean
	}
}