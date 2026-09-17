package com.base.android.ui.base.activity;

import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.base.android.MVVMApplication;
import com.base.android.data.Repository;
import com.base.android.data.model.other.ToastMessage;
import com.base.android.helper.ThemeHelper;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import lombok.Getter;
import lombok.Setter;

public class BaseViewModel extends ViewModel {
    protected CompositeDisposable compositeDisposable;
    protected final ObservableBoolean mIsLoading = new ObservableBoolean();
    protected final MutableLiveData<String> progressBarMsg = new MutableLiveData<>();
    protected final MutableLiveData<ToastMessage> mErrorMessage = new MutableLiveData<>();

    @Setter
    protected String token;

    @Setter
    protected String deviceId;

    protected final Repository repository;
    @Getter
    protected final MVVMApplication application;

    public final ObservableBoolean isNightMode = new ObservableBoolean();

    public BaseViewModel(Repository repository, MVVMApplication application){
        this.repository = repository;
        this.application = application;
        this.compositeDisposable = new CompositeDisposable();
        boolean isDark = ThemeHelper.isDarkMode(application, repository.getSharedPreferences().getAppTheme());
        this.isNightMode.set(isDark);
    }

    public void setNightMode(boolean isDark) {
        this.isNightMode.set(isDark);
    }

    @Override
    protected void onCleared() {
        compositeDisposable.dispose();
        super.onCleared();
    }

    public void showLoading(){
        mIsLoading.set(true);
    }

    public void hideLoading(){
        mIsLoading.set(false);
    }

    public void showSuccessMessage(String message){
        mErrorMessage.setValue(new ToastMessage(ToastMessage.TYPE_SUCCESS,message));
    }

    public void showNormalMessage(String message){
        mErrorMessage.setValue(new ToastMessage(ToastMessage.TYPE_NORMAL,message));
    }

    public void showWarningMessage(String message){
        mErrorMessage.setValue(new ToastMessage(ToastMessage.TYPE_WARNING,message));
    }

    public void showErrorMessage(String message){
        mErrorMessage.setValue(new ToastMessage(ToastMessage.TYPE_ERROR,message));
    }

    public void changeProgressBarMsg(String message){
        progressBarMsg.setValue(message);
    }

    public void setLanguage(String code) {
        repository.getSharedPreferences().setAppLanguage(code);
    }

    public String getLanguage() {
        return repository.getSharedPreferences().getAppLanguage();
    }
}
