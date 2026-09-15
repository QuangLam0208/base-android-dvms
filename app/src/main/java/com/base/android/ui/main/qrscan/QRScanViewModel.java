package com.base.android.ui.main.qrscan;

import androidx.databinding.ObservableField;

import com.base.android.MVVMApplication;
import com.base.android.data.Repository;
import com.base.android.ui.base.activity.BaseViewModel;

public class QRScanViewModel extends BaseViewModel {

    public final ObservableField<String> qrResult = new ObservableField<>("");
    public final ObservableField<Boolean> hasResult = new ObservableField<>(false);

    public QRScanViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }

    public void setQrResult(String result) {
        qrResult.set(result);
        hasResult.set(result != null && !result.isEmpty());
    }

    public void clearResult() {
        qrResult.set("");
        hasResult.set(false);
    }
}
