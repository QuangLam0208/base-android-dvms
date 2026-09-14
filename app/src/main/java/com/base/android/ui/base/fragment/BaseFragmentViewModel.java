package com.base.android.ui.base.fragment;

import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.base.android.MVVMApplication;
import com.base.android.data.Repository;
import com.base.android.data.model.other.ToastMessage;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import lombok.Setter;

public class BaseFragmentViewModel extends ViewModel {

    protected final Repository repository;
    protected final MVVMApplication application;
    protected final MutableLiveData<ToastMessage> mErrorMessage = new MutableLiveData<>();
    protected final ObservableBoolean mIsLoading = new ObservableBoolean();
    protected final CompositeDisposable compositeDisposable = new CompositeDisposable();


    @Setter
    protected String token;

    public BaseFragmentViewModel(Repository repository, MVVMApplication application) {
        this.repository = repository;
        this.application = application;
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
    public void showLoading(){
        mIsLoading.set(true);
    }

    public void hideLoading(){
        mIsLoading.set(false);
    }

    public void setLanguage(String code) {
        repository.getSharedPreferences().setAppLanguage(code);
    }

    public String getLanguage() {
        return repository.getSharedPreferences().getAppLanguage();
    }

    public void setTheme(String mode) {
        repository.getSharedPreferences().setAppTheme(mode);
    }

    public String getTheme() {
        return repository.getSharedPreferences().getAppTheme();
    }

    @Override
    protected void onCleared() {
        compositeDisposable.dispose();
        super.onCleared();
    }
}

