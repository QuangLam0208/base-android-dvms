package com.base.android.data.model.other;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.base.android.R;

import lombok.Data;

@Data
public class ToastMessage {
    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_SUCCESS = 1;
    public static final int TYPE_ERROR = 2;
    public static final int TYPE_WARNING = 3;

    private int type;
    private String message;

    public ToastMessage(int type, String message) {
        this.type = type;
        this.message = message;
    }

    public void showMessage(Context context){
        if (Looper.myLooper() != Looper.getMainLooper()) {
            new Handler(Looper.getMainLooper()).post(() -> showMessage(context));
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(context);
        @SuppressLint("InflateParams") View layout = inflater.inflate(R.layout.layout_toast, null);

        ImageView icon = layout.findViewById(R.id.icon);
        TextView text = layout.findViewById(R.id.mgs);

        text.setText(message);
        Toast toast = new Toast(context);

        switch (type){
            case TYPE_SUCCESS:
                icon.setImageResource(R.drawable.ic_toast_success);
                layout.setBackgroundResource(R.drawable.bg_toast_success);
                break;
            case TYPE_ERROR:
                icon.setImageResource(R.drawable.ic_toast_error);
                layout.setBackgroundResource(R.drawable.bg_toast_error);
                break;
            case TYPE_WARNING:
                icon.setImageResource(R.drawable.ic_toast_warning);
                layout.setBackgroundResource(R.drawable.bg_toast_warning);
                break;
            case TYPE_NORMAL:
            default:
                icon.setImageResource(R.drawable.ic_bell);
                layout.setBackgroundResource(R.drawable.bg_toast_normal);
                break;
        }
        toast.setView(layout);
        toast.setDuration(Toast.LENGTH_SHORT);

        // Hiển thị ở Top Center (phía trên, cách mép trên màn hình hợp lý dưới StatusBar)
        // để không bị che bởi BottomNavigationView hay bàn phím
        int topOffset = context.getResources().getDimensionPixelSize(R.dimen._45sdp);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, topOffset);
        toast.show();
    }
}
