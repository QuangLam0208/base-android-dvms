package com.base.android.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.base.android.R;

public class DialogUtils {

    private DialogUtils(){
        //do not init
    }

    public static AlertDialog dialogConfirm(Context context,
                                            String msg,
                                            String btnPositive,
                                            DialogInterface.OnClickListener positive,
                                            String btnNegative,
                                            DialogInterface.OnClickListener negative) {

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setMessage(msg)
                .setPositiveButton(btnPositive, positive)
                .setNegativeButton(btnNegative, negative)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
        TextView message = dialog.findViewById(android.R.id.message);
        if (message != null) {
            message.setTextSize(context.getResources().getDimension(R.dimen._7ssp));
        }
        Button buttonPositive = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (buttonPositive != null) {
            buttonPositive.setTextSize(context.getResources().getDimension(R.dimen._6ssp));
        }

        Button buttonN = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        if (buttonN != null) {
            buttonN.setTextSize(context.getResources().getDimension(R.dimen._6ssp));
        }
        return dialog;
    }
}
