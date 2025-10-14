package ru.mammoth70.wherearetheynow;

import androidx.appcompat.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class AboutBox extends DialogFragment {
    // Класс создаёт диалоговое окно About.
    public final static String DIALOG_MESSAGE= "DIALOG_MESSAGE";
    public final static String DIALOG_TITLE= "DIALOG_TITLE";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(),R.style.AboutDialogStyle);
        builder.setIcon(R.mipmap.ic_launcher_round);
        builder.setPositiveButton(R.string.ok, (dialog, id) -> {
            // OK
        });
        if (getArguments() != null) {
            builder.setView(R.layout.dialog_about);
            builder.setTitle(getArguments().getString(DIALOG_TITLE));
            builder.setMessage(getArguments().getString(DIALOG_MESSAGE));
        }
        return builder.create();
    }

}