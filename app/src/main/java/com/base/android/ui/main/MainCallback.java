package com.base.android.ui.main;

import com.base.android.data.model.api.ResponseWrapper;
import com.base.android.ui.base.activity.BaseCallback;

public interface MainCallback<T> extends BaseCallback {
    default void doSuccess(T object) {
    }

    default void doErrorForm(ResponseWrapper<?> response) {
    }
}

