package com.base.android.di.module;

import android.content.Context;

import androidx.core.util.Supplier;
import androidx.lifecycle.ViewModelProvider;

import com.base.android.MVVMApplication;
import com.base.android.ViewModelProviderFactory;
import com.base.android.data.Repository;
import com.base.android.di.scope.ActivityScope;
import com.base.android.ui.base.activity.BaseActivity;
import com.base.android.ui.main.MainViewModel;
import com.base.android.ui.main.account.login.LoginViewModel;
import com.base.android.ui.main.qrscan.QRScanViewModel;
import com.base.android.ui.main.splash.SplashViewModel;
import com.base.android.utils.GetInfo;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module
public class ActivityModule {

    private BaseActivity<?, ?> activity;

    public ActivityModule(BaseActivity<?, ?> activity) {
        this.activity = activity;
    }

    @Named("access_token")
    @Provides
    @ActivityScope
    String provideToken(Repository repository){
        return repository.getToken();
    }

    @Named("device_id")
    @Provides
    @ActivityScope
    String provideDeviceId( Context applicationContext){
        return GetInfo.getAll(applicationContext);
    }


    @Provides
    @ActivityScope
    MainViewModel provideMainViewModel(Repository repository, Context application) {
        Supplier<MainViewModel> supplier = () -> new MainViewModel(repository, (MVVMApplication)application);
        ViewModelProviderFactory<MainViewModel> factory = new ViewModelProviderFactory<>(MainViewModel.class, supplier);
        return new ViewModelProvider(activity, factory).get(MainViewModel.class);
    }

    @Provides
    @ActivityScope
    LoginViewModel provideLoginViewModel(Repository repository, Context application) {
        Supplier<LoginViewModel> supplier = () -> new LoginViewModel(repository, (MVVMApplication)application);
        ViewModelProviderFactory<LoginViewModel> factory = new ViewModelProviderFactory<>(LoginViewModel.class, supplier);
        return new ViewModelProvider(activity, factory).get(LoginViewModel.class);
    }

    @Provides
    @ActivityScope
    SplashViewModel provideSplashViewModel(Repository repository, Context application) {
        Supplier<SplashViewModel> supplier = () -> new SplashViewModel(repository, (MVVMApplication) application);
        ViewModelProviderFactory<SplashViewModel> factory = new ViewModelProviderFactory<>(SplashViewModel.class, supplier);
        return new ViewModelProvider(activity, factory).get(SplashViewModel.class);
    }

    @Provides
    @ActivityScope
    QRScanViewModel provideQRScanViewModel(Repository repository, Context application) {
        Supplier<QRScanViewModel> supplier = () -> new QRScanViewModel(repository, (MVVMApplication) application);
        ViewModelProviderFactory<QRScanViewModel> factory = new ViewModelProviderFactory<>(QRScanViewModel.class, supplier);
        return new ViewModelProvider(activity, factory).get(QRScanViewModel.class);
    }
}
