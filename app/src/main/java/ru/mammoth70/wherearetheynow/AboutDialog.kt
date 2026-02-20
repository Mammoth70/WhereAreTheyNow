package ru.mammoth70.wherearetheynow

import android.app.Dialog
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AboutDialog : DialogFragment() {
    // Диалоговое окно About.


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Функция создаёт диалоговое окно About.

        val customView = layoutInflater.inflate(R.layout.dialog_about, null)
        val textView = customView.findViewById<TextView>(R.id.about_text_content)
        val title = getString(R.string.app_name)
        val text = """
                        |${getString(R.string.description)}
                        |${getString(R.string.version)} ${BuildConfig.VERSION_NAME}
                        |
                        |${getString(R.string.for_about)}
                        """.trimMargin()
        textView.text = text
        textView.movementMethod = LinkMovementMethod.getInstance()

        return MaterialAlertDialogBuilder(requireActivity())
            .setIcon(R.mipmap.ic_launcher_round)
            .setTitle(title)
            .setView(customView)
            .setPositiveButton(R.string.ok, null)
            .create()
    }

}