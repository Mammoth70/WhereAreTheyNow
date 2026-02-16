package ru.mammoth70.wherearetheynow

import android.app.Dialog
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AboutBox : DialogFragment() {
    // Диалоговое окно About.

    companion object {
        const val ABOUT_MESSAGE = "ABOUT_MESSAGE"
        const val ABOUT_TITLE = "ABOUT_TITLE"
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Функция создаёт диалоговое окно About.

        val customView = layoutInflater.inflate(R.layout.dialog_about, null)
        val textView = customView.findViewById<TextView>(R.id.about_text_content)

        textView.text = arguments?.getString(ABOUT_MESSAGE)
        textView.movementMethod = LinkMovementMethod.getInstance()

        return MaterialAlertDialogBuilder(requireActivity())
            .setIcon(R.mipmap.ic_launcher_round)
            .setTitle(arguments?.getString(ABOUT_TITLE))
            .setView(customView)
            .setPositiveButton(R.string.ok, null)
            .create()
    }

}