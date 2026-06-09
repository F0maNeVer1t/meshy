package ru.itis.meshy.android.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION
import android.text.TextUtils
import android.view.View.GONE
import android.widget.CheckBox
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.briarproject.nullsafety.MethodsNotNullByDefault
import org.briarproject.nullsafety.ParametersNotNullByDefault
import ru.itis.meshy.R
import ru.itis.meshy.android.activity.BaseActivity
import ru.itis.meshy.android.util.UiUtils.tryToStartActivity
import ru.itis.meshy.api.android.ScreenFilterMonitor
import ru.itis.meshy.api.android.ScreenFilterMonitor.AppDetails
import javax.inject.Inject

/**
 * Диалог с предупреждением о наличии overlay-приложений, способных
 * перехватывать тапы. Показывается из [BaseActivity], когда пользователь
 * касается экрана при активных overlay-приложениях.
 *
 * На API ≤ 29 — отображает список и предлагает галочку «разрешить эти
 * приложения». На API ≥ 30 — список запросить нельзя (изменения в Android),
 * предлагается перейти в системные настройки overlay-разрешений.
 */
@MethodsNotNullByDefault
@ParametersNotNullByDefault
class ScreenFilterDialogFragment : DialogFragment() {

    @Inject
    internal lateinit var screenFilterMonitor: ScreenFilterMonitor

    private var dismissListener: DismissListener? = null

    fun setDismissListener(dismissListener: DismissListener) {
        this.dismissListener = dismissListener
    }

    override fun onAttach(ctx: Context) {
        super.onAttach(ctx)
        (requireActivity() as BaseActivity).activityComponent.inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()
        val builder = MaterialAlertDialogBuilder(activity, R.style.MeshyDialogThemeNoFilter)
        builder.setTitle(R.string.screen_filter_title)

        val args = requireArguments()
        val appNames = checkNotNull(args.getStringArrayList(ARG_APP_NAMES)) {
            "Missing argument: $ARG_APP_NAMES"
        }
        val packageNames = checkNotNull(args.getStringArrayList(ARG_PACKAGE_NAMES)) {
            "Missing argument: $ARG_PACKAGE_NAMES"
        }

        // See https://stackoverflow.com/a/24720976/6314875
        @SuppressLint("InflateParams")
        val dialogView = activity.layoutInflater.inflate(R.layout.dialog_screen_filter, null)
        builder.setView(dialogView)

        val message = dialogView.findViewById<TextView>(R.id.screen_filter_message)
        val allow = dialogView.findViewById<CheckBox>(R.id.screen_filter_checkbox)

        if (SDK_INT <= 29) {
            message.text = getString(R.string.screen_filter_body, TextUtils.join("\n", appNames))
        } else {
            message.setText(R.string.screen_filter_body_api_30)
            allow.visibility = GONE
            builder.setNeutralButton(R.string.screen_filter_review_apps) { _, _ ->
                val i = Intent(ACTION_MANAGE_OVERLAY_PERMISSION)
                tryToStartActivity(requireActivity(), i)
            }
        }
        builder.setPositiveButton(R.string.continue_button) { dialog, _ ->
            if (allow.isChecked) screenFilterMonitor.allowApps(packageNames)
            dialog.dismiss()
        }
        builder.setCancelable(false)
        return builder.create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissListener?.onDialogDismissed()
    }

    /**
     * Callback на закрытие диалога. `fun interface` — поддерживает
     * SAM-conversion из Kotlin-лямбды: `dialog.setDismissListener { ... }`.
     */
    fun interface DismissListener {
        fun onDialogDismissed()
    }

    companion object {
        @JvmField
        val TAG: String = ScreenFilterDialogFragment::class.java.name

        private const val ARG_APP_NAMES = "appNames"
        private const val ARG_PACKAGE_NAMES = "packageNames"

        @JvmStatic
        fun newInstance(apps: Collection<AppDetails>): ScreenFilterDialogFragment =
            ScreenFilterDialogFragment().apply {
                arguments = Bundle().apply {
                    putStringArrayList(ARG_APP_NAMES, ArrayList(apps.map { it.name }))
                    putStringArrayList(ARG_PACKAGE_NAMES, ArrayList(apps.map { it.packageName }))
                }
            }
    }
}