package ru.mammoth70.wherearetheynow

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class AboutBox : DialogFragment() {
    // Класс создаёт диалоговое окно About.

    companion object {
        const val DIALOG_MESSAGE: String = "DIALOG_MESSAGE"
        const val DIALOG_TITLE: String = "DIALOG_TITLE"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity(),
            R.style.AboutDialogStyle)
        builder.setIcon(R.mipmap.ic_launcher_round)
        builder.setPositiveButton(R.string.ok) { dialog: DialogInterface?, id: Int -> }
        if (arguments != null) {
            builder.setView(R.layout.dialog_about)
            builder.setTitle(requireArguments().getString(DIALOG_TITLE))
            builder.setMessage(requireArguments().getString(DIALOG_MESSAGE))
        }
        return builder.create()
    }

}